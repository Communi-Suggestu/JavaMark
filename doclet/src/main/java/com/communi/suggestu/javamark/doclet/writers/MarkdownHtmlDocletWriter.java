package com.communi.suggestu.javamark.doclet.writers;

import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.StartElementTree;
import jdk.javadoc.internal.doclets.formats.html.HtmlConfiguration;
import jdk.javadoc.internal.doclets.formats.html.HtmlDocletWriter;
import jdk.javadoc.internal.doclets.formats.html.TagletWriterImpl;
import jdk.javadoc.internal.doclets.formats.html.markup.RawHtml;
import jdk.javadoc.internal.doclets.toolkit.Content;
import jdk.javadoc.internal.doclets.toolkit.util.DocPath;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class MarkdownHtmlDocletWriter extends HtmlDocletWriter
{
    /**
     * Creates an {@code HtmlDocletWriter}.
     *
     * @param configuration the configuration for this doclet
     * @param path          the file to be generated.
     */
    public MarkdownHtmlDocletWriter(final HtmlConfiguration configuration, final DocPath path)
    {
        super(configuration, path);
    }

    @Override
    public Content commentTagsToContent(final Element element, final List<? extends DocTree> trees, final boolean isFirstSentence, final boolean inSummary)
    {
        var content = super.commentTagsToContent(element, trees, isFirstSentence, inSummary);
        closeOpenDanglingContent(trees, content);
        return content;
    }

    private void closeOpenDanglingContent(final List<? extends DocTree> trees, Content target) {
        Stack<Name> openTags = new Stack<>();

        for (final DocTree tree : trees)
        {
            if (tree instanceof StartElementTree startElementTree) {
                openTags.push(startElementTree.getName());
            } else if (tree instanceof EndElementTree endElementTree) {
                var lastOpenedElement = openTags.pop();
                if (lastOpenedElement != endElementTree.getName())
                    throw new IllegalStateException("HTML Tree closing from an invalid opening!");
            }
        }

        while(!openTags.isEmpty()) {
            var openName = openTags.pop();
            target.add("\n");
            target.add(RawHtml.endElement(openName));
        }
    }
}
