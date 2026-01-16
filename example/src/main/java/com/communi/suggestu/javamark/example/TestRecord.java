package com.communi.suggestu.javamark.example;

import java.util.List;

/**
 * Testable record with value.
 * <p>
 *     With some special code in paragraphs
 * </p>
 * <pre>
 * {@code
 * Set<String> s;
 * System.out.println(s);
 * }
 * </pre>
 * @param value The value
 */
public record TestRecord(List<String> value)
{
}
