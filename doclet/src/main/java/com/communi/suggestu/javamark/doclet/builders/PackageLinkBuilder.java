package com.communi.suggestu.javamark.doclet.builders;

import com.communi.suggestu.javamark.doclet.utils.PackageLinkProvider;

import javax.lang.model.element.PackageElement;

/**
 * Builds Markdown-compatible links between packages using a PackageLinkProvider.
 */
public class PackageLinkBuilder {
    public enum DisplayMode {
        FULLY_QUALIFIED_NAME,
        SIMPLE_NAME
    }

    private final PackageLinkProvider linkProvider;
    private       DisplayMode         displayMode = DisplayMode.SIMPLE_NAME;

    public PackageLinkBuilder(PackageLinkProvider linkProvider) {
        this.linkProvider = linkProvider;
    }

    private PackageLinkBuilder(final PackageLinkProvider linkProvider, final DisplayMode displayMode)
    {
        this.linkProvider = linkProvider;
        this.displayMode = displayMode;
    }

    /**
     * Sets the display mode for the link text.
     */
    public PackageLinkBuilder withDisplayMode(DisplayMode mode) {
        if (mode == this.displayMode)
            return this;

        return new PackageLinkBuilder(
            this.linkProvider,
            mode
        );
    }

    /**
     * Builds a Markdown link from sourcePackage to targetPackage.
     * If no link can be generated, emits just the required name in the display mode.
     */
    public String build(PackageElement sourcePackage, PackageElement targetPackage) {
        String linkText = displayMode == DisplayMode.FULLY_QUALIFIED_NAME
                ? targetPackage.getQualifiedName().toString()
                : targetPackage.getSimpleName().toString();
        String relPath = linkProvider.getRelativeLink(sourcePackage, targetPackage);
        if (relPath == null)
            return linkText;
        return "[" + linkText + "](" + relPath + ")";
    }
}

