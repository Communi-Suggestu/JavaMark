package com.communi.suggestu.javamark.doclet.utils;

import jdk.javadoc.doclet.DocletEnvironment;

import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor14;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility class for extracting module, package, and type elements from a DocletEnvironment.
 * <p>
 * This class provides static factory methods to create an extractor for either the included or specified elements
 * in a given {@link jdk.javadoc.doclet.DocletEnvironment}. It then categorizes the elements into modules, packages,
 * and types, which can be retrieved via the respective getter methods.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 *     DocletElementExtractor extractor = DocletElementExtractor.included(environment);
 *     Set<ModuleElement> modules = extractor.getModules();
 *     Set<PackageElement> packages = extractor.getPackages();
 *     Set<TypeElement> types = extractor.getTypes();
 * </pre>
 * </p>
 */
public class DocletElementExtractor
{

    /**
     * Returns a new instance of the extractor for the elements included in the given DocletEnvironment.
     *
     * @param environment the DocletEnvironment to extract elements from
     * @return a DocletElementExtractor for the included elements
     */
    public static DocletElementExtractor included(DocletEnvironment environment) {
        return new DocletElementExtractor(environment, true);
    }

    /**
     * Returns a new instance of the extractor for the elements specified in the given DocletEnvironment.
     *
     * @param environment the DocletEnvironment to extract elements from
     * @return a DocletElementExtractor for the specified elements
     */
    public static DocletElementExtractor specified(DocletEnvironment environment) {
        return new DocletElementExtractor(environment, false);
    }

    private final Set<ModuleElement>  modules  = new LinkedHashSet<>();
    private final Set<PackageElement> packages = new LinkedHashSet<>();
    private final Set<TypeElement>    types    = new LinkedHashSet<>();

    private DocletElementExtractor(DocletEnvironment docEnv, boolean included) {

        Set<? extends Element> inset = included
            ? docEnv.getIncludedElements()
            : docEnv.getSpecifiedElements();

        for (Element e : inset) {
            new SimpleElementVisitor14<Void, Void>() {
                @Override
                public Void visitModule(ModuleElement e, Void p) {
                    modules.add(e);
                    return null;
                }

                @Override
                public Void visitPackage(PackageElement e, Void p) {
                    packages.add(e);
                    return null;
                }

                @Override
                public Void visitType(TypeElement e, Void p) {
                    types.add(e);
                    return null;
                }

                @Override
                protected Void defaultAction(Element e, Void p) {
                    throw new AssertionError("unexpected element: " + e);
                }

            }.visit(e);
        }
    }

    /**
     * Gets the set of module elements extracted from the DocletEnvironment.
     *
     * @return a set of ModuleElement objects
     */
    public Set<ModuleElement> getModules()
    {
        return modules;
    }

    /**
     * Gets the set of package elements extracted from the DocletEnvironment.
     *
     * @return a set of PackageElement objects
     */
    public Set<PackageElement> getPackages()
    {
        return packages;
    }

    /**
     * Gets the set of type elements extracted from the DocletEnvironment.
     *
     * @return a set of TypeElement objects
     */
    public Set<TypeElement> getTypes()
    {
        return types;
    }
}
