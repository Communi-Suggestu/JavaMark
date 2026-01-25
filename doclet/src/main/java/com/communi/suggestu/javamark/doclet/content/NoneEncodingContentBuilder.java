package com.communi.suggestu.javamark.doclet.content;

import jdk.javadoc.internal.doclets.toolkit.Content;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NoneEncodingContentBuilder extends Content
{
    protected List<Content> contents = List.of();

    public NoneEncodingContentBuilder() { }

    @Override
    public NoneEncodingContentBuilder add(Content content) {
        Objects.requireNonNull(content);
        ensureMutableContents();
        contents.add(content);
        return this;
    }

    @Override
    public NoneEncodingContentBuilder add(CharSequence text) {
        if (text.length() > 0) {
            ensureMutableContents();
            Content c = contents.isEmpty() ? null : contents.get(contents.size() - 1);
            NoneEncodingTextBuilder tb;
            if (c instanceof NoneEncodingTextBuilder tbi) {
                tb = tbi;
            } else {
                contents.add(tb = new NoneEncodingTextBuilder());
            }
            tb.add(text);
        }
        return this;
    }

    @Override
    public boolean isEmpty() {
        for (Content content: contents) {
            if (!content.isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public int charCount() {
        int n = 0;
        for (Content c : contents)
            n += c.charCount();
        return n;
    }

    private void ensureMutableContents() {
        if (contents.isEmpty())
            contents = new ArrayList<>();
    }

    @Override
    public boolean write(final Writer out, final String newline, boolean atNewline) throws IOException
    {
        for (Content content: contents) {
            if (!content.isEmpty())
            {
                atNewline = content.write(out, newline, atNewline);
                if (!atNewline)
                {
                    out.write(newline);
                    atNewline = true;
                }
            }
        }
        return atNewline;
    }
}
