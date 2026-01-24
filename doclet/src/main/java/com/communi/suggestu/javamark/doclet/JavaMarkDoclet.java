package com.communi.suggestu.javamark.doclet;

import com.communi.suggestu.javamark.doclet.builders.ConstantsFileBuilder;
import com.communi.suggestu.javamark.doclet.builders.PackageFileBuilder;
import com.communi.suggestu.javamark.doclet.builders.PackageLinkBuilder;
import com.communi.suggestu.javamark.doclet.builders.TypeDisplayNameBuilder;
import com.communi.suggestu.javamark.doclet.builders.TypeFileBuilder;
import com.communi.suggestu.javamark.doclet.builders.TypeLinkBuilder;
import com.communi.suggestu.javamark.doclet.utils.PackageLinkProvider;
import com.communi.suggestu.javamark.doclet.utils.TypeLinkProvider;
import com.communi.suggestu.javamark.doclet.utils.TypeUniverse;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import jdk.javadoc.internal.doclets.formats.html.HtmlConfiguration;
import jdk.javadoc.internal.doclets.formats.html.HtmlDoclet;
import jdk.javadoc.internal.doclets.toolkit.DocletException;
import jdk.javadoc.internal.doclets.toolkit.util.ClassTree;
import jdk.javadoc.internal.doclets.toolkit.util.DocFile;
import jdk.javadoc.internal.doclets.toolkit.util.DocPath;
import jdk.javadoc.internal.doclets.toolkit.util.DocPaths;
import org.apache.commons.io.FileUtils;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaMarkDoclet implements Doclet
{

    public JavaMarkDoclet()
    {
    }

    private final HtmlDoclet innerDoclet = new HtmlDoclet(this);

    @Override
    public void init(final Locale locale, final Reporter reporter)
    {
        innerDoclet.init(locale, reporter);
    }

    @Override
    public String getName()
    {
        return "javamark";
    }

    @Override
    public Set<? extends Option> getSupportedOptions()
    {
        return getConfiguration().getOptions().getSupportedOptions();
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.RELEASE_17;
    }

    public HtmlConfiguration getConfiguration()
    {
        return innerDoclet.getConfiguration();
    }

    @Override
    public boolean run(final DocletEnvironment environment)
    {
        innerDoclet.run(environment);
        DocFile outputDir = DocFile.createFileForOutput(
            getConfiguration(),
            DocPath.create("/")
        );

        try
        {
            FileUtils.deleteDirectory(Path.of(outputDir.getPath()).toFile());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        try
        {
            var classTree = new ClassTree(getConfiguration());
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

            var typeUniverse = new TypeUniverse(environment, environment.getElementUtils(), environment.getTypeUtils(), knownTypes, knownPackages);
            var typeLinkProvider = new TypeLinkProvider(typeUniverse, includedTypes);
            var typeLinkBuilder = new TypeLinkBuilder(typeUniverse, typeLinkProvider);
            var displayNameBuilder = new TypeDisplayNameBuilder(typeUniverse, typeLinkProvider);
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
                    processType(environment, typeUniverse, classTree, typeElement, typeLinkBuilder, packageLinkBuilder, displayNameBuilder);
                }
            }

            processConstants();
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
        DocFile target = DocFile.createFileForOutput(
            getConfiguration(),
            DocPath.create(packageFilePath(element) + "/index.md")
        );
        new PackageFileBuilder(
            Path.of(target.getPath()),
            typeLinkBuilder,
            packageLinkBuilder,
            typeUniverse,
            docTrees)
            .from(element)
            .build();
    }

    private void processType(
        DocletEnvironment environment, final TypeUniverse typeUniverse,
        final ClassTree classTree,
        TypeElement typeElement, TypeLinkBuilder typeLinkBuilder, PackageLinkBuilder packageLinkBuilder,
        final TypeDisplayNameBuilder displayNameBuilder) throws IOException, DocletException
    {
        DocFile target = DocFile.createFileForOutput(
            getConfiguration(),
            DocPath.create(typeFilePath(typeElement) + ".md")
        );
        new TypeFileBuilder(
            getConfiguration(),
            typeUniverse,
            classTree,
            environment.getTypeUtils(),
            Path.of(target.getPath()),
            packageLinkBuilder,
            typeLinkBuilder,
            displayNameBuilder)
            .from(typeElement)
            .build();
    }

    private void processConstants() throws IOException, DocletException
    {
        DocFile target = DocFile.createFileForOutput(
            getConfiguration(),
            DocPath.create("constant-values.md")
        );
        new ConstantsFileBuilder(
            getConfiguration(),
            Path.of(target.getPath())
        ).build();
    }

    private String packageFilePath(PackageElement packageElement)
    {
        return packageElement.getQualifiedName().toString().replace(".", File.separator).toLowerCase(Locale.ROOT);
    }

    private String typeFilePath(TypeElement typeElement) throws IOException
    {
        if (typeElement.getEnclosingElement() instanceof TypeElement outer)
        {
            return typeFilePath(outer) + "." + typeElement.getSimpleName();
        }

        if (typeElement.getEnclosingElement() instanceof PackageElement packageElement)
        {
            return packageFilePath(packageElement) + File.separator + typeElement.getSimpleName();
        }

        return typeElement.getSimpleName().toString();
    }
}
