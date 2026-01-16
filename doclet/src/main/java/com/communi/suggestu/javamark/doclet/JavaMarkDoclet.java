package com.communi.suggestu.javamark.doclet;

import com.communi.suggestu.javamark.doclet.builders.PackageFileBuilder;
import com.communi.suggestu.javamark.doclet.builders.PackageLinkBuilder;
import com.communi.suggestu.javamark.doclet.builders.TypeFileBuilder;
import com.communi.suggestu.javamark.doclet.builders.TypeLinkBuilder;
import com.communi.suggestu.javamark.doclet.utils.PackageLinkProvider;
import com.communi.suggestu.javamark.doclet.utils.TypeLinkProvider;
import com.communi.suggestu.javamark.doclet.utils.TypeUniverse;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaMarkDoclet implements Doclet
{
    private Configuration configuration;

    @Override
    public void init(final Locale locale, final Reporter reporter)
    {
        configuration = new Configuration();
    }

    @Override
    public String getName()
    {
        return "javamark";
    }

    @Override
    public Set<? extends Option> getSupportedOptions()
    {
        return configuration.getSupportedOptions();
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.RELEASE_17;
    }

    @Override
    public boolean run(final DocletEnvironment environment)
    {
        try
        {
            if (!configuration.isValid())
            {
                throw new IllegalStateException("Configuration was not valid!");
            }

            configuration.prepare();

            var knownTypes = environment.getIncludedElements()
                .stream()
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .collect(Collectors.toSet());
            var knownPackages = environment.getIncludedElements()
                .stream()
                .filter(PackageElement.class::isInstance)
                .map(PackageElement.class::cast)
                .collect(Collectors.toSet());

            var includedTypes = knownTypes.stream()
                .map(TypeElement::asType)
                .collect(Collectors.toSet());

            var typeUniverse = new TypeUniverse(environment.getElementUtils(), environment.getTypeUtils(), knownTypes, knownPackages);
            var typeLinkProvider = new TypeLinkProvider(typeUniverse, includedTypes);
            var typeLinkBuilder = new TypeLinkBuilder(typeLinkProvider, typeUniverse);
            var packageLinkProvider = new PackageLinkProvider(knownPackages);
            var packageLinkBuilder = new PackageLinkBuilder(packageLinkProvider);

            for (final Element includedElement : environment.getIncludedElements())
            {
                if (includedElement instanceof PackageElement packageElement)
                {
                    processPackage(packageElement, typeUniverse, typeLinkBuilder, packageLinkBuilder, environment.getDocTrees());
                }
                else if (includedElement instanceof TypeElement typeElement)
                {
                    processType(environment, typeElement, typeLinkBuilder, packageLinkBuilder);
                }
            }
        }
        catch (Exception exception)
        {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
            return false;
        }

        return true;
    }

    private void processPackage(
        PackageElement element, final TypeUniverse typeUniverse, final TypeLinkBuilder typeLinkBuilder, PackageLinkBuilder packageLinkBuilder,
        final DocTrees docTrees)
        throws IOException
    {
        var targetPath = configuration.getDestination().resolve(packageFilePath(element) + "/index.md");
        new PackageFileBuilder(
            targetPath,
            typeLinkBuilder,
            packageLinkBuilder,
            typeUniverse,
            docTrees)
            .from(element)
            .build();
    }

    private void processType(DocletEnvironment environment, TypeElement typeElement, TypeLinkBuilder typeLinkBuilder, PackageLinkBuilder packageLinkBuilder) throws IOException
    {
        var targetPath = configuration.getDestination().resolve(typeFilePath(typeElement) + ".md");
        new TypeFileBuilder(
            environment.getTypeUtils(),
            targetPath,
            packageLinkBuilder,
            typeLinkBuilder)
            .from(typeElement)
            .build();
    }

    private String packageFilePath(PackageElement packageElement)
    {
        return packageElement.getQualifiedName().toString().replace(".", File.separator).toLowerCase(Locale.ROOT);
    }

    private String typeFilePath(TypeElement typeElement) throws IOException
    {
        if (typeElement.getEnclosingElement() instanceof TypeElement outer)
        {
            return typeFilePath(outer) + "$" + typeElement.getSimpleName();
        }

        if (typeElement.getEnclosingElement() instanceof PackageElement packageElement)
        {
            return packageFilePath(packageElement) + File.separator + typeElement.getSimpleName();
        }

        return typeElement.getSimpleName().toString();
    }
}
