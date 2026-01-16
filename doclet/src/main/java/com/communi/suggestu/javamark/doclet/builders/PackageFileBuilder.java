package com.communi.suggestu.javamark.doclet.builders;

import com.communi.suggestu.javamark.doclet.utils.ElementUtils;
import com.communi.suggestu.javamark.doclet.utils.TypeUniverse;
import com.sun.source.util.DocTrees;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class PackageFileBuilder {
    private final Path path;
    private final TypeLinkBuilder typeLinkBuilder;
    private final PackageLinkBuilder packageLinkBuilder;
    private final TypeUniverse typeUniverse;
    private final DocTrees     docTrees;
    private       String       result = null;

    public PackageFileBuilder(Path path, TypeLinkBuilder typeLinkBuilder, PackageLinkBuilder packageLinkBuilder, TypeUniverse typeUniverse, final DocTrees docTrees) {
        this.path = path;
        this.typeLinkBuilder = typeLinkBuilder;
        this.packageLinkBuilder = packageLinkBuilder;
        this.typeUniverse = typeUniverse;
        this.docTrees = docTrees;
    }

    public PackageFileBuilder from(PackageElement pkg) {
        StringBuilder builder = new StringBuilder();
        // 1. Frontmatter
        builder.append("---\n");
        builder.append("title: ").append(pkg.getQualifiedName()).append("\n");
        builder.append("---\n\n");
        // 2. Header
        builder.append("# Package: ").append(pkg.getQualifiedName()).append("\n\n---\n\n");
        // 3. Declaration
        builder.append("```").append("\n");
        var annotations = ElementUtils.getAnnotations(pkg);
        if (!annotations.trim().isBlank())
            builder.append(annotations).append("\n");
        builder.append("package ").append(pkg.getQualifiedName()).append(";\n```\n");
        // 4. Javadoc
        String docComment = ElementUtils.getDocComment(pkg, docTrees);
        if (StringUtils.isNotBlank(docComment)) {
            builder.append(docComment).append("\n\n");
        }
        // 5. Package Contents
        builder.append("### Package Contents\n\n");
        builder.append(buildContentsTabs(pkg));
        // 6. Related Section
        String relatedSection = buildRelatedSection(pkg);
        if (StringUtils.isNotBlank(relatedSection)) {
            builder.append("\n### Related\n\n").append(relatedSection);
        }
        result = builder.toString();
        return this;
    }

    private String buildContentsTabs(PackageElement pkg) {
        List<TypeElement> typesInPkg = typeUniverse.getTypesInPackage(pkg);
        Map<String, List<TypeElement>> categories = categorizeTypes(typesInPkg);
        VitepressTabbedEnvironmentBuilder tabs = new VitepressTabbedEnvironmentBuilder().withKey("PackageContents");
        tabs.addTab("All Types", sb -> sb.append(buildTypeTable(typesInPkg, pkg)));
        tabs.addTab("Interfaces", sb -> sb.append(buildTypeTable(categories.get("Interfaces"), pkg)));
        tabs.addTab("Classes", sb -> sb.append(buildTypeTable(categories.get("Classes"), pkg)));
        tabs.addTab("Enums", sb -> sb.append(buildTypeTable(categories.get("Enums"), pkg)));
        tabs.addTab("Records", sb -> sb.append(buildTypeTable(categories.get("Records"), pkg)));
        return tabs.build();
    }

    private Map<String, List<TypeElement>> categorizeTypes(List<TypeElement> types) {
        Map<String, List<TypeElement>> map = new HashMap<>();
        map.put("Interfaces", types.stream().filter(t -> t.getKind().isInterface() && !ElementUtils.isRecord(t)).collect(Collectors.toList()));
        map.put("Classes", types.stream().filter(t -> t.getKind().isClass() && !ElementUtils.isEnum(t) && !ElementUtils.isRecord(t)).collect(Collectors.toList()));
        map.put("Enums", types.stream().filter(ElementUtils::isEnum).collect(Collectors.toList()));
        map.put("Records", types.stream().filter(ElementUtils::isRecord).collect(Collectors.toList()));
        return map;
    }

    private String buildTypeTable(List<TypeElement> types, PackageElement in) {
        if (types == null || types.isEmpty()) return "No types found.";
        TableBuilder table = new MarkdownTableBuilder().withHeaders("Class", "Description");
        for (TypeElement type : types) {
            String link = typeLinkBuilder.withDisplayMode(TypeLinkBuilder.DisplayMode.SIMPLE_NAME).build(in, type.asType());
            String doc = ElementUtils.getDocComment(type, docTrees);
            if (doc != null && doc.endsWith("\n"))
                doc = doc.substring(0, doc.length() - 1);
            String desc = (doc != null && !doc.isEmpty()) ? doc : "";
            table.addRow(link, desc);
        }
        return table.build();
    }

    private String buildRelatedSection(PackageElement pkg) {
        List<PackageElement> children = typeUniverse.getChildPackages(pkg);
        PackageElement parent = typeUniverse.getParentOf(pkg);
        if (parent == null && children.isEmpty()) return "";
        TableBuilder table = new MarkdownTableBuilder().withHeaders("Package", "Description");
        if (parent != null) {
            String link = packageLinkBuilder.withDisplayMode(PackageLinkBuilder.DisplayMode.FULLY_QUALIFIED_NAME).build(pkg, parent);
            String doc = ElementUtils.getDocComment(parent, docTrees);
            String desc = (doc != null && !doc.isEmpty()) ? doc : "";
            table.addRow(link, desc);
        }
        for (PackageElement child : children) {
            String link = packageLinkBuilder.withDisplayMode(PackageLinkBuilder.DisplayMode.FULLY_QUALIFIED_NAME).build(pkg, child);
            String doc = ElementUtils.getDocComment(child, docTrees);
            String desc = (doc != null && !doc.isEmpty()) ? doc : "";
            table.addRow(link, desc);
        }
        return table.build();
    }

    public void build() throws IOException {
        if (result == null)
            return;
        Files.createDirectories(path.getParent());
        Files.writeString(path, result);
    }
}
