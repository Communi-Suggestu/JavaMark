package com.communi.suggestu.javamark.example.inner;

/**
 * Represents the size information of the palette.
 * <p>
 * The size is only accurate if it has changed.
 *
 * @param hasChanged Whether the size has changed.
 * @param size       The size of the palette, 0 if not changed.
 */
public record SizeInformation(
    boolean hasChanged,
    int size)
{
    public static SizeInformation notChanged()
    {
        return new SizeInformation(false, 0);
    }

    public static SizeInformation changed(int size)
    {
        return new SizeInformation(true, size);
    }
}