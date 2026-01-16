package com.communi.suggestu.javamark.doclet.builders;

import com.communi.suggestu.javamark.doclet.utils.Constants;
import com.communi.suggestu.javamark.doclet.utils.ElementUtils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

public class TypeFileBuilder
{
    private final Types           typeUtils;
    private final Path            path;
    private final PackageLinkBuilder packageLinkBuilder;
    private final TypeLinkBuilder linkBuilder;
    private       String          result = null;

    public TypeFileBuilder(final Types typeUtils, final Path path, final PackageLinkBuilder packageLinkBuilder, final TypeLinkBuilder linkBuilder) {
        this.typeUtils = typeUtils;
        this.path = path;
        this.packageLinkBuilder = packageLinkBuilder;
        this.linkBuilder = linkBuilder;
    }

    public TypeFileBuilder from(TypeElement element) {
        String builder =
            "---" + Constants.NEW_LINE
            + "title:" + element.getSimpleName() + Constants.NEW_LINE
            + "---" + Constants.NEW_LINE;

        final PackageElement packageElement = ElementUtils.getEnclosingPackage(element);
        if (packageElement != null) {
            String packageLink = packageLinkBuilder.withDisplayMode(PackageLinkBuilder.DisplayMode.FULLY_QUALIFIED_NAME).build(packageElement, packageElement);
            builder += "_Package:_ " + packageLink + Constants.NEW_LINE;
        }

        builder += "# " + StringUtils.capitalize(element.getKind().name().toLowerCase(Locale.ROOT)) + " " + element.getSimpleName() + Constants.NEW_LINE;
        builder += "---" + Constants.NEW_LINE;

        if (element.getKind() == ElementKind.CLASS)
            builder += extractSuperTypeHierarchy(element);

        result = builder;
        return this;
    }

    private String extractSuperTypeHierarchy(TypeElement element) {
        var superTypes = typeUtils.directSupertypes(element.asType());
        StringBuilder superTypeHierachyString = new StringBuilder();
        for (int superTypeIndex = 0; superTypeIndex < superTypes.size(); superTypeIndex++)
        {
            String link = linkBuilder.withDisplayMode(TypeLinkBuilder.DisplayMode.FULLY_QUALIFIED_NAME).build(element, superTypes.get(superTypeIndex));
            superTypeHierachyString.append(String.join("", Collections.nCopies(superTypeIndex * 4, "&ensp;"))).append(link).append(Constants.NEW_LINE);
        }
        // Add the current element itself as the last in the hierarchy
        String selfLink = linkBuilder.withDisplayMode(TypeLinkBuilder.DisplayMode.FULLY_QUALIFIED_NAME).build(element, element.asType());
        superTypeHierachyString.append(String.join("", Collections.nCopies(superTypes.size() * 4, "&ensp;"))).append(selfLink).append(Constants.NEW_LINE);
        return superTypeHierachyString.toString();
    }

    public void build() throws IOException
    {
        if (result == null)
            return;

        Files.createDirectories(path.getParent());
        Files.writeString(path, result);
    }
}
