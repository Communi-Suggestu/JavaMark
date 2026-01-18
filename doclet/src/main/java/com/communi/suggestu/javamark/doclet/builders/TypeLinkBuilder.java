package com.communi.suggestu.javamark.doclet.builders;

import com.communi.suggestu.javamark.doclet.utils.TypeLinkProvider;
import com.communi.suggestu.javamark.doclet.utils.TypeUniverse;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor14;
import java.util.List;

/**
 * Builds Markdown-compatible links between types using a TypeLinkProvider.
 */
public class TypeLinkBuilder
{

    private final TypeLinkProvider linkProvider;
    private final TypeUniverse     typeUniverse;
    private final TypeDisplayNameBuilder typeDisplayNameBuilder;

    public TypeLinkBuilder(TypeUniverse typeUniverse, TypeLinkProvider linkProvider)
    {
        this.linkProvider = linkProvider;
        this.typeUniverse = typeUniverse;
        this.typeDisplayNameBuilder = new TypeDisplayNameBuilder(typeUniverse, linkProvider);
    }

    private TypeLinkBuilder(final TypeUniverse typeUniverse, final TypeLinkProvider linkProvider, final TypeDisplayNameBuilder builder)
    {
        this.linkProvider = linkProvider;
        this.typeUniverse = typeUniverse;
        this.typeDisplayNameBuilder = builder;
    }

    /**
     * Sets the display mode for the link text.
     */
    public TypeLinkBuilder withDisplayMode(TypeDisplayNameBuilder.DisplayMode mode)
    {
        var builder = this.typeDisplayNameBuilder.withDisplayMode(mode);
        if (builder == this.typeDisplayNameBuilder)
            return this;

        return new TypeLinkBuilder(typeUniverse, linkProvider, builder);
    }

    /**
     * Builds a Markdown link from sourceType to targetType.
     * If no link can be generated, emits just the required name in the display mode.
     */
    public String build(Element sourceType, TypeMirror targetType)
    {
        String linkText = typeDisplayNameBuilder.build(targetType);
        String relPath = linkProvider.getRelativeLink(sourceType, targetType);
        if (relPath == null || sourceType.asType() == targetType)
        {
            return linkText;
        }
        return "[" + linkText + "](" + relPath + ")";
    }

    /**
     * Builds a Markdown link from sourceType to targetType.
     * If no link can be generated, emits just the required name in the display mode.
     */
    public String build(Element sourceType, Element targetType)
    {
        return build(sourceType, targetType.asType());
    }
}
