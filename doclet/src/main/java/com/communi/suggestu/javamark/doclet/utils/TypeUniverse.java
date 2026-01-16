package com.communi.suggestu.javamark.doclet.utils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TypeUniverse provides utilities to navigate and introspect Java types using TypeMirror and related APIs.
 */
public class TypeUniverse {
    private final Elements elementUtils;
    private final Types typeUtils;
    private final Set<TypeElement> knownTypes;
    private final Set<PackageElement> knownPackages;
    public TypeUniverse(Elements elementUtils, Types typeUtils, final Set<TypeElement> knownTypes, final Set<PackageElement> knownPackages) {
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.knownTypes = knownTypes;
        this.knownPackages = knownPackages;
    }

    /**
     * Returns the TypeElement for a given TypeMirror.
     */
    public TypeElement asTypeElement(TypeMirror typeMirror) {
        return (TypeElement) typeUtils.asElement(typeMirror);
    }

    /**
     * Returns the package hierarchy for a given TypeMirror.
     */
    public List<String> getPackageHierarchy(TypeMirror typeMirror) {
        TypeElement typeElement = asTypeElement(typeMirror);
        if (typeElement == null) return Collections.emptyList();
        PackageElement pkg = elementUtils.getPackageOf(typeElement);
        return getPackageHierarchy(pkg);
    }

    public List<String> getPackageHierarchy(PackageElement pkg)
    {
        List<String> hierarchy = new ArrayList<>();
        while (pkg != null && !pkg.isUnnamed()) {
            hierarchy.add(0, pkg.getSimpleName().toString());
            Element enclosing = pkg.getEnclosingElement();
            if (enclosing instanceof PackageElement) {
                pkg = (PackageElement) enclosing;
            } else {
                break;
            }
        }
        return hierarchy;
    }

    /**
     * Returns the direct superclass of the given type, or null if none.
     */
    public TypeMirror getSuperType(TypeMirror typeMirror) {
        TypeElement typeElement = asTypeElement(typeMirror);
        if (typeElement == null) return null;
        return typeElement.getSuperclass();
    }

    /**
     * Returns the interfaces implemented or extended by the given type.
     */
    public List<TypeMirror> getInterfaces(TypeMirror typeMirror) {
        TypeElement typeElement = asTypeElement(typeMirror);
        if (typeElement == null) return Collections.emptyList();
        return new ArrayList<>(typeElement.getInterfaces());
    }

    /**
     * Returns all methods declared in the given type (excluding inherited methods).
     */
    public List<ExecutableElement> getDeclaredMethods(TypeMirror typeMirror) {
        TypeElement typeElement = asTypeElement(typeMirror);
        if (typeElement == null) return Collections.emptyList();
        return typeElement.getEnclosedElements().stream()
                .filter(e -> e instanceof ExecutableElement)
                .map(e -> (ExecutableElement) e)
                .collect(Collectors.toList());
    }

    /**
     * Returns the parameters of a given method.
     */
    public List<? extends VariableElement> getMethodParameters(ExecutableElement method) {
        return method.getParameters();
    }

    /**
     * Returns all supertypes (superclass and interfaces, recursively) for the given type.
     */
    public Set<TypeMirror> getAllSuperTypes(TypeMirror typeMirror) {
        Set<TypeMirror> result = new java.util.HashSet<>();
        collectSuperTypes(typeMirror, result);
        return result;
    }

    private void collectSuperTypes(TypeMirror typeMirror, Set<TypeMirror> result) {
        TypeElement typeElement = asTypeElement(typeMirror);
        if (typeElement == null) return;
        TypeMirror superClass = typeElement.getSuperclass();
        if (superClass != null && typeUtils.asElement(superClass) != null) {
            if (result.add(superClass)) {
                collectSuperTypes(superClass, result);
            }
        }
        for (TypeMirror iface : typeElement.getInterfaces()) {
            if (result.add(iface)) {
                collectSuperTypes(iface, result);
            }
        }
    }

    public List<TypeElement> getTypesInPackage(PackageElement pkg) {
        if (pkg == null) return Collections.emptyList();
        return knownTypes.stream()
            .filter(t -> {
                PackageElement enclosing = com.communi.suggestu.javamark.doclet.utils.ElementUtils.getEnclosingPackage(t);
                return enclosing != null && enclosing.equals(pkg);
            })
            .collect(Collectors.toList());
    }

    public PackageElement getParentOf(PackageElement pkg) {
        if (!(pkg.getEnclosingElement() instanceof PackageElement packageElement))
            return null;

        return packageElement;
    }

    public List<PackageElement> getChildPackages(PackageElement pkg) {
        return knownPackages.stream()
            .filter(p -> isDirectChild(pkg, p)).toList();
    }

    private static boolean isDirectChild(PackageElement parent, PackageElement child) {
        String parentName = parent.getQualifiedName().toString();
        String childName = child.getQualifiedName().toString();
        if (!childName.startsWith(parentName + ".")) return false;
        return childName.substring(parentName.length() + 1).indexOf('.') < 0;
    }
}
