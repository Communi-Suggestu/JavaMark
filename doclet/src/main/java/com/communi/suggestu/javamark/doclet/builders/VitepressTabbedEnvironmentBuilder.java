package com.communi.suggestu.javamark.doclet.builders;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Builder for emitting Vitepress tabbed environments in Markdown.
 * Example output:
 * :::tabs key:ab
 * == tab a
 * a content 2
 * == tab b
 * b content 2
 * :::
 */
public class VitepressTabbedEnvironmentBuilder {
    private String key = null;
    private final Map<String, StringBuilder> tabs = new LinkedHashMap<>();

    /**
     * Sets the key for the tabbed environment (optional).
     */
    public VitepressTabbedEnvironmentBuilder withKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * Adds a tab with the given name and content.
     */
    public VitepressTabbedEnvironmentBuilder addTab(String tabName, String content) {
        tabs.computeIfAbsent(tabName, k -> new StringBuilder()).append(content);
        return this;
    }

    /**
     * Adds a tab with the given name and content supplied by a Consumer<StringBuilder>.
     */
    public VitepressTabbedEnvironmentBuilder addTab(String tabName, Consumer<StringBuilder> contentBuilder) {
        StringBuilder sb = tabs.computeIfAbsent(tabName, k -> new StringBuilder());
        contentBuilder.accept(sb);
        return this;
    }

    /**
     * Builds the tabbed environment as a Markdown string.
     */
    public String build() {
        StringBuilder out = new StringBuilder();
        out.append(":::tabs");
        if (key != null && !key.isEmpty()) {
            out.append(" key:").append(key);
        }
        out.append("\n");
        for (Map.Entry<String, StringBuilder> entry : tabs.entrySet()) {
            out.append("== ").append(entry.getKey()).append("\n");
            out.append(entry.getValue());
            if (!entry.getValue().toString().endsWith("\n")) {
                out.append("\n");
            }
        }
        out.append(":::\n");
        return out.toString();
    }
}

