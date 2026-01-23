package com.communi.suggestu.javamark.doclet.content;

import com.communi.suggestu.javamark.doclet.builders.VitepressTabbedEnvironmentBuilder;
import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlId;
import jdk.javadoc.internal.doclets.toolkit.Content;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class VitepressTableContent extends Content
{

    private       String                      key  = null;
    private final Map<Content, ContentBuilder> tabs = new LinkedHashMap<>();

    /**
     * Sets the key for the tabbed environment (optional).
     */
    public VitepressTableContent withKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * Sets the key for the tabbed environment (optional).
     */
    public VitepressTableContent withKey(HtmlId key) {
        this.key = key.name();
        return this;
    }

    /**
     * Adds a tab with the given name and content.
     */
    public VitepressTableContent addTab(Content tabName, Content content) {
        tabs.computeIfAbsent(tabName, k -> new ContentBuilder()).add(content);
        return this;
    }

    /**
     * Adds a tab with the given name and content supplied by a Consumer<StringBuilder>.
     */
    public VitepressTableContent addTab(Content tabName, Consumer<Content> contentBuilder) {
        ContentBuilder cb = tabs.computeIfAbsent(tabName, k -> new ContentBuilder());
        contentBuilder.accept(cb);
        return this;
    }

    @Override
    public boolean write(final Writer writer, final String newline, final boolean atNewline) throws IOException
    {
        var builder = new VitepressTabbedEnvironmentBuilder()
            .withKey(key);

        tabs.forEach((name, content) -> {
            builder.addTab(name.toString(), content.toString());
        });

        writer.write(builder.build());
        writer.write(newline);
        return atNewline;
    }

    @Override
    public boolean isEmpty()
    {
        return tabs.isEmpty();
    }
}
