package com.communi.suggestu.javamark.doclet.writers;

import com.communi.suggestu.javamark.doclet.content.Content;
import com.communi.suggestu.javamark.doclet.content.ContentBuilder;
import com.sun.source.doctree.AttributeTree;
import com.sun.source.doctree.CommentTree;
import com.sun.source.doctree.DocRootTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.ErroneousTree;
import com.sun.source.doctree.EscapeTree;
import com.sun.source.doctree.IndexTree;
import com.sun.source.doctree.InheritDocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.SummaryTree;
import com.sun.source.doctree.SystemPropertyTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.SimpleDocTreeVisitor;
import jdk.javadoc.internal.doclets.formats.html.ModuleWriterImpl;
import jdk.javadoc.internal.doclets.formats.html.PackageWriterImpl;
import jdk.javadoc.internal.doclets.toolkit.taglets.DocRootTaglet;
import jdk.javadoc.internal.doclets.toolkit.util.DocPath;
import jdk.javadoc.internal.doclets.toolkit.util.DocPaths;
import jdk.javadoc.internal.doclets.toolkit.util.Utils;
import jdk.javadoc.internal.doclint.HtmlTag;
import org.apache.commons.lang3.Strings;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor14;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.source.doctree.DocTree.Kind.CODE;
import static com.sun.source.doctree.DocTree.Kind.COMMENT;
import static com.sun.source.doctree.DocTree.Kind.LINK;
import static com.sun.source.doctree.DocTree.Kind.LINK_PLAIN;
import static com.sun.source.doctree.DocTree.Kind.SEE;
import static com.sun.source.doctree.DocTree.Kind.START_ELEMENT;
import static com.sun.source.doctree.DocTree.Kind.TEXT;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class MarkdownDocletWriter
{

    public static class Context {
        /**
         * Whether or not the trees are appearing in a context of just the first sentence,
         * such as in the summary table of the enclosing element.
         */
        final boolean isFirstSentence;
        /**
         * Whether or not the trees are appearing in the "summary" section of the
         * page for a declaration.
         */
        final boolean           inSummary;
        /**
         * The set of enclosing kinds of tags.
         */
        final Set<DocTree.Kind> inTags;

        /**
         * Creates an outermost context, with no enclosing tags.
         *
         * @param isFirstSentence {@code true} if the trees are appearing in a context of just the
         *                        first sentence and {@code false} otherwise
         * @param inSummary       {@code true} if the trees are appearing in the "summary" section
         *                        of the page for a declaration and {@code false} otherwise
         */
        Context(boolean isFirstSentence, boolean inSummary) {
            this(isFirstSentence, inSummary, EnumSet.noneOf(DocTree.Kind.class));
        }

        private Context(boolean isFirstSentence, boolean inSummary, Set<DocTree.Kind> inTags) {
            this.isFirstSentence = isFirstSentence;
            this.inSummary = inSummary;
            this.inTags = inTags;
        }

        /**
         * Creates a new {@code Context} that includes an extra tag kind in the set of enclosing
         * kinds of tags.
         *
         * @param tree the enclosing tree
         *
         * @return the new {@code Context}
         */
        Context within(DocTree tree) {
            var newInTags = EnumSet.copyOf(inTags);
            newInTags.add(tree.getKind());
            return new Context(isFirstSentence, inSummary, newInTags);
        }
    }

    // Notify the next DocTree handler to take necessary action
    private boolean commentRemoved = false;

    /**
     * Converts inline tags and text to content, expanding the
     * inline tags along the way.  Called wherever text can contain
     * an inline tag, such as in comments or in free-form text arguments
     * to block tags.
     *
     * @param element   specific element where comment resides
     * @param trees     list of text trees and inline tag trees (often alternating)
     * @param context   the enclosing context for the trees
     *
     * @return a Content object
     */
    public StringBuilder commentTagsToContent(
        Element element,
        List<? extends DocTree> trees,
        Context context)
    {
        final ContentBuilder result = new ContentBuilder() {
            @Override
            public ContentBuilder add(CharSequence text) {
                return super.add(normalizeNewlines(text));
            }
        };
        commentRemoved = false;
        List<Name> openTags = new ArrayList<>();

        for (ListIterator<? extends DocTree> iterator = trees.listIterator(); iterator.hasNext();) {
            boolean isFirstNode = !iterator.hasPrevious();
            DocTree tag = iterator.next();
            boolean isLastNode  = !iterator.hasNext();

            if (context.isFirstSentence) {
                // Ignore block tags
                if (ignoreNonInlineTag(tag, openTags)) {
                    continue;
                }

                // Ignore any trailing whitespace OR whitespace after removed html comment
                if ((isLastNode || commentRemoved)
                    && tag.getKind() == TEXT
                    && ((tag instanceof TextTree tt) && tt.getBody().isBlank())) {
                    continue;
                }

                // Ignore any leading html comments
                if ((isFirstNode || commentRemoved) && tag.getKind() == COMMENT) {
                    commentRemoved = true;
                    continue;
                }
            }

            var docTreeVisitor = new SimpleDocTreeVisitor<Boolean, Content>() {

                private boolean inAnAtag() {
                    return (tag instanceof StartElementTree st) && Strings.CI.equals(st.getName(), "a");
                }

                @Override
                public Boolean visitAttribute(AttributeTree node, Content content) {
                    if (!content.isEmpty()) {
                        content.add(" ");
                    }
                    content.add(node.getName());
                    if (node.getValueKind() == AttributeTree.ValueKind.EMPTY) {
                        return false;
                    }
                    content.add("=");
                    String quote = switch (node.getValueKind()) {
                        case DOUBLE -> "\"";
                        case SINGLE -> "'";
                        default -> "";
                    };
                    content.add(quote);

                    /* In the following code for an attribute value:
                     * 1. {@docRoot} followed by text beginning "/.." is replaced by the value
                     *    of the docrootParent option, followed by the remainder of the text
                     * 2. in the value of an "href" attribute in a <a> tag, an initial text
                     *    value will have a relative link redirected.
                     * Note that, realistically, it only makes sense to ever use {@docRoot}
                     * at the beginning of a URL in an attribute value, but this is not
                     * required or enforced.
                     */
                    boolean isHRef = inAnAtag() && equalsIgnoreCase(node.getName(), "href");
                    boolean first = true;
                    DocRootTree pendingDocRoot = null;
                    for (DocTree dt : node.getValue()) {
                        if (pendingDocRoot != null) {
                            pendingDocRoot.accept(this, content);
                            pendingDocRoot = null;
                        }

                        if (dt instanceof TextTree tt) {
                            String text = tt.getBody();
                            if (first && isHRef) {
                                text = tt.getBody();
                            }
                            content.add(textCleanup(text, isLastNode));
                        } else if (dt instanceof DocRootTree drt) {
                            // defer until we see what, if anything, follows this node
                            pendingDocRoot = drt;
                        } else {
                            dt.accept(this, content);
                        }
                        first = false;
                    }
                    if (pendingDocRoot != null) {
                        pendingDocRoot.accept(this, content);
                    }

                    content.add(quote);
                    return false;
                }

                @Override
                public Boolean visitComment(CommentTree node, Content content) {
                    content.add("<!--" + node.getBody() + "-->");
                    return false;
                }

                @Override
                public Boolean visitDocRoot(DocRootTree node, Content content) {
                    content.add(getInlineTagOutput(element, node, context));
                    return false;
                }

                @Override
                public Boolean visitEndElement(EndElementTree node, Content content) {
                    content.add(RawHtml.endElement(node.getName()));
                    return false;
                }

                @Override
                public Boolean visitEntity(EntityTree node, Content content) {
                    content.add(Entity.of(node.getName()));
                    return false;
                }

                @Override
                public Boolean visitErroneous(ErroneousTree node, Content content) {
                    DocTreePath dtp = ch.getDocTreePath(node);
                    if (dtp != null) {
                        String body = node.getBody();
                        Matcher m = Pattern.compile("(?i)\\{@([a-z]+).*").matcher(body);
                        String tagName = m.matches() ? m.group(1) : null;
                        if (tagName == null) {
                            if (!configuration.isDocLintSyntaxGroupEnabled()) {
                                messages.warning(dtp, "doclet.tag.invalid_input", body);
                            }
                            content.add(invalidTagOutput(resources.getText("doclet.tag.invalid_input", body),
                                Optional.empty()));
                        } else {
                            messages.warning(dtp, "doclet.tag.invalid_usage", body);
                            content.add(invalidTagOutput(resources.getText("doclet.tag.invalid", tagName),
                                Optional.of(Text.of(body))));
                        }
                    }
                    return false;
                }

                @Override
                public Boolean visitEscape(EscapeTree node, Content content) {
                    result.add(node.getBody());
                    return false;
                }

                @Override
                public Boolean visitInheritDoc(InheritDocTree node, Content content) {
                    Content output = getInlineTagOutput(element, node, context);
                    content.add(output);
                    // if we obtained the first sentence successfully, nothing more to do
                    return (context.isFirstSentence && !output.isEmpty());
                }

                @Override
                public Boolean visitIndex(IndexTree node, Content content) {
                    Content output = getInlineTagOutput(element, node, context);
                    if (output != null) {
                        content.add(output);
                    }
                    return false;
                }

                @Override
                public Boolean visitLink(LinkTree node, Content content) {
                    var inTags = context.inTags;
                    if (inTags.contains(LINK) || inTags.contains(LINK_PLAIN) || inTags.contains(SEE)) {
                        DocTreePath dtp = ch.getDocTreePath(node);
                        if (dtp != null) {
                            messages.warning(dtp, "doclet.see.nested_link", "{@" + node.getTagName() + "}");
                        }
                        Content label = commentTagsToContent(element, node.getLabel(), context);
                        if (label.isEmpty()) {
                            label = Text.of(node.getReference().getSignature());
                        }
                        content.add(label);
                    } else {
                        TagletWriterImpl t = getTagletWriterInstance(context.within(node));
                        content.add(t.linkTagOutput(element, node));
                    }
                    return false;
                }

                @Override
                public Boolean visitLiteral(LiteralTree node, Content content) {
                    String s = node.getBody().getBody();
                    Content t = Text.of(Text.normalizeNewlines(s));
                    content.add(node.getKind() == CODE ? HtmlTree.CODE(t) : t);
                    return false;
                }

                @Override
                public Boolean visitStartElement(StartElementTree node, Content content) {
                    Content attrs = new ContentBuilder();
                    if (node.getName().toString().matches("(?i)h[1-6]")
                        && isIndexable()) {
                        createSectionIdAndIndex(node, trees, attrs, element, context);
                    }
                    for (DocTree dt : node.getAttributes()) {
                        dt.accept(this, attrs);
                    }
                    content.add(RawHtml.startElement(node.getName(), attrs, node.isSelfClosing()));
                    return false;
                }

                @Override
                public Boolean visitSummary(SummaryTree node, Content content) {
                    Content output = getInlineTagOutput(element, node, context);
                    content.add(output);
                    return false;
                }

                @Override
                public Boolean visitSystemProperty(SystemPropertyTree node, Content content) {
                    Content output = getInlineTagOutput(element, node, context);
                    if (output != null) {
                        content.add(output);
                    }
                    return false;
                }

                private CharSequence textCleanup(String text, boolean isLast) {
                    return textCleanup(text, isLast, false);
                }

                private CharSequence textCleanup(String text, boolean isLast, boolean stripLeading) {
                    boolean stripTrailing = context.isFirstSentence && isLast;
                    if (stripLeading && stripTrailing) {
                        text = text.strip();
                    } else if (stripLeading) {
                        text = text.stripLeading();
                    } else if (stripTrailing) {
                        text = text.stripTrailing();
                    }
                    text = utils.replaceTabs(text);
                    return Text.normalizeNewlines(text);
                }

                @Override
                public Boolean visitText(TextTree node, Content content) {
                    String text = node.getBody();
                    result.add(text.startsWith("<![CDATA[")
                        ? RawHtml.cdata(text)
                        : Text.of(textCleanup(text, isLastNode, commentRemoved)));
                    return false;
                }

                @Override
                protected Boolean defaultAction(DocTree node, Content content) {
                    Content output = getInlineTagOutput(element, node, context);
                    if (output != null) {
                        content.add(output);
                    }
                    return false;
                }

            };

            boolean allDone = docTreeVisitor.visit(tag, result);
            commentRemoved = false;

            if (allDone)
                break;
        }
        // Close any open inline tags
        while (!openTags.isEmpty()) {
            result.add(RawHtml.endElement(removeLastHelper(openTags)));
        }
        return result;
    }

    public static CharSequence normalizeNewlines(CharSequence text) {
        // fast-track when the input is a string with no \r characters
        if (text instanceof String s && s.indexOf('\r') != -1) {
            return text;
        } else {
            var sb = new StringBuilder();
            var s = text.toString();
            int sLen = s.length();
            int start = 0;
            int pos;
            while ((pos = s.indexOf('\r', start)) != -1) {
                sb.append(s, start, pos);
                sb.append('\n');
                pos++;
                if (pos < sLen && s.charAt(pos) == '\n') {
                    pos++;
                }
                start = pos;
            }
            sb.append(s.substring(start));
            return sb;
        }
    }

    boolean ignoreNonInlineTag(DocTree dtree, List<Name> openTags) {
        Name name = null;
        DocTree.Kind kind = dtree.getKind();
        if (kind == DocTree.Kind.START_ELEMENT) {
            name = ((StartElementTree)dtree).getName();
        } else if (kind == DocTree.Kind.END_ELEMENT) {
            name = ((EndElementTree)dtree).getName();
        }

        if (name != null) {
            HtmlTag htmlTag = HtmlTag.get(name);
            if (htmlTag != null) {
                if (htmlTag.blockType != HtmlTag.BlockType.INLINE) {
                    return true;
                }
                // Keep track of open inline tags that need to be closed, see 8326332
                if (kind == START_ELEMENT && htmlTag.endKind == HtmlTag.EndKind.REQUIRED) {
                    openTags.add(name);
                } else if (kind == DocTree.Kind.END_ELEMENT && !openTags.isEmpty()
                    && getLastHelper(openTags).equals(name)) {
                    removeLastHelper(openTags);
                }
            }
        }
        return false;
    }

    private static Name getLastHelper(List<Name> l) {
        return l.getLast();
    }

    private static Name removeLastHelper(List<Name> l) {
        return l.removeLast();
    }


    /**
     * Suppose a piece of documentation has a relative link.  When you copy
     * that documentation to another place such as the index or class-use page,
     * that relative link will no longer work.  We should redirect those links
     * so that they will work again.
     * <p>
     * Here is the algorithm used to fix the link:
     * <p>
     * {@literal <relative link> => docRoot + <relative path to file> + <relative link> }
     * <p>
     * For example, suppose DocletEnvironment has this link:
     * {@literal <a href="package-summary.html">The package Page</a> }
     * <p>
     * If this link appeared in the index, we would redirect
     * the link like this:
     *
     * {@literal <a href="./jdk/javadoc/doclet/package-summary.html">The package Page</a>}
     *
     * @param element the Element object whose documentation is being written.
     * @param tt the text being written.
     *
     * @return the text, with all the relative links redirected to work.
     */
    private String redirectRelativeLinks(Element element, TextTree tt) {
        String text = tt.getBody();
        if (!shouldRedirectRelativeLinks(element)) {
            return text;
        }
        String lower = Utils.toLowerCase(text);
        if (lower.startsWith("mailto:")
            || lower.startsWith("http:")
            || lower.startsWith("https:")
            || lower.startsWith("file:")
            || lower.startsWith("ftp:")) {
            return text;
        }
        if (text.startsWith("#")) {
            // Redirected fragment link: prepend HTML file name to make it work
            if (utils.isModule(element)) {
                text = "module-summary.html" + text;
            } else if (utils.isPackage(element)) {
                text = DocPaths.PACKAGE_SUMMARY.getPath() + text;
            } else {
                TypeElement typeElement = element instanceof TypeElement
                    ? (TypeElement) element : utils.getEnclosingTypeElement(element);
                text = docPaths.forName(typeElement).getPath() + text;
            }
        }

        if (!inSamePackage(element)) {
            DocPath redirectPathFromRoot = new SimpleElementVisitor14<DocPath, Void>() {
                @Override
                public DocPath visitType(TypeElement e, Void p) {
                    return docPaths.forPackage(utils.containingPackage(e));
                }

                @Override
                public DocPath visitPackage(PackageElement e, Void p) {
                    return docPaths.forPackage(e);
                }

                @Override
                public DocPath visitVariable(VariableElement e, Void p) {
                    return docPaths.forPackage(utils.containingPackage(e));
                }

                @Override
                public DocPath visitExecutable(ExecutableElement e, Void p) {
                    return docPaths.forPackage(utils.containingPackage(e));
                }

                @Override
                public DocPath visitModule(ModuleElement e, Void p) {
                    return DocPaths.forModule(e);
                }

                @Override
                protected DocPath defaultAction(Element e, Void p) {
                    return null;
                }
            }.visit(element);
            if (redirectPathFromRoot != null) {
                text = "{@" + (new DocRootTaglet()).getName() + "}/"
                    + redirectPathFromRoot.resolve(text).getPath();
                return replaceDocRootDir(text);
            }
        }
        return text;
    }
}
