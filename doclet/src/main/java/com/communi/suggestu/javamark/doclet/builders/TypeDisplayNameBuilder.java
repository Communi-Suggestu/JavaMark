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
import java.util.ArrayList;
import java.util.List;

public class TypeDisplayNameBuilder
{
    public enum DisplayMode
    {
        FULLY_QUALIFIED_NAME,
        FULLY_QUALIDIED_JAVADOC_NAME,
        JAVADOC,
        SIMPLE_NAME
    }

    private final TypeLinkProvider linkProvider;
    private final TypeUniverse     typeUniverse;
    private       DisplayMode      displayMode = DisplayMode.JAVADOC;

    public TypeDisplayNameBuilder(TypeUniverse typeUniverse, TypeLinkProvider linkProvider)
    {
        this.linkProvider = linkProvider;
        this.typeUniverse = typeUniverse;
    }

    private TypeDisplayNameBuilder(final TypeUniverse typeUniverse, final TypeLinkProvider linkProvider, final DisplayMode displayMode)
    {
        this.linkProvider = linkProvider;
        this.typeUniverse = typeUniverse;
        this.displayMode = displayMode;
    }

    /**
     * Sets the display mode for the link text.
     */
    public TypeDisplayNameBuilder withDisplayMode(DisplayMode mode)
    {
        if (mode == this.displayMode)
        {
            return this;
        }

        return new TypeDisplayNameBuilder(
            typeUniverse, linkProvider,
            mode
        );
    }

    /**
     * Builds a Markdown link from sourceType to targetType.
     * If no link can be generated, emits just the required name in the display mode.
     */
    public String build(TypeMirror targetType)
    {
        return createLinkText(targetType);
    }

    private String createLinkText(TypeMirror target)
    {
        return switch (displayMode)
        {
            case FULLY_QUALIFIED_NAME -> typeUniverse.asTypeElement(target).getQualifiedName().toString();
            case FULLY_QUALIDIED_JAVADOC_NAME -> createFullyQualifiedJavadocName(target);
            case JAVADOC -> createJavadocLinkText(target);
            case SIMPLE_NAME -> typeUniverse.asTypeElement(target).getSimpleName().toString();
        };
    }

    private String createFullyQualifiedJavadocName(TypeMirror target) {
        var pkg = typeUniverse.getPackageOf(target);
        var resultBuilder = new StringBuilder();
        if (pkg != null) {
            resultBuilder.append(pkg.getQualifiedName()).append(".");
        }

        resultBuilder.append(createJavadocLinkText(target));

        return resultBuilder.toString();
    }

    private String createJavadocLinkText(TypeMirror target)
    {
        SimpleTypeVisitor14<StringBuilder, Void> linkVisitor = new SimpleTypeVisitor14<>()
        {

            final StringBuilder link = new StringBuilder();

            // handles primitives, no types and error types
            @Override
            protected StringBuilder defaultAction(TypeMirror type, Void linkInfo)
            {
                link.append(typeUniverse.getTypeName(type, false));
                return link;
            }

            @Override
            public StringBuilder visitWildcard(WildcardType type, Void linkInfo)
            {
                link.append(getTypeAnnotationLinks(type));
                link.append("?");
                TypeMirror extendsBound = type.getExtendsBound();
                if (extendsBound != null)
                {
                    link.append(" extends ");
                    link.append(createLinkText(extendsBound));
                }
                TypeMirror superBound = type.getSuperBound();
                if (superBound != null)
                {
                    link.append(" super ");
                    link.append(createLinkText(superBound));
                }
                return link;
            }

            @Override
            public StringBuilder visitTypeVariable(TypeVariable type, Void linkInfo)
            {
                link.append(getTypeAnnotationLinks(type));
                TypeVariable typevariable = (typeUniverse.isArrayType(type))
                    ? (TypeVariable) typeUniverse.getComponentType(type)
                    : type;
                link.append(typeUniverse.getTypeName(typevariable, false));

                TypeParameterElement tpe = ((TypeParameterElement) typevariable.asElement());
                boolean more = false;
                List<? extends TypeMirror> bounds = typeUniverse.getBounds(tpe);
                for (TypeMirror bound : bounds)
                {
                    // we get everything as extends java.lang.Object we suppress
                    // all of them except those that have multiple extends
                    if (bounds.size() == 1 &&
                        typeUniverse.isSameType(bound, typeUniverse.getObjectType()) &&
                        !typeUniverse.isAnnotated(bound))
                    {
                        continue;
                    }
                    link.append(more ? " & " : " extends ");
                    link.append(createLinkText(bound));
                    more = true;
                }

                return link;
            }

            @Override
            public StringBuilder visitDeclared(DeclaredType type, Void linkInfo)
            {
                TypeMirror enc = type.getEnclosingType();
                if (enc instanceof DeclaredType dt && typeUniverse.isGenericType(dt))
                {
                    // If an enclosing type has type parameters render them as separate links as
                    // otherwise this information is lost. On the other hand, plain enclosing types
                    // are not linked separately as they are easy to reach from the nested type.
                    visitDeclared(dt, linkInfo);
                    link.append(".");
                }
                link.append(getTypeAnnotationLinks(type));
                link.append(typeUniverse.asTypeElement(type).getSimpleName());
                link.append(getTypeParameterLinks(type));
                return link;
            }
        };
        return linkVisitor.visit(target).toString();
    }

    /**
     * Returns links to the type annotations.
     *
     * @param linkInfo the information about the link to construct
     * @return the links to the type annotations
     */
    private String getTypeAnnotationLinks(TypeMirror linkInfo)
    {
        StringBuilder links = new StringBuilder();
        List<? extends AnnotationMirror> annotations;
        if (typeUniverse.isAnnotated(linkInfo))
        {
            annotations = linkInfo.getAnnotationMirrors();
        }
        else if (typeUniverse.isTypeVariable(linkInfo))
        {
            Element element = typeUniverse.asElement(linkInfo);
            annotations = element.getAnnotationMirrors();
        }
        else
        {
            return links.toString();
        }

        if (annotations.isEmpty())
        {
            return links.toString();
        }

        return new AnnotationBuilder(
            typeUniverse,
            linkProvider
        ).from(annotations)
            .build();
    }


    /**
     * Returns links to the type parameters.
     *
     * @param linkInfo the information about the link to construct
     * @return the links to the type parameters
     */
    protected String getTypeParameterLinks(TypeMirror linkInfo) {
        StringBuilder links = new StringBuilder();
        List<TypeMirror> vars = new ArrayList<>();
        TypeMirror ctype = typeUniverse.getComponentType(linkInfo);
        if (linkInfo != null && typeUniverse.isDeclaredType(linkInfo)) {
            vars.addAll(((DeclaredType) linkInfo).getTypeArguments());
        } else if (ctype != null && typeUniverse.isDeclaredType(ctype)) {
            vars.addAll(((DeclaredType) ctype).getTypeArguments());
        } else if (ctype == null && typeUniverse.asTypeElement(linkInfo) != null) {
            typeUniverse.asTypeElement(linkInfo).getTypeParameters().forEach(t -> vars.add(t.asType()));
        } else {
            // Nothing to document.
            return links.toString();
        }
        if (!vars.isEmpty()) {
            links.append("\\<");
            boolean many = false;
            for (TypeMirror t : vars) {
                if (many) {
                    links.append(",");
                }
                links.append(new TypeDisplayNameBuilder(typeUniverse, linkProvider, DisplayMode.JAVADOC).build(t));
                many = true;
            }
            links.append("\\>");
        }
        return links.toString();
    }

    /**
     * Builds a Markdown link from sourceType to targetType.
     * If no link can be generated, emits just the required name in the display mode.
     */
    public String build(Element targetType)
    {
        return build(targetType.asType());
    }
}
