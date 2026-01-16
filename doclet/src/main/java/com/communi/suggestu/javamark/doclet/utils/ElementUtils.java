package com.communi.suggestu.javamark.doclet.utils;

import com.sun.source.util.DocTrees;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.stream.Collectors;

public class ElementUtils
{
    public static PackageElement getEnclosingPackage(TypeElement element) {
        if (element.getEnclosingElement() instanceof PackageElement packageElement)
            return packageElement;

        if (element.getEnclosingElement() instanceof TypeElement typeElement)
            return getEnclosingPackage(typeElement);

        return null;
    }

    public static String getAnnotations(Element element) {
        StringBuilder sb = new StringBuilder();
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            sb.append("@").append(annotation.getAnnotationType()).append(" ");
        }
        return sb.toString();
    }

    public static String getDocComment(Element element, DocTrees docTrees) {
        var docTree = docTrees.getDocCommentTree(element);
        if (docTree == null)
            return "";
        return docTree
            .getFirstSentence()
            .stream()
            .map(Object::toString)
            .collect(Collectors.joining("\n"));
    }

    public static boolean isEnum(TypeElement type) {
        return type.getKind() == ElementKind.ENUM;
    }

    public static boolean isRecord(TypeElement type) {
        return type.getKind().name().equals("RECORD");
    }
}
