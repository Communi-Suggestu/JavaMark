package com.communi.suggestu.javamark.doclet.builders;

import com.communi.suggestu.javamark.doclet.utils.Constants;
import com.communi.suggestu.javamark.doclet.utils.ElementUtils;
import com.communi.suggestu.javamark.doclet.utils.TypeUniverse;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;

public class TypeFileBuilder
{
    private final TypeUniverse typeUniverse;
    private final Types           typeUtils;
    private final Path            path;
    private final PackageLinkBuilder packageLinkBuilder;
    private final TypeLinkBuilder linkBuilder;
    private final TypeDisplayNameBuilder displayNameBuilder;
    private       String          result = null;

    public TypeFileBuilder(final TypeUniverse typeUniverse, final Types typeUtils, final Path path, final PackageLinkBuilder packageLinkBuilder, final TypeLinkBuilder linkBuilder,
        final TypeDisplayNameBuilder displayNameBuilder) {
        this.typeUniverse = typeUniverse;
        this.typeUtils = typeUtils;
        this.path = path;
        this.packageLinkBuilder = packageLinkBuilder;
        this.linkBuilder = linkBuilder;
        this.displayNameBuilder = displayNameBuilder;
    }

    public TypeFileBuilder from(TypeElement element) {
        String builder =
            "--- \n" +
            "title: " + element.getSimpleName() + "\n" +
            "aside: false \n" +
            "---\n";

        final PackageElement packageElement = ElementUtils.getEnclosingPackage(element);
        if (packageElement != null) {
            String packageLink = packageLinkBuilder.withDisplayMode(PackageLinkBuilder.DisplayMode.FULLY_QUALIFIED_NAME).build(packageElement, packageElement);
            builder += "_Package:_ " + packageLink + Constants.NEW_LINE;
        }

        builder += "# " + StringUtils.capitalize(element.getKind().name().toLowerCase(Locale.ROOT)) + " " +
            displayNameBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.JAVADOC).build(element) +
            Constants.NEW_LINE;

        if (element.getKind() == ElementKind.CLASS)
            builder += extractSuperTypeHierarchy(element) + Constants.NEW_LINE;

        var implementingInterfaces = listImplementedInterfaces(element);
        var directSubtypes = listDirectKnownSubTypes(element);
        var enclosingClass = listEnclosingClass(element);

        if (!implementingInterfaces.isBlank())
            builder += implementingInterfaces + Constants.NEW_LINE + "\n";

        if (!directSubtypes.isBlank())
            builder += directSubtypes + Constants.NEW_LINE + "\n";

        if (!enclosingClass.isBlank())
            builder += enclosingClass + Constants.NEW_LINE + "\n";

        builder += "---\n";

        result = builder;
        return this;
    }

    private String extractSuperTypeHierarchy(TypeElement element) {
        var superTypes = typeUniverse.getSuperTypeHierarchy(element.asType());
        StringBuilder superTypeHierarchyString = new StringBuilder();
        for (int superTypeIndex = 0; superTypeIndex < superTypes.size(); superTypeIndex++)
        {
            String link = linkBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.FULLY_QUALIDIED_JAVADOC_NAME).build(element, superTypes.get(superTypeIndex));
            superTypeHierarchyString
                .append(String.join("", Collections.nCopies(superTypeIndex, "&ensp;")))
                .append(superTypeIndex == 0 ? "" : "↳")
                .append(link).append(Constants.NEW_LINE);
        }
        // Add the current element itself as the last in the hierarchy
        String selfLink = linkBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.FULLY_QUALIDIED_JAVADOC_NAME).build(element, element.asType());
        superTypeHierarchyString
            .append(String.join("", Collections.nCopies(superTypes.size(), "&ensp;")))
            .append("↳")
            .append(selfLink).append(Constants.NEW_LINE);
        return superTypeHierarchyString.toString();
    }

    private String listImplementedInterfaces(TypeElement element) {
        var interfaces = typeUtils.directSupertypes(element.asType())
            .stream()
            .filter(type -> typeUniverse.asElement(type).getKind() == ElementKind.INTERFACE)
            .toList();

        if (interfaces.isEmpty())
            return "";

        return "**All Implemented Interfaces:**" + Constants.NEW_LINE +
            interfaces.stream()
                .map(i -> linkBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.JAVADOC).build(element, i))
                .collect(Collectors.joining(", "));
    }

    private String listDirectKnownSubTypes(TypeElement element) {
        var directSubTypes = typeUniverse.getDirectSubTypes(element);
        if (directSubTypes == null || directSubTypes.isEmpty())
            return "";

        return "**Direct Known Subclasses:**" + Constants.NEW_LINE +
            directSubTypes.stream()
                .map(s -> linkBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.JAVADOC).build(element, s))
                .collect(Collectors.joining(", "));
    }

    private String listEnclosingClass(TypeElement element) {
        if (!(element.getEnclosingElement() instanceof TypeElement enclosingType))
            return "";

        return "**Enclosing Type:**" + Constants.NEW_LINE +
            linkBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.JAVADOC).build(element, enclosingType);
    }

    private String listTypeParameters(TypeElement element) {

        var paramTrees = typeUniverse.getTypeParamTrees(element);
        if (paramTrees.isEmpty())
            return "";

        var result = new StringBuilder();
        var indexMap = typeUniverse.mapNameToPosition(element.getTypeParameters());
        result.append("**Type Parameters:**").append(Constants.NEW_LINE);
        paramTrees.forEach(tree -> {
            var name = tree.getName().getName().toString();
            if (indexMap.containsKey(name))
                return;

            result.append(name).append(" - ").append()
        });

        return result.toString();
    }

    public void build() throws IOException
    {
        if (result == null)
            return;

        Files.createDirectories(path.getParent());
        Files.writeString(path, result);
    }

    private String typeDisplayName(TypeElement typeElement)
    {
        if (typeElement.getEnclosingElement() instanceof TypeElement outer)
        {
            return typeDisplayName(outer) + "." + typeElement.getSimpleName();
        }

        return typeElement.getSimpleName().toString();
    }
}
