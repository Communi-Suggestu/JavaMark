package com.communi.suggestu.javamark.doclet.builders;

import com.communi.suggestu.javamark.doclet.utils.TypeLinkProvider;
import com.communi.suggestu.javamark.doclet.utils.TypeUniverse;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * Builds Markdown-compatible links between types using a TypeLinkProvider.
 */
public class TypeLinkBuilder {
    public enum DisplayMode {
        FULLY_QUALIFIED_NAME,
        SIMPLE_NAME
    }

    private final TypeLinkProvider linkProvider;
    private final TypeUniverse     typeUniverse;
    private       DisplayMode      displayMode = DisplayMode.SIMPLE_NAME;

    public TypeLinkBuilder(TypeLinkProvider linkProvider, TypeUniverse typeUniverse) {
        this.linkProvider = linkProvider;
        this.typeUniverse = typeUniverse;
    }

    private TypeLinkBuilder(final TypeLinkProvider linkProvider, final TypeUniverse typeUniverse, final DisplayMode displayMode)
    {
        this.linkProvider = linkProvider;
        this.typeUniverse = typeUniverse;
        this.displayMode = displayMode;
    }

    /**
     * Sets the display mode for the link text.
     */
    public TypeLinkBuilder withDisplayMode(DisplayMode mode) {
        if (mode == this.displayMode)
            return this;

        return new TypeLinkBuilder(
            linkProvider,
            typeUniverse,
            mode
        );
    }

    /**
     * Builds a Markdown link from sourceType to targetType.
     * If no link can be generated, emits just the required name in the display mode.
     */
    public String build(Element sourceType, TypeMirror targetType) {
        String linkText = displayMode == DisplayMode.FULLY_QUALIFIED_NAME
                ? typeUniverse.asTypeElement(targetType).getQualifiedName().toString()
                : typeUniverse.asTypeElement(targetType).getSimpleName().toString();
        String relPath = linkProvider.getRelativeLink(sourceType, targetType);
        if (relPath == null || sourceType == targetType)
            return linkText;
        return "[" + linkText + "](" + relPath + ")";
    }
}
