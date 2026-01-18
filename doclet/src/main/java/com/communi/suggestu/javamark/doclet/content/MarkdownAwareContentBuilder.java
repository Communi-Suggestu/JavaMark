package com.communi.suggestu.javamark.doclet.content;

import com.communi.suggestu.javamark.doclet.utils.Constants;
import jdk.javadoc.internal.doclets.formats.html.markup.Entity;
import jdk.javadoc.internal.doclets.formats.html.markup.TextBuilder;
import jdk.javadoc.internal.doclets.toolkit.Content;

import java.io.IOException;
import java.io.Writer;

public class MarkdownAwareContentBuilder extends TextBuilder
{
    @Override
    public Content add(final Content content)
    {
        return super.add(processHtmlContent(content));
    }

    private static String processHtmlContent(final Content content)
    {
        var stringified = content.toString();
        return String.join(Constants.HTML_NEW_LINE, stringified.split("\n"));
    }

    @Override
    public boolean write(final Writer out, final String newline, final boolean atNewline) throws IOException
    {
        String s = toString();
        out.write(s.replace("\n", newline));
        return s.endsWith("\n");
    }
}
