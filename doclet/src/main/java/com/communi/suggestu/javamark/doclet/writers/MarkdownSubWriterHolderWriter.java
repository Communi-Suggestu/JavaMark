package com.communi.suggestu.javamark.doclet.writers;

import com.sun.source.doctree.DocTree;
import jdk.javadoc.internal.doclets.formats.html.HtmlConfiguration;
import jdk.javadoc.internal.doclets.formats.html.SubWriterHolderWriter;
import jdk.javadoc.internal.doclets.formats.html.markup.Entity;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTree;
import jdk.javadoc.internal.doclets.toolkit.Content;
import jdk.javadoc.internal.doclets.toolkit.util.DocPath;

import javax.lang.model.element.Element;
import java.util.List;

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
}
