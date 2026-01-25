package com.communi.suggestu.javamark.doclet.builders;

import com.communi.suggestu.javamark.doclet.content.MarkdownAwareContentBuilder;
import com.communi.suggestu.javamark.doclet.content.NoneEncodingContentBuilder;
import com.communi.suggestu.javamark.doclet.utils.Constants;
import com.communi.suggestu.javamark.doclet.utils.DocTreeUtils;
import com.communi.suggestu.javamark.doclet.utils.ElementUtils;
import com.communi.suggestu.javamark.doclet.utils.SignatureUtils;
import com.communi.suggestu.javamark.doclet.utils.TypeUniverse;
import com.communi.suggestu.javamark.doclet.writers.MarkdownAnnotationTypeMemberWriterImpl;
import com.communi.suggestu.javamark.doclet.writers.MarkdownConstructorWriterImpl;
import com.communi.suggestu.javamark.doclet.writers.MarkdownEnumConstantsWriterImpl;
import com.communi.suggestu.javamark.doclet.writers.MarkdownFieldWriterImpl;
import com.communi.suggestu.javamark.doclet.writers.MarkdownHtmlDocletWriter;
import com.communi.suggestu.javamark.doclet.writers.MarkdownMethodWriterImpl;
import com.communi.suggestu.javamark.doclet.writers.MarkdownPropertyWriterImpl;
import com.communi.suggestu.javamark.doclet.writers.MarkdownSubWriterHolderWriter;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocTree;
import jdk.javadoc.internal.doclets.formats.html.HtmlConfiguration;
import jdk.javadoc.internal.doclets.formats.html.HtmlDocletWriter;
import jdk.javadoc.internal.doclets.formats.html.HtmlOptions;
import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.toolkit.Content;
import jdk.javadoc.internal.doclets.toolkit.DocletException;
import jdk.javadoc.internal.doclets.toolkit.builders.AbstractBuilder;
import jdk.javadoc.internal.doclets.toolkit.builders.AnnotationTypeMemberBuilder;
import jdk.javadoc.internal.doclets.toolkit.builders.BuilderFactory;
import jdk.javadoc.internal.doclets.toolkit.builders.ConstructorBuilder;
import jdk.javadoc.internal.doclets.toolkit.builders.EnumConstantBuilder;
import jdk.javadoc.internal.doclets.toolkit.builders.FieldBuilder;
import jdk.javadoc.internal.doclets.toolkit.builders.MethodBuilder;
import jdk.javadoc.internal.doclets.toolkit.builders.PropertyBuilder;
import jdk.javadoc.internal.doclets.toolkit.util.ClassTree;
import jdk.javadoc.internal.doclets.toolkit.util.CommentHelper;
import jdk.javadoc.internal.doclets.toolkit.util.DocFile;
import jdk.javadoc.internal.doclets.toolkit.util.DocPath;
import jdk.javadoc.internal.doclets.toolkit.util.Utils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TypeFileBuilder
{
    private final HtmlConfiguration      configuration;
    private final TypeUniverse           typeUniverse;
    private final ClassTree              classTree;
    private final Types                  typeUtils;
    private final Path                   rootPath;
    private final Path                   path;
    private final PackageLinkBuilder     packageLinkBuilder;
    private final TypeLinkBuilder        linkBuilder;
    private final TypeDisplayNameBuilder displayNameBuilder;

    private final Utils       utils;
    private final HtmlOptions options;

    private String result = null;

    public TypeFileBuilder(
        final HtmlConfiguration configuration, final TypeUniverse typeUniverse,
        final ClassTree classTree,
        final Types typeUtils, final Path path, final PackageLinkBuilder packageLinkBuilder, final TypeLinkBuilder linkBuilder,
        final TypeDisplayNameBuilder displayNameBuilder)
    {
        this.configuration = configuration;
        this.typeUniverse = typeUniverse;
        this.typeUtils = typeUtils;
        this.packageLinkBuilder = packageLinkBuilder;
        this.linkBuilder = linkBuilder;
        this.displayNameBuilder = displayNameBuilder;

        DocFile target = DocFile.createFileForOutput(
            configuration,
            DocPath.create("/")
        );

        this.utils = configuration.utils;
        this.options = configuration.getOptions();

        this.rootPath = Path.of(target.getPath());
        this.path = rootPath.relativize(path);
        this.classTree = classTree;
    }

    public TypeFileBuilder from(TypeElement element) throws DocletException
    {
        String builder =
            "--- \n" +
                "title: " + element.getSimpleName() + "\n" +
                "aside: false \n" +
                "sidebar: false \n" +
                "---\n";

        final PackageElement packageElement = ElementUtils.getEnclosingPackage(element);
        if (packageElement != null)
        {
            String packageLink = packageLinkBuilder.withDisplayMode(PackageLinkBuilder.DisplayMode.FULLY_QUALIFIED_NAME).build(packageElement, packageElement);
            builder += "_Package:_ " + packageLink + Constants.MARKDOWN_NEW_LINE;
        }

        String key = switch (element.getKind()) {
            case INTERFACE       -> "doclet.Interface";
            case ENUM            -> "doclet.Enum";
            case RECORD          -> "doclet.RecordClass";
            case ANNOTATION_TYPE -> "doclet.AnnotationType";
            case CLASS           -> "doclet.Class";
            default -> throw new IllegalStateException(element.getKind() + " " + element);
        };

        var kindTitle = configuration.docResources.getText(key);

        builder += "# " + StringUtils.capitalize(kindTitle) + " " +
            displayNameBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.JAVADOC).build(element) +
            Constants.MARKDOWN_NEW_LINE;

        if (element.getKind() == ElementKind.CLASS)
        {
            builder += extractSuperTypeHierarchy(element) + Constants.MARKDOWN_NEW_LINE;
        }

        var typeParameters = listTypeParameters(element);
        var superInterfaces = listSuperInterfaces(element);
        var implementingInterfaces = listImplementedInterfaces(element);
        var directSubtypes = listDirectKnownSubTypes(element);
        var directSubInterfaces = listDirectKnownSubInterfaces(element);
        var interfaceUsage = listDirectKnownImplementers(element);
        var enclosingClass = listEnclosingClass(element);
        var functionalInterfaces = listFunctionalInterfaceInformation(element);

        if (!typeParameters.isBlank())
        {
            builder += typeParameters + "\n";
        }

        if (!superInterfaces.isBlank())
        {
            builder += superInterfaces + Constants.MARKDOWN_NEW_LINE + "\n";
        }

        if (!implementingInterfaces.isBlank())
        {
            builder += implementingInterfaces + Constants.MARKDOWN_NEW_LINE + "\n";
        }

        if (!directSubtypes.isBlank())
        {
            builder += directSubtypes + Constants.MARKDOWN_NEW_LINE + "\n";
        }

        if (!directSubInterfaces.isBlank())
        {
            builder += directSubInterfaces + Constants.MARKDOWN_NEW_LINE + "\n";
        }

        if (!interfaceUsage.isBlank())
        {
            builder += interfaceUsage + Constants.MARKDOWN_NEW_LINE + "\n";
        }

        if (!enclosingClass.isBlank())
        {
            builder += enclosingClass + Constants.MARKDOWN_NEW_LINE + "\n";
        }

        if (!functionalInterfaces.isBlank())
        {
            builder += functionalInterfaces + Constants.MARKDOWN_NEW_LINE + "\n";
        }

        builder += "---\n";

        var classSignature = listClassSignature(element);
        var deprecationNotice = listDeprecationInformation(element);
        var classDescription = listClassDescription(element);
        var classTags = listClassTags(element);
        var memberSummary = listMemberSummary(element);

        builder += classSignature + Constants.MARKDOWN_NEW_LINE + "\n";
        builder += deprecationNotice + Constants.MARKDOWN_NEW_LINE + "\n";
        builder += classDescription + Constants.MARKDOWN_NEW_LINE + "\n";
        builder += classTags + Constants.MARKDOWN_NEW_LINE + "\n";
        builder += memberSummary + Constants.MARKDOWN_NEW_LINE + "\n";

        var enumMemberDetails = listEnumConstantsDetails(element);
        var propertyMemberDetails = listPropertyDetails(element);
        var fieldMemberDetails = listFieldDetails(element);
        var constructorMemberDetails = listConstructorDetails(element);
        var annotationTypeMemberDetails = listAnnotationMemberDetails(element);
        var methodDetails = listMethodMemberDetails(element);

        builder += enumMemberDetails + Constants.MARKDOWN_NEW_LINE + "\n";
        builder += propertyMemberDetails + Constants.MARKDOWN_NEW_LINE + "\n";
        builder += fieldMemberDetails + Constants.MARKDOWN_NEW_LINE + "\n";
        builder += constructorMemberDetails + Constants.MARKDOWN_NEW_LINE + "\n";
        builder += annotationTypeMemberDetails + Constants.MARKDOWN_NEW_LINE + "\n";
        builder += methodDetails + Constants.MARKDOWN_NEW_LINE + "\n";

        result = builder;
        return this;
    }

    private String extractSuperTypeHierarchy(TypeElement element)
    {
        var superTypes = typeUniverse.getSuperTypeHierarchy(element.asType());
        StringBuilder superTypeHierarchyString = new StringBuilder();
        for (int superTypeIndex = 0; superTypeIndex < superTypes.size(); superTypeIndex++)
        {
            String link = linkBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.FULLY_QUALIDIED_JAVADOC_NAME).build(element, superTypes.get(superTypeIndex));
            superTypeHierarchyString
                .append(String.join("", Collections.nCopies(superTypeIndex, "&ensp;")))
                .append(superTypeIndex == 0 ? "" : "↳")
                .append(link).append(Constants.MARKDOWN_NEW_LINE);
        }
        // Add the current element itself as the last in the hierarchy
        String selfLink = linkBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.FULLY_QUALIDIED_JAVADOC_NAME).build(element, element.asType());
        superTypeHierarchyString
            .append(String.join("", Collections.nCopies(superTypes.size(), "&ensp;")))
            .append("↳")
            .append(selfLink).append(Constants.MARKDOWN_NEW_LINE);
        return superTypeHierarchyString.toString();
    }

    private String listTypeParameters(TypeElement element)
    {

        var paramTrees = typeUniverse.getTypeParamTrees(element);
        if (paramTrees.isEmpty())
        {
            return "";
        }

        var result = new StringBuilder();
        var indexMap = typeUniverse.mapNameToPosition(element.getTypeParameters());
        result.append("**Type Parameters:**").append(Constants.MARKDOWN_NEW_LINE);
        paramTrees.forEach(tree -> {
            var name = tree.getName().getName().toString();
            if (!indexMap.containsKey(name))
            {
                return;
            }

            var htmlWriter = new MarkdownHtmlDocletWriter(configuration, DocPath.create(path.toString()));
            var description = DocTreeUtils.getTags(tree, configuration);
            result.append(name).append(" - ").append(
                htmlWriter.commentTagsToContent(element, description, false, false)
            ).append(Constants.MARKDOWN_NEW_LINE);
        });

        return result.toString();
    }

    private String listSuperInterfaces(TypeElement element)
    {
        if (!configuration.utils.isInterface(element))
        {
            return "";
        }

        SortedSet<TypeMirror> interfaces = new TreeSet<>(configuration.utils.comparators.makeTypeMirrorClassUseComparator());
        interfaces.addAll(configuration.utils.getAllInterfaces(element));

        if (interfaces.isEmpty())
        {
            return "";
        }

        return "**All Extended Interfaces:**" + Constants.MARKDOWN_NEW_LINE +
            interfaces.stream()
                .map(i -> linkBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.JAVADOC).build(element, i))
                .collect(Collectors.joining(", "));
    }

    private String listImplementedInterfaces(TypeElement element)
    {
        if (!typeUniverse.isClass(element))
        {
            return "";
        }

        var interfaces = typeUtils.directSupertypes(element.asType())
            .stream()
            .filter(type -> typeUniverse.asElement(type).getKind() == ElementKind.INTERFACE)
            .toList();

        if (interfaces.isEmpty())
        {
            return "";
        }

        return "**All Implemented Interfaces:**" + Constants.MARKDOWN_NEW_LINE +
            interfaces.stream()
                .map(i -> linkBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.JAVADOC).build(element, i))
                .collect(Collectors.joining(", "));
    }

    private String listDirectKnownSubTypes(TypeElement element)
    {
        if (!configuration.utils.isClass(element))
        {
            return "";
        }

        var directSubTypes = typeUniverse.getDirectSubTypes(element);
        if (directSubTypes == null || directSubTypes.isEmpty())
        {
            return "";
        }

        return "**Direct Known Subclasses:**" + Constants.MARKDOWN_NEW_LINE +
            directSubTypes.stream()
                .map(s -> linkBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.JAVADOC).build(element, s))
                .collect(Collectors.joining(", "));
    }

    private String listDirectKnownSubInterfaces(TypeElement element)
    {
        if (!configuration.utils.isInterface(element))
        {
            return "";
        }

        var directSubTypes = typeUniverse.getDirectSubTypes(element);
        if (directSubTypes == null || directSubTypes.isEmpty())
        {
            return "";
        }

        return "**Direct Known Subinterfaces:**" + Constants.MARKDOWN_NEW_LINE +
            directSubTypes.stream()
                .filter(typeElement -> configuration.utils.isInterface(typeElement))
                .map(s -> linkBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.JAVADOC).build(element, s))
                .collect(Collectors.joining(", "));
    }

    private String listDirectKnownImplementers(TypeElement element)
    {
        if (!configuration.utils.isInterface(element))
        {
            return "";
        }

        var directSubTypes = typeUniverse.getDirectSubTypes(element);
        if (directSubTypes == null || directSubTypes.isEmpty())
        {
            return "";
        }

        return "**Direct Known Subinterfaces:**" + Constants.MARKDOWN_NEW_LINE +
            directSubTypes.stream()
                .filter(typeElement -> configuration.utils.isClass(typeElement))
                .map(s -> linkBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.JAVADOC).build(element, s))
                .collect(Collectors.joining(", "));
    }

    private String listEnclosingClass(TypeElement element)
    {
        if (!(element.getEnclosingElement() instanceof TypeElement enclosingType))
        {
            return "";
        }

        return "**Enclosing Class:**" + Constants.MARKDOWN_NEW_LINE +
            linkBuilder.withDisplayMode(TypeDisplayNameBuilder.DisplayMode.JAVADOC).build(element, enclosingType);
    }

    private String listFunctionalInterfaceInformation(TypeElement element)
    {
        if (!isFunctionalInterface(element))
        {
            return "";
        }

        return "**Functional Interface:**" + Constants.MARKDOWN_NEW_LINE +
            "This is a functional interface and can therefore be used as the assignment target for a lambda expression or method reference.";
    }

    private String listClassSignature(TypeElement element)
    {
        var htmlWriter = new HtmlDocletWriter(configuration, DocPath.create(path.toString()));
        var content = SignatureUtils.createTypeSignatureAndToContent(element, htmlWriter);
        var builder = new MarkdownAwareContentBuilder();
        builder.add(content);
        return builder.toString();
    }

    private boolean isFunctionalInterface(TypeElement typeElement)
    {
        List<? extends AnnotationMirror> annotationMirrors = typeElement.getAnnotationMirrors();
        for (AnnotationMirror anno : annotationMirrors)
        {
            if (configuration.utils.isFunctionalInterface(anno))
            {
                return true;
            }
        }
        return false;
    }

    private String listDeprecationInformation(TypeElement typeElement)
    {
        List<? extends DeprecatedTree> deprs = utils.getDeprecatedTrees(typeElement);
        if (utils.isDeprecated(typeElement))
        {
            var forRemoval = utils.isDeprecatedForRemoval(typeElement);
            var containerType = forRemoval ? "danger" : "warning";

            var builder = new StringBuilder();
            builder.append("::: ").append(containerType).append(" ");
            builder.append(getDeprecatedPhrase(typeElement, forRemoval)).append(Constants.MARKDOWN_NEW_LINE);

            CommentHelper ch = utils.getCommentHelper(typeElement);
            DocTree dt = deprs.getFirst();
            List<? extends DocTree> commentTags = ch.getBody(dt);
            if (!commentTags.isEmpty())
            {
                var htmlWriter = new HtmlDocletWriter(configuration, DocPath.create(path.toString()));
                var target = new MarkdownAwareContentBuilder();
                htmlWriter.addInlineDeprecatedComment(
                    typeElement,
                    deprs.getFirst(),
                    target
                );
                builder.append(target);
            }
            builder.append("\n");
            builder.append(":::\n");

            return builder.toString();
        }

        return "";
    }

    private String getDeprecatedPhrase(Element e, boolean forRemoval)
    {
        return forRemoval
            ? "**Deprecated, for removal: This API element is subject to removal in a future version.**"
            : "**Deprecated.**";
    }

    private String listClassDescription(TypeElement typeElement)
    {
        if (!options.noComment())
        {
            var tags = utils.getFullBody(typeElement);
            // generate documentation for the class.
            if (!tags.isEmpty())
            {
                var htmlWriter = new MarkdownHtmlDocletWriter(configuration, DocPath.create(path.toString()));
                var content = htmlWriter.commentTagsToContent(typeElement, tags, false, true);
                return content.toString();
            }
        }

        return "";
    }

    private String listClassTags(TypeElement typeElement)
    {
        if (!options.noComment())
        {
            //Custom inner class which gives access to the underlying addTagsInfo method.
            var htmlWriter = new HtmlDocletWriter(configuration, DocPath.create(path.toString()))
            {
                @Override
                public void addTagsInfo(final Element e, final Content content)
                {
                    super.addTagsInfo(e, content);
                }
            };
            var target = new MarkdownAwareContentBuilder();
            htmlWriter.addTagsInfo(typeElement, target);
            return target.toString();
        }

        return "";
    }

    public String listMemberSummary(TypeElement element)
    {
        var content = new NoneEncodingContentBuilder();
        var memberBuilder = new MemberSummaryBuilder(
            configuration,
            element,
            classTree
        );
        memberBuilder.build(content);
        return content.toString();
    }

    public String listEnumConstantsDetails(TypeElement element) throws DocletException
    {
        var context = getContext();
        var innerWriter = new MarkdownSubWriterHolderWriter(configuration, DocPath.create(path.toString()));
        var builder = EnumConstantBuilder.getInstance(context, element, new MarkdownEnumConstantsWriterImpl(innerWriter, element));

        var content = new ContentBuilder();
        builder.build(content);
        return content.toString();
    }

    public String listPropertyDetails(TypeElement element) throws DocletException
    {
        var context = getContext();
        var innerWriter = new MarkdownSubWriterHolderWriter(configuration, DocPath.create(path.toString()));
        var builder = PropertyBuilder.getInstance(context, element, new MarkdownPropertyWriterImpl(innerWriter, element));

        var content = new ContentBuilder();
        builder.build(content);
        return content.toString();
    }

    public String listFieldDetails(TypeElement element) throws DocletException
    {
        var context = getContext();
        var innerWriter = new MarkdownSubWriterHolderWriter(configuration, DocPath.create(path.toString()));
        var builder = FieldBuilder.getInstance(context, element, new MarkdownFieldWriterImpl(innerWriter, element));

        var content = new ContentBuilder();
        builder.build(content);
        return content.toString();
    }

    public String listConstructorDetails(TypeElement element) throws DocletException
    {
        var context = getContext();
        var innerWriter = new MarkdownSubWriterHolderWriter(configuration, DocPath.create(path.toString()));
        var builder = ConstructorBuilder.getInstance(context, element, new MarkdownConstructorWriterImpl(innerWriter, element));

        var content = new ContentBuilder();
        builder.build(content);
        return content.toString();
    }

    public String listAnnotationMemberDetails(TypeElement element) throws DocletException
    {
        var context = getContext();
        var innerWriter = new MarkdownSubWriterHolderWriter(configuration, DocPath.create(path.toString()));
        var builder = AnnotationTypeMemberBuilder.getInstance(context, element, new MarkdownAnnotationTypeMemberWriterImpl(innerWriter, element,
            MarkdownAnnotationTypeMemberWriterImpl.Kind.ANY));

        var content = new ContentBuilder();
        builder.build(content);
        return content.toString();
    }

    public String listMethodMemberDetails(TypeElement element) throws DocletException
    {
        var context = getContext();
        var innerWriter = new MarkdownSubWriterHolderWriter(configuration, DocPath.create(path.toString()));
        var builder = MethodBuilder.getInstance(context, element, new MarkdownMethodWriterImpl(innerWriter, element));

        var content = new ContentBuilder();
        builder.build(content);
        return content.toString();
    }

    public void build() throws IOException
    {
        if (result == null)
        {
            return;
        }

        var path = this.rootPath.resolve(this.path);

        Files.createDirectories(path.getParent());
        Files.writeString(path, result);
    }

    private AbstractBuilder.Context getContext()
    {
        try
        {
            var field = BuilderFactory.class.getDeclaredField("context");
            field.setAccessible(true);
            return (AbstractBuilder.Context) field.get(configuration.getBuilderFactory());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
