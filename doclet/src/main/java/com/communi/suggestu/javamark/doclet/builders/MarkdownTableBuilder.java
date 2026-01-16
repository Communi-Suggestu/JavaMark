package com.communi.suggestu.javamark.doclet.builders;

import java.util.List;
import java.util.function.Function;

/**
 * Builder for emitting Markdown-compatible tables.
 */
public class MarkdownTableBuilder implements TableBuilder {
    private List<String> headers;
    private final StringBuilder rows = new StringBuilder();

    /**
     * Sets the table headers from a List.
     */
    @Override
    public MarkdownTableBuilder withHeaders(List<String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Sets the table headers from an array of Strings.
     */
    @Override
    public MarkdownTableBuilder withHeaders(String... headers) {
        this.headers = java.util.Arrays.asList(headers);
        return this;
    }

    /**
     * Adds a row to the table from a List.
     */
    @Override
    public MarkdownTableBuilder addRow(List<String> cells) {
        if (headers == null || cells.size() != headers.size()) {
            throw new IllegalArgumentException("Row size must match header size");
        }
        // Multiline cell support: replace newlines with <br> for markdown tables
        rows.append("|");
        for (String cell : cells) {
            String cellContent = cell == null ? "" : cell.replace("\n", "<br>");
            rows.append(cellContent).append("|");
        }
        rows.append("\n");
        return this;
    }

    /**
     * Adds a row to the table from an array of Strings.
     */
    @Override
    public MarkdownTableBuilder addRow(String... cells) {
        return addRow(java.util.Arrays.asList(cells));
    }

    /**
     * Adds a row to the table using a functional API (e.g., via a lambda).
     */
    @Override
    public MarkdownTableBuilder addRow(Function<List<String>, List<String>> rowSupplier) {
        List<String> row = rowSupplier.apply(headers);
        return addRow(row);
    }

    /**
     * Builds the Markdown table as a string.
     */
    public String build() {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalStateException("Headers must be set before building the table");
        }
        StringBuilder table = new StringBuilder();
        // Header row
        table.append("|");
        for (String header : headers) {
            table.append(header).append("|");
        }
        table.append("\n|");
        // Separator row
        table.append("---|".repeat(headers.size()));
        table.append("\n");
        // Data rows
        table.append(rows);
        return table.toString();
    }
}
