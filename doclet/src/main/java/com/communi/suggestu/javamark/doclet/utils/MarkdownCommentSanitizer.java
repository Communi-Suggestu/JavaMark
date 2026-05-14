package com.communi.suggestu.javamark.doclet.utils;

import java.util.ArrayList;
import java.util.List;

public final class MarkdownCommentSanitizer
{
    private MarkdownCommentSanitizer()
    {
        throw new IllegalStateException("Can not instantiate utility class");
    }

    public static String stripBlankLinesInsideDivBlocks(final String input)
    {
        final List<String> outputLines = new ArrayList<>();
        boolean inFencedCodeBlock = false;

        final String[] lines = input.split("\\n", -1);
        for (final String line : lines)
        {
            final String trimmed = line.trim();
            if (trimmed.startsWith("```") || trimmed.startsWith("~~~"))
            {
                inFencedCodeBlock = !inFencedCodeBlock;
            }

            if (!(line.isBlank() && !inFencedCodeBlock))
            {
                outputLines.add(line);
            }
        }

        return String.join("\n", outputLines);
    }
}

