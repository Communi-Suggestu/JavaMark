package com.communi.suggestu.javamark.doclet.content;

import jdk.javadoc.internal.doclets.toolkit.Content;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Pattern;

public class ContainerContent extends Content
{
    private static final Pattern DEPTH_CHECK = Pattern.compile("<!-- JavaMarkContainer Depth: (?<depth>[0-9]+) -->");

    @Override
    public boolean write(final Writer writer, final String newline, boolean atNewline) throws IOException
    {
        if (content.isEmpty())
        {
            return atNewline;
        }

        var depth = determineDepth();
        var marker = ":::" + String.join("", Collections.nCopies(depth, "::"));
        writer.write("<!-- JavaMarkContainer Depth: %s -->".formatted(depth));
        writer.write(newline);
        writer.write(marker + " " + type.name().toLowerCase(Locale.ROOT));
        atNewline = false;
        if (this.header != null)
        {
            writer.write(" ");
            atNewline = this.header.write(writer, newline, atNewline);
            if (!atNewline)
            {
                writer.write(newline);
                atNewline = true;
            }
        }
        else
        {
            writer.write(newline);
            atNewline = true;
        }

        atNewline = content.write(writer, newline, atNewline);

        if (!atNewline)
        {
            writer.write(newline);
        }

        writer.write(marker);
        writer.write(newline);
        return true;
    }

    private int determineDepth() {
        var content = this.content.toString();
        var match = DEPTH_CHECK.matcher(content);

        if (!match.find())
            return 1;

        int currentMax = -1;
        while(match.hasMatch()) {
            var groupDepthString = match.group("depth");
            var depth = Integer.parseInt(groupDepthString);

            currentMax = Math.max(currentMax, depth);

            if (!match.find()) {
                break;
            }
        }

        if (currentMax == -1)
            throw new IllegalStateException("Could not find the depth marker!");

        return currentMax + 1;
    }

    @Override
    public boolean isEmpty()
    {
        return content.isEmpty();
    }

    public enum Type
    {
        INFO,
        TIP,
        WARNING,
        DANGER,
        DETAILS;
    }

    private final Content content;
    private final Content header;
    private final Type    type;

    public ContainerContent(final Content content, final Type type)
    {
        this.content = content;
        this.header = null;
        this.type = type;
    }

    public ContainerContent(final Content content, final Content header, final Type type)
    {
        this.content = content;
        this.header = header;
        this.type = type;
    }
}
