package com.communi.suggestu.javamark.doclet.utils;

import jdk.javadoc.internal.doclets.formats.html.HtmlDocletWriter;
import jdk.javadoc.internal.doclets.toolkit.Content;

import javax.lang.model.element.TypeElement;

public class SignatureUtils
{
    /**
     * Reflectively instantiates a Signatures.TypeSignature and invokes its toContent() method.
     * @param typeElement The TypeElement instance to pass to the constructor.
     * @param htmlDocletWriter The HtmlDocletWriter instance to pass to the constructor.
     * @return The result of toContent(), or null if reflection fails.
     */
    public static Content createTypeSignatureAndToContent(TypeElement typeElement, HtmlDocletWriter htmlDocletWriter) {
        try {
            // Load the Signatures class
            Class<?> signaturesClass = Class.forName("jdk.javadoc.internal.doclets.formats.html.Signatures");
            // Find the TypeSignature inner class
            for (Class<?> inner : signaturesClass.getDeclaredClasses()) {
                if (inner.getSimpleName().equals("TypeSignature")) {
                    // Get the constructor (TypeElement, HtmlDocletWriter)
                    var ctor = inner.getDeclaredConstructor(TypeElement.class, HtmlDocletWriter.class);
                    ctor.setAccessible(true);
                    Object typeSignature = ctor.newInstance(typeElement, htmlDocletWriter);
                    // Call toContent()
                    var toContent = inner.getDeclaredMethod("toContent");
                    toContent.setAccessible(true);
                    return (Content) toContent.invoke(typeSignature);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Could not find type signatures!");
    }
}
