package com.communi.suggestu.javamark.doclet.utils;

import jdk.javadoc.internal.doclets.formats.html.TableHeader;
import jdk.javadoc.internal.doclets.toolkit.Content;

import java.lang.reflect.Field;
import java.util.List;

public class TableHeaderUtils
{
    /**
     * Extracts the cellContents field from a TableHeader instance using reflection.
     * @param tableHeader the TableHeader instance
     * @return the List of cell contents, or null if extraction fails
     */
    @SuppressWarnings("unchecked")
    public static List<Content> extractCellContents(TableHeader tableHeader) {
        try {
            Class<?> clazz = tableHeader.getClass();
            Field cellContentsField = clazz.getDeclaredField("cellContents");
            cellContentsField.setAccessible(true);
            return (List<Content>) cellContentsField.get(tableHeader);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract cellContents from TableHeader", e);
        }
    }
}
