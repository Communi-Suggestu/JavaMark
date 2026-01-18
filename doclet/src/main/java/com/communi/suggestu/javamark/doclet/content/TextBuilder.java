/*
 * Copyright (c) 2010, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.communi.suggestu.javamark.doclet.content;

import java.io.IOException;
import java.io.Writer;

/**
 * Class for generating string content for HTML tags of javadoc output.
 * The content is mutable to the extent that additional content may be added.
 * Newlines are always represented by {@code \n}.
 * Any special HTML characters will be escaped if and when the content is written out.
 */
public class TextBuilder extends Content
{

    private final StringBuilder stringBuilder;

    /**
     * Constructor to construct StringContent object.
     */
    public TextBuilder() {
        stringBuilder = new StringBuilder();
    }

    /**
     * Constructor to construct StringContent object with some initial content.
     *
     * @param initialContent initial content for the object
     */
    public TextBuilder(CharSequence initialContent) {
        assert !initialContent.toString().contains("\r");
        stringBuilder = new StringBuilder(initialContent);
    }

    /**
     * Adds content for the StringContent object.
     *
     * @param strContent string content to be added
     */
    @Override
    public TextBuilder add(CharSequence strContent) {
        assert !strContent.toString().contains("\r");;
        stringBuilder.append(strContent);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return (stringBuilder.length() == 0);
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
    public boolean write(Writer out, String newline, boolean atNewline) throws IOException {
        String s = escapeHtmlChars(stringBuilder);
        out.write(s.replace("\n", newline));
        return s.endsWith("\n");
    }

    static String escapeHtmlChars(CharSequence s) {
        // Convert to string as CharSequence implementations can be slow - see JDK-8263321
        String str = s.toString();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            switch (ch) {
                // only start building a new string if we need to
                case '<', '>', '&' -> {
                    StringBuilder sb = new StringBuilder(str.substring(0, i));
                    escapeHtmlChars(str, i, sb);
                    return sb.toString();
                }
            }
        }
        return str;
    }
}
