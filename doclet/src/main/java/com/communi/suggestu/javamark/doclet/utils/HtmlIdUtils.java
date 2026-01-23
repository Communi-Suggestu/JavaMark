package com.communi.suggestu.javamark.doclet.utils;

import jdk.javadoc.internal.doclets.formats.html.markup.HtmlId;
import jdk.javadoc.internal.doclets.toolkit.util.Utils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class HtmlIdUtils
{

    /**
     * Returns an id for an executable element, suitable for use when the
     * simple name and argument list will be unique within the page, such as
     * in the page for the declaration of the enclosing class or interface.
     *
     * @param element the element
     *
     * @return the id
     */
    public static HtmlId forMember(Utils utils, ExecutableElement element) {
        String a = element.getSimpleName()
            + utils.makeSignature(element, null, true, true);
        // utils.makeSignature includes spaces
        return HtmlId.of(a.replaceAll("\\s", ""));
    }


    /**
     * Returns an id for an executable element, including the context
     * of its documented enclosing class or interface.
     *
     * @param typeElement the enclosing class or interface
     * @param member      the element
     *
     * @return the id
     */
    public static HtmlId forMember(Utils utils, TypeElement typeElement, ExecutableElement member) {
        return HtmlId.of(utils.getSimpleName(member) + utils.signature(member, typeElement));
    }

    /**
     * Returns an id for a field, suitable for use when the simple name
     * will be unique within the page, such as in the page for the
     * declaration of the enclosing class or interface.
     *
     * <p>Warning: the name may not be unique if a property with the same
     * name is also being documented in the same class.
     *
     * @param element the element
     *
     * @return the id
     */
    public static HtmlId forMember(VariableElement element) {
        return HtmlId.of(element.getSimpleName().toString());
    }

    /**
     * Returns an id for a field, including the context
     * of its documented enclosing class or interface.
     *
     * @param typeElement the enclosing class or interface
     * @param member the element
     *
     * @return the id
     */
    public static HtmlId forMember(TypeElement typeElement, VariableElement member) {
        return HtmlId.of(typeElement.getQualifiedName() + "." + member.getSimpleName());
    }

}
