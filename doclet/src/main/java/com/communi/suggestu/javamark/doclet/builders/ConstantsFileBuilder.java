package com.communi.suggestu.javamark.doclet.builders;

import com.communi.suggestu.javamark.doclet.utils.Constants;
import com.communi.suggestu.javamark.doclet.writers.MarkdownConstantsSummaryWriterImpl;
import jdk.javadoc.internal.doclets.formats.html.HtmlConfiguration;
import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.toolkit.Content;
import jdk.javadoc.internal.doclets.toolkit.DocletException;
import jdk.javadoc.internal.doclets.toolkit.util.DocFile;
import jdk.javadoc.internal.doclets.toolkit.util.DocPath;
import jdk.javadoc.internal.doclets.toolkit.util.Utils;
import jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.ENUM_CONSTANTS;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.FIELDS;

public class ConstantsFileBuilder
{

    /**
     * The maximum number of package directories shown in the headings of
     * the constant values contents list and headings.
     */
    private static final int MAX_CONSTANT_VALUE_INDEX_LENGTH = 2;

    private final HtmlConfiguration      configuration;
    private final Path                   rootPath;
    private final Path                   path;

    private final Utils       utils;

    /**
     * The set of type elements that have constant fields.
     */
    protected final Set<TypeElement> typeElementsWithConstFields;

    /**
     * The set of package-group headings.
     */
    protected final Set<String> packageGroupHeadings;

    /**
     * The current package being documented.
     */
    private PackageElement currentPackage;

    private final MarkdownConstantsSummaryWriterImpl writer;
    /**
     * The current class being documented.
     */
    private TypeElement currentClass;

    public ConstantsFileBuilder(
        final HtmlConfiguration configuration,
        final Path path)
    {
        this.configuration = configuration;

        DocFile target = DocFile.createFileForOutput(
            configuration,
            DocPath.create("/")
        );

        this.utils = configuration.utils;

        this.rootPath = Path.of(target.getPath());
        this.path = rootPath.relativize(path);

        this.typeElementsWithConstFields = new HashSet<>();
        this.packageGroupHeadings = new TreeSet<>(utils::compareStrings);
        this.writer = new MarkdownConstantsSummaryWriterImpl(configuration);
    }

    public void build() throws DocletException, IOException
    {
        boolean anyConstants = configuration.packages.stream().anyMatch(this::hasConstantField);
        if (!anyConstants) {
            return;
        }

        String builder =
            "--- \n" +
                "title: Constants\n" +
                "aside: false \n" +
                "order: 1000000000 \n" +
                "---\n";

        buildConstantSummary();

        var content = new ContentBuilder();
        writer.printDocument(content);
        builder += Constants.MARKDOWN_NEW_LINE + Constants.MARKDOWN_NEW_LINE + content;

        var path = this.rootPath.resolve(this.path);

        Files.createDirectories(path.getParent());
        Files.writeString(path, builder);
    }

    private void buildConstantSummary() throws DocletException {
        writer.getHeader();

        buildContents();
        buildConstantSummaries();

        writer.addFooter();
    }

    private void buildContents()
    {
        Content contentList = writer.getContentsHeader();
        packageGroupHeadings.clear();
        for (PackageElement pkg : configuration.packages) {
            String abbrevPackageName = getAbbrevPackageName(pkg);
            if (hasConstantField(pkg) && !packageGroupHeadings.contains(abbrevPackageName)) {
                writer.addLinkToPackageContent(abbrevPackageName, contentList);
                packageGroupHeadings.add(abbrevPackageName);
            }
        }
        writer.addContentsList(contentList);
    }

    protected void buildConstantSummaries() throws DocletException
    {
        packageGroupHeadings.clear();
        Content summaries = writer.getConstantSummaries();
        for (PackageElement aPackage : configuration.packages) {
            if (hasConstantField(aPackage)) {
                currentPackage = aPackage;
                //Build the documentation for the current package.
                buildPackageHeader(summaries);
                buildClassConstantSummary();
            }
        }
        writer.addConstantSummaries(summaries);
    }

    private void buildPackageHeader(Content target)
    {
        String abbrevPkgName = getAbbrevPackageName(currentPackage);
        if (!packageGroupHeadings.contains(abbrevPkgName))
        {
            writer.addPackageGroup(abbrevPkgName, target);
            packageGroupHeadings.add(abbrevPkgName);
        }
    }

    /**
     * Builds the summary for the current class.
     *
     * @throws DocletException if there is a problem while building the documentation
     */
    protected void buildClassConstantSummary()
        throws DocletException {
        SortedSet<TypeElement> classes = !currentPackage.isUnnamed()
            ? utils.getAllClasses(currentPackage)
            : configuration.typeElementCatalog.allUnnamedClasses();
        Content classConstantHeader = writer.getClassConstantHeader();
        for (TypeElement te : classes) {
            if (!typeElementsWithConstFields.contains(te) ||
                !utils.isIncluded(te)) {
                continue;
            }
            currentClass = te;
            //Build the documentation for the current class.

            buildConstantMembers(classConstantHeader);

        }
        writer.addClassConstant(classConstantHeader);
    }

    /**
     * Builds the summary of constant members in the class.
     *
     * @param target the content to which the table of constant members will be added
     */
    protected void buildConstantMembers(Content target) {
        new ConstantFieldBuilder(currentClass).buildMembersSummary(target);
    }

    /**
     * {@return the abbreviated name for a package, containing the leading segments of the name}
     *
     * @param pkg the package
     */
    private String getAbbrevPackageName(PackageElement pkg)
    {
        if (pkg.isUnnamed())
        {
            return "";
        }

        String packageName = utils.getPackageName(pkg);
        int index = -1;
        for (int j = 0; j < MAX_CONSTANT_VALUE_INDEX_LENGTH; j++)
        {
            index = packageName.indexOf(".", index + 1);
        }
        return index == -1 ? packageName : packageName.substring(0, index);
    }

    /**
     * {@return true if the given package has constant fields to document}
     *
     * @param pkg the package to be checked
     */
    private boolean hasConstantField(PackageElement pkg)
    {
        SortedSet<TypeElement> classes = !pkg.isUnnamed()
            ? utils.getAllClasses(pkg)
            : configuration.typeElementCatalog.allUnnamedClasses();
        boolean found = false;
        for (TypeElement te : classes)
        {
            if (utils.isIncluded(te) && hasConstantField(te))
            {
                found = true;
            }
        }
        return found;
    }

    /**
     * {@return true if the given class has constant fields to document}
     *
     * @param typeElement the class to be checked
     */
    private boolean hasConstantField(TypeElement typeElement)
    {
        VisibleMemberTable vmt = configuration.getVisibleMemberTable(typeElement);
        List<? extends Element> fields = vmt.getVisibleMembers(FIELDS);
        for (Element f : fields)
        {
            VariableElement field = (VariableElement) f;
            if (field.getConstantValue() != null)
            {
                typeElementsWithConstFields.add(typeElement);
                return true;
            }
        }
        return false;
    }


    /**
     * Builder for the table of fields with constant values.
     */
    private class ConstantFieldBuilder {

        /**
         * The type element that we are examining constants for.
         */
        protected TypeElement typeElement;

        /**
         * Constructs a {@code ConstantFieldBuilder}.
         * @param typeElement the type element that we are examining constants for
         */
        public ConstantFieldBuilder(TypeElement typeElement) {
            this.typeElement = typeElement;
        }

        /**
         * Builds the table of constants for a given class.
         *
         * @param target the content to which the table of class constants will be added
         */
        protected void buildMembersSummary(Content target) {
            SortedSet<VariableElement> members = members();
            if (!members.isEmpty()) {
                writer.addConstantMembers(typeElement, members, target);
            }
        }

        /**
         * {@return a set of visible constant fields for the given type}
         */
        protected SortedSet<VariableElement> members() {
            VisibleMemberTable vmt = configuration.getVisibleMemberTable(typeElement);
            List<Element> members = new ArrayList<>();
            members.addAll(vmt.getVisibleMembers(FIELDS));
            members.addAll(vmt.getVisibleMembers(ENUM_CONSTANTS));
            SortedSet<VariableElement> includes =
                new TreeSet<>(utils.comparators.makeGeneralPurposeComparator());
            for (Element element : members) {
                VariableElement member = (VariableElement)element;
                if (member.getConstantValue() != null) {
                    includes.add(member);
                }
            }
            return includes;
        }
    }
}
