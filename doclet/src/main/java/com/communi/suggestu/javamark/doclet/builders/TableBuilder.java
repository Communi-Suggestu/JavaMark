package com.communi.suggestu.javamark.doclet.builders;

import java.util.List;
import java.util.function.Function;

public interface TableBuilder {
    TableBuilder withHeaders(List<String> headers);
    TableBuilder withHeaders(String... headers);
    TableBuilder addRow(List<String> cells);
    TableBuilder addRow(String... cells);
    TableBuilder addRow(Function<List<String>, List<String>> rowSupplier);
    String build();
}

