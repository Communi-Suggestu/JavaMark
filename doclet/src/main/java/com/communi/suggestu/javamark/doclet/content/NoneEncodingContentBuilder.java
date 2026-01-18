package com.communi.suggestu.javamark.doclet.content;

import com.communi.suggestu.javamark.doclet.utils.Constants;
import jdk.javadoc.internal.doclets.formats.html.markup.TextBuilder;
import jdk.javadoc.internal.doclets.toolkit.Content;

import java.io.IOException;
import java.io.Writer;

public class NoneEncodingContentBuilder extends TextBuilder
{
    @Override
    public Content add(final Content content)
    {
        return super.add(content.toString());
    }

    @Override
    public boolean write(final Writer out, final String newline, final boolean atNewline) throws IOException
    {
        String s = toString();
        out.write(s.replace("\n", newline));
        return s.endsWith("\n");
    }
}
