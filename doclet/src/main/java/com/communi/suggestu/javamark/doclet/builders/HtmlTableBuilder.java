package com.communi.suggestu.javamark.doclet.builders;

import java.util.List;
import java.util.function.Function;

/**
 * Builder for emitting HTML-compatible tables.
 */
public class HtmlTableBuilder implements TableBuilder {
    private List<String> headers;
    private final StringBuilder rows = new StringBuilder();

    @Override
    public HtmlTableBuilder withHeaders(List<String> headers) {
        this.headers = headers;
        return this;
    }

    @Override
    public HtmlTableBuilder withHeaders(String... headers) {
        this.headers = java.util.Arrays.asList(headers);
        return this;
    }

    @Override
    public HtmlTableBuilder addRow(List<String> cells) {
        if (headers == null || cells.size() != headers.size()) {
            throw new IllegalArgumentException("Row size must match header size");
        }
        rows.append("  <tr>");
        for (String cell : cells) {
            String cellContent = cell == null ? "" : cell.replace("\n", "<br>");
            rows.append("<td>").append(cellContent).append("</td>");
        }
        rows.append("</tr>\n");
        return this;
    }

    @Override
    public HtmlTableBuilder addRow(String... cells) {
        return addRow(java.util.Arrays.asList(cells));
    }

    @Override
    public HtmlTableBuilder addRow(Function<List<String>, List<String>> rowSupplier) {
        List<String> row = rowSupplier.apply(headers);
        return addRow(row);
    }

    @Override
    public String build() {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalStateException("Headers must be set before building the table");
        }
        StringBuilder table = new StringBuilder();
        table.append("<table>\n  <thead>\n    <tr>");
        for (String header : headers) {
            table.append("<th>").append(header).append("</th>");
        }
        table.append("</tr>\n  </thead>\n  <tbody>\n");
        table.append(rows);
        table.append("  </tbody>\n</table>\n");
        return table.toString();
    }
}

