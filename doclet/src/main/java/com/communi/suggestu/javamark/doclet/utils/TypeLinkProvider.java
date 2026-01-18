package com.communi.suggestu.javamark.doclet.utils;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Provides relative path links between types, only for types known to a given TypeUniverse.
 */
public class TypeLinkProvider {
    private final TypeUniverse typeUniverse;

    public TypeLinkProvider(TypeUniverse typeUniverse, Set<TypeMirror> knownTypes) {
        this.typeUniverse = typeUniverse;
    }

    /**
     * Generates a relative path from sourceType to targetType, or null if either is not in the known set.
     * The path is based on package hierarchy and type name (e.g., ../../foo/Bar.html).
     */
    public String getRelativeLink(Element sourceType, TypeMirror targetType) {
        if (sourceType.equals(typeUniverse.asTypeElement(targetType))) {
            return getTypeFileName(targetType);
        }

        if (!typeUniverse.contains(targetType))
            return null;

        if (!(sourceType instanceof TypeElement) && !(sourceType instanceof PackageElement)) {
            return null;
        }

        List<String> sourcePkg = sourceType instanceof TypeElement typeElement ? typeUniverse.getPackageHierarchy(typeElement.asType()) : typeUniverse.getPackageHierarchy((PackageElement) sourceType);
        List<String> targetPkg = typeUniverse.getPackageHierarchy(targetType);
        int common = 0;
        while (common < sourcePkg.size() && common < targetPkg.size() && Objects.equals(sourcePkg.get(common), targetPkg.get(common))) {
            common++;
        }
        StringBuilder rel = new StringBuilder();
        for (int i = common; i < sourcePkg.size(); i++) {
            rel.append("../");
        }
        for (int i = common; i < targetPkg.size(); i++) {
            rel.append(targetPkg.get(i)).append("/");
        }
        rel.append(getTypeFileName(targetType));
        return rel.toString();
    }

    private String getTypeFileName(TypeMirror type) {
        TypeElement el = typeUniverse.asTypeElement(type);
        return typeFilePath(el) + ".md";
    }

    private String typeFilePath(TypeElement typeElement)
    {
        if (typeElement.getEnclosingElement() instanceof TypeElement outer)
        {
            return typeFilePath(outer) + "$" + typeElement.getSimpleName();
        }

        return typeElement.getSimpleName().toString();
    }
}

