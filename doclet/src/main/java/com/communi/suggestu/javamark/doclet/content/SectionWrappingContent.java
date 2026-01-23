package com.communi.suggestu.javamark.doclet.content;

import jdk.javadoc.internal.doclets.formats.html.markup.HtmlId;
import jdk.javadoc.internal.doclets.toolkit.Content;

import java.io.IOException;
import java.io.Writer;

public class SectionWrappingContent extends Content
{

    private final HtmlId anchor;
    private final Content content;

    public SectionWrappingContent(final HtmlId anchor, final Content content) {
        this.anchor = anchor;
        this.content = content;
    }

    @Override
    public boolean write(final Writer writer, final String newline, boolean atNewline) throws IOException
    {
        if (!atNewline)
            writer.write(newline);

        atNewline = true;

        writer.write("<section id=\"%s\">".formatted(anchor.name()));
        writer.write(newline);
        writer.write(newline);

        atNewline = content.write(writer, newline, atNewline);

        if (!atNewline)
            writer.write(newline);

        writer.write(newline);
        writer.write("</section>");
        writer.write(newline);
        writer.write(newline);

        return true;
    }

    @Override
    public boolean isEmpty()
    {
        return content.isEmpty();
    }
}
