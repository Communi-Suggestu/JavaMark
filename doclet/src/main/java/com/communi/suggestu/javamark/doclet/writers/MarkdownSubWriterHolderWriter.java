package com.communi.suggestu.javamark.doclet.writers;

import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.DocTreeVisitor;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.StartElementTree;
import jdk.javadoc.internal.doclets.formats.html.HtmlConfiguration;
import jdk.javadoc.internal.doclets.formats.html.SubWriterHolderWriter;
import jdk.javadoc.internal.doclets.formats.html.markup.Entity;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTree;
import jdk.javadoc.internal.doclets.formats.html.markup.RawHtml;
import jdk.javadoc.internal.doclets.toolkit.Content;
import jdk.javadoc.internal.doclets.toolkit.util.DocPath;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class MarkdownSubWriterHolderWriter extends SubWriterHolderWriter
{
    public MarkdownSubWriterHolderWriter(final HtmlConfiguration configuration, final DocPath filename)
    {
        super(configuration, filename);
    }

    @Override
    public void addInlineComment(final Element element, final Content target)
    {
        addCommentTags(element, utils.getFullBody(element), false, false, false, target);
    }

    /**
     * Adds the comment tags.
     *
     * @param element the Element for which the comment tags will be generated
     * @param tags the first sentence tags for the doc
     * @param depr true if it is deprecated
     * @param first true if the first sentence tags should be added
     * @param inSummary true if the comment tags are added into the summary section
     * @param target the content to which the comment tags will be added
     */
    private void addCommentTags(Element element, List<? extends DocTree> tags, boolean depr,
        boolean first, boolean inSummary, Content target) {
        tags = closeDanglingParagraphs(tags);

        if (options.noComment()) {
            return;
        }
        Content result = commentTagsToContent(element, tags, first, inSummary);
        if (!result.isEmpty()) {
            target.add(result);
        }
        if (tags.isEmpty()) {
            target.add(Entity.NO_BREAK_SPACE);
        }
    }

    private List<DocTree> closeDanglingParagraphs(List<? extends DocTree> tags) {
        var result = new ArrayList<DocTree>();
        var tagStack = new Stack<Name>();
        for (final DocTree tag : tags)
        {
            if (tag instanceof StartElementTree startElementTree) {
                if (!tagStack.isEmpty())
                {
                    var lastTag = tagStack.peek();
                    if (Objects.equals(lastTag.toString(), "p"))
                    {
                        //Last tag was a paragraph that was not closed.
                        result.add(new EndDocElement(lastTag));
                        tagStack.pop();
                    }
                }

                tagStack.add(startElementTree.getName());
            } else if (tag instanceof EndElementTree endElementTree) {
                if (tagStack.isEmpty())
                    throw new IllegalStateException("Closing tag without opening!");

                var previousOpen = tagStack.pop();
                if (!previousOpen.equals(endElementTree.getName()))
                    throw new IllegalStateException("Tag: " + previousOpen + " but the next closing tag was: " + endElementTree.getName());
            }

            result.add(tag);
        }

        while(!tagStack.isEmpty()) {
            result.add(new EndDocElement(tagStack.pop()));
        }

        return result;
    }

    private record EndDocElement(Name name) implements EndElementTree
    {
        @Override
            public Name getName()
            {
                return name;
            }

            @Override
            public Kind getKind()
            {
                return Kind.END_ELEMENT;
            }

            @Override
            public <R, D> R accept(final DocTreeVisitor<R, D> visitor, final D data)
            {
                return visitor.visitEndElement(this, data);
            }
        }
}
