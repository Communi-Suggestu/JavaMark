package com.communi.suggestu.javamark.doclet.content;

import jdk.javadoc.internal.doclets.toolkit.Content;

import java.io.IOException;
import java.io.Writer;

public class ContentWrapper extends Content
{

    private final Content target;
    private final Content wrapper;

    public ContentWrapper(final Content target, final Content wrapper) {
        this.target = target;
        this.wrapper = wrapper;
    }

    @Override
    public boolean write(final Writer writer, final String newline, final boolean atNewline) throws IOException
    {
        return wrapper.write(writer, newline, atNewline);
    }

    @Override
    public boolean isEmpty()
    {
        return wrapper.isEmpty();
    }

    @Override
    public Content add(final Content content)
    {
        return target.add(content);
    }

    @Override
    public Content add(final CharSequence stringContent)
    {
        return target.add(stringContent);
    }

    public Content getWrapper()
    {
        return wrapper;
    }
}
