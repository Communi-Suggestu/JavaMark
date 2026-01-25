package com.communi.suggestu.javamark.doclet.content;

import jdk.javadoc.internal.doclets.toolkit.Content;

import java.io.IOException;
import java.io.Writer;

/**
 * Class for generating string content for HTML tags of javadoc output.
 * The content is mutable to the extent that additional content may be added.
 * Newlines are always represented by {@code \n}.
 */
public class NoneEncodingTextBuilder extends Content
{

    private final StringBuilder stringBuilder;

    /**
     * Constructor to construct StringContent object.
     */
    public NoneEncodingTextBuilder() {
        stringBuilder = new StringBuilder();
    }

    /**
     * Constructor to construct StringContent object with some initial content.
     *
     * @param initialContent initial content for the object
     */
    public NoneEncodingTextBuilder(CharSequence initialContent) {
        assert checkNewlines(initialContent);
        stringBuilder = new StringBuilder(initialContent);
    }

    /**
     * Check for the absence of {@code \r} characters.
     * @param cs the characters to be checked
     * @return {@code true} if there are no {@code \r} characters, and {@code false} otherwise
     */
    static boolean checkNewlines(CharSequence cs) {
        return !cs.toString().contains("\r");
    }

    /**
     * Adds content for the StringContent object.
     *
     * @param strContent string content to be added
     */
    @Override
    public NoneEncodingTextBuilder add(CharSequence strContent) {
        assert checkNewlines(strContent);
        stringBuilder.append(strContent);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return (stringBuilder.isEmpty());
    }

    @Override
    public int charCount() {
        return stringBuilder.length();
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }

    @Override
    public boolean write(Writer out, String newline, boolean atNewline) throws IOException
    {
        String s = stringBuilder.toString();
        out.write(s.replace("\n", newline));
        return s.endsWith("\n");
    }
}