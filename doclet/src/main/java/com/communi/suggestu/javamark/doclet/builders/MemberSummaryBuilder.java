package com.communi.suggestu.javamark.doclet.builders;

import com.communi.suggestu.javamark.doclet.utils.Constants;
import com.communi.suggestu.javamark.doclet.writers.MarkdownEnumConstantsWriterImpl;
import com.communi.suggestu.javamark.doclet.writers.MarkdownNestedClassWriterImpl;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import jdk.javadoc.internal.doclets.formats.html.ClassWriterImpl;
import jdk.javadoc.internal.doclets.formats.html.HtmlConfiguration;
import jdk.javadoc.internal.doclets.toolkit.BaseConfiguration;
import jdk.javadoc.internal.doclets.toolkit.Content;
import jdk.javadoc.internal.doclets.toolkit.MemberSummaryWriter;
import jdk.javadoc.internal.doclets.toolkit.builders.AbstractMemberBuilder;
import jdk.javadoc.internal.doclets.toolkit.util.ClassTree;
import jdk.javadoc.internal.doclets.toolkit.util.DocFinder;
import jdk.javadoc.internal.doclets.toolkit.util.Utils;
import jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.ANNOTATION_TYPE_MEMBER_OPTIONAL;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.ANNOTATION_TYPE_MEMBER_REQUIRED;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.CONSTRUCTORS;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.ENUM_CONSTANTS;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.FIELDS;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.METHODS;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.NESTED_CLASSES;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.PROPERTIES;

public class MemberSummaryBuilder extends AbstractMemberBuilder
{
    private final Comparator<Element>                                   comparator;
    private final EnumMap<VisibleMemberTable.Kind, MemberSummaryWriter> memberSummaryWriters;
    private final PropertyHelper                                        propertyHelper;

    public MemberSummaryBuilder(
        final HtmlConfiguration configuration,
        final TypeElement typeElement,
        final ClassTree classTree
    )
    {
        super(createContext(configuration), typeElement);

        memberSummaryWriters = new EnumMap<>(VisibleMemberTable.Kind.class);
        comparator = utils.comparators.makeIndexElementComparator();

        var classWriter = new ClassWriterImpl(configuration, typeElement, classTree);
        for (VisibleMemberTable.Kind kind : VisibleMemberTable.Kind.values())
        {
            memberSummaryWriters.put(
                kind,
                switch (kind) {
                    case NESTED_CLASSES -> new MarkdownNestedClassWriterImpl(classWriter, typeElement);
                    case ENUM_CONSTANTS -> new MarkdownEnumConstantsWriterImpl(classWriter, typeElement);
                    default -> null;
                }
            );
        }

        this.propertyHelper = new PropertyHelper(this);
    }

    public VisibleMemberTable getVisibleMemberTable()
    {
        return visibleMemberTable;
    }

    @Override
    public void build(final Content target)
    {
        buildPropertiesSummary(target);
        buildNestedClassesSummary(target);
        buildEnumConstantsSummary(target);
        buildAnnotationTypeRequiredMemberSummary(target);
        buildAnnotationTypeOptionalMemberSummary(target);
        buildFieldsSummary(target);
        buildConstructorsSummary(target);
        buildMethodsSummary(target);
    }

    @Override
    public boolean hasMembersToDocument()
    {
        return visibleMemberTable.hasVisibleMembers();
    }

    /**
     * Builds the summary for any optional members of an annotation type.
     *
     * @param summariesList the list of summaries to which the summary will be added
     */
    protected void buildAnnotationTypeOptionalMemberSummary(Content summariesList)
    {
        MemberSummaryWriter writer = memberSummaryWriters.get(ANNOTATION_TYPE_MEMBER_OPTIONAL);
        addSummary(writer, ANNOTATION_TYPE_MEMBER_OPTIONAL, false, summariesList);
    }

    /**
     * Builds the summary for any required members of an annotation type.
     *
     * @param summariesList the list of summaries to which the summary will be added
     */
    protected void buildAnnotationTypeRequiredMemberSummary(Content summariesList)
    {
        MemberSummaryWriter writer = memberSummaryWriters.get(ANNOTATION_TYPE_MEMBER_REQUIRED);
        addSummary(writer, ANNOTATION_TYPE_MEMBER_REQUIRED, false, summariesList);
    }

    /**
     * Builds the summary for any enum constants of an enum type.
     *
     * @param summariesList the list of summaries to which the summary will be added
     */
    protected void buildEnumConstantsSummary(Content summariesList)
    {
        MemberSummaryWriter writer = memberSummaryWriters.get(ENUM_CONSTANTS);
        addSummary(writer, ENUM_CONSTANTS, false, summariesList);
    }

    /**
     * Builds the summary for any fields.
     *
     * @param summariesList the list of summaries to which the summary will be added
     */
    protected void buildFieldsSummary(Content summariesList)
    {
        MemberSummaryWriter writer = memberSummaryWriters.get(FIELDS);
        addSummary(writer, FIELDS, true, summariesList);
    }

    /**
     * Builds the summary for any properties.
     *
     * @param summariesList the list of summaries to which the summary will be added
     */
    protected void buildPropertiesSummary(Content summariesList)
    {
        MemberSummaryWriter writer = memberSummaryWriters.get(PROPERTIES);
        addSummary(writer, PROPERTIES, true, summariesList);
    }

    /**
     * Builds the summary for any nested classes.
     *
     * @param summariesList the list of summaries to which the summary will be added
     */
    protected void buildNestedClassesSummary(Content summariesList)
    {
        MemberSummaryWriter writer = memberSummaryWriters.get(NESTED_CLASSES);
        addSummary(writer, NESTED_CLASSES, true, summariesList);
    }

    /**
     * Builds the summary for any methods.
     *
     * @param summariesList the content to which the documentation will be added
     */
    protected void buildMethodsSummary(Content summariesList)
    {
        MemberSummaryWriter writer = memberSummaryWriters.get(METHODS);
        addSummary(writer, METHODS, true, summariesList);
    }

    /**
     * Builds the summary for any constructors.
     *
     * @param summariesList the content to which the documentation will be added
     */
    protected void buildConstructorsSummary(Content summariesList)
    {
        MemberSummaryWriter writer = memberSummaryWriters.get(CONSTRUCTORS);
        addSummary(writer, CONSTRUCTORS, false, summariesList);
    }

    private SortedSet<? extends Element> asSortedSet(Collection<? extends Element> members)
    {
        SortedSet<Element> out = new TreeSet<>(comparator);
        out.addAll(members);
        return out;
    }

    /**
     * Adds the summary for the documentation.
     *
     * @param writer               the writer for this member summary
     * @param kind                 the kind of members to document
     * @param showInheritedSummary true if a summary of any inherited elements should be documented
     * @param summariesList        the list of summaries to which the summary will be added
     */
    private void addSummary(
        MemberSummaryWriter writer,
        VisibleMemberTable.Kind kind,
        boolean showInheritedSummary,
        Content summariesList)
    {
        if (writer == null)
            return;

        LinkedList<Content> summaryTreeList = new LinkedList<>();
        buildSummary(writer, kind, summaryTreeList);
        if (showInheritedSummary)
        {
            buildInheritedSummary(writer, kind, summaryTreeList);
        }
        if (!summaryTreeList.isEmpty())
        {
            Content member = writer.getMemberSummaryHeader(typeElement, summariesList);
            summaryTreeList.forEach(member::add);
            writer.addSummary(summariesList, member);
            summariesList.add(Constants.MARKDOWN_NEW_LINE).add(Constants.MARKDOWN_NEW_LINE);
        }
    }

    /**
     * Build the inherited member summary for the given methods.
     *
     * @param writer  the writer for this member summary.
     * @param kind    the kind of members to document.
     * @param targets the list of contents to which the documentation will be added
     */
    private void buildInheritedSummary(
        MemberSummaryWriter writer,
        VisibleMemberTable.Kind kind, LinkedList<Content> targets)
    {
        VisibleMemberTable visibleMemberTable = getVisibleMemberTable();
        SortedSet<? extends Element> inheritedMembersFromMap = asSortedSet(visibleMemberTable.getAllVisibleMembers(kind));

        for (TypeElement inheritedClass : visibleMemberTable.getVisibleTypeElements())
        {
            if (!(utils.isPublic(inheritedClass) || utils.isLinkable(inheritedClass)))
            {
                continue;
            }
            if (Objects.equals(inheritedClass, typeElement))
            {
                continue;
            }
            if (utils.hasHiddenTag(inheritedClass))
            {
                continue;
            }

            List<? extends Element> members = inheritedMembersFromMap.stream()
                .filter(e -> Objects.equals(utils.getEnclosingTypeElement(e), inheritedClass))
                .toList();

            if (!members.isEmpty())
            {
                SortedSet<Element> inheritedMembers = new TreeSet<>(comparator);
                inheritedMembers.addAll(members);
                Content inheritedHeader = writer.getInheritedSummaryHeader(inheritedClass);
                Content links = writer.getInheritedSummaryLinks();
                addSummaryFootNote(inheritedClass, inheritedMembers, links, writer);
                inheritedHeader.add(links);
                targets.add(inheritedHeader);
            }
        }
    }

    private void addSummaryFootNote(
        TypeElement inheritedClass, Iterable<Element> inheritedMembers,
        Content links, MemberSummaryWriter writer)
    {
        boolean isFirst = true;
        for (var iterator = inheritedMembers.iterator(); iterator.hasNext(); )
        {
            var member = iterator.next();
            TypeElement t = utils.isUndocumentedEnclosure(inheritedClass)
                ? typeElement : inheritedClass;
            writer.addInheritedMemberSummary(t, member, isFirst, !iterator.hasNext(), links);
            isFirst = false;
        }
    }

    /**
     * Build the member summary for the given members.
     *
     * @param writer          the summary writer to write the output.
     * @param kind            the kind of  members to summarize.
     * @param summaryTreeList the list of contents to which the documentation will be added
     */
    private void buildSummary(
        MemberSummaryWriter writer,
        VisibleMemberTable.Kind kind,
        LinkedList<Content> summaryTreeList)
    {
        SortedSet<? extends Element> members = asSortedSet(getVisibleMembers(kind));
        if (!members.isEmpty())
        {
            for (Element member : members)
            {
                final Element property = propertyHelper.getPropertyElement(member);
                if (property != null && member instanceof ExecutableElement ee)
                {
                    configuration.cmtUtils.updatePropertyMethodComment(ee, property);
                }
                if (utils.isMethod(member))
                {
                    var docFinder = utils.docFinder();
                    Optional<List<? extends DocTree>> r = docFinder.search((ExecutableElement) member, (m -> {
                        var firstSentenceTrees = utils.getFirstSentenceTrees(m);
                        Optional<List<? extends DocTree>> optional = firstSentenceTrees.isEmpty() ? Optional.empty() : Optional.of(firstSentenceTrees);
                        return DocFinder.Result.fromOptional(optional);
                    })).toOptional();
                    // The fact that we use `member` for possibly unrelated tags is suspicious
                    writer.addMemberSummary(typeElement, member, r.orElse(List.of()));
                }
                else
                {
                    writer.addMemberSummary(typeElement, member, utils.getFirstSentenceTrees(member));
                }
            }
            summaryTreeList.add(writer.getSummaryTable(typeElement));
        }
    }

    /**
     * Creates an AbstractBuilder.Context using the given HtmlConfiguration and an empty Set of PackageElement,
     * using reflection to bypass package-private constructor access.
     *
     * @param configuration the HtmlConfiguration to use
     * @return a new AbstractBuilder.Context instance
     */
    private static Context createContext(HtmlConfiguration configuration)
    {
        try
        {
            Class<?> contextClass = Class.forName("jdk.javadoc.internal.doclets.toolkit.builders.AbstractBuilder$Context");
            var ctor = contextClass.getDeclaredConstructor(BaseConfiguration.class, Set.class);
            ctor.setAccessible(true);
            Object ctx = ctor.newInstance(configuration, Set.of());
            return (Context) ctx;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to create AbstractBuilder.Context via reflection", e);
        }
    }

    private static class PropertyHelper
    {

        private final Map<Element, Element> classPropertiesMap = new HashMap<>();

        private final MemberSummaryBuilder builder;

        PropertyHelper(MemberSummaryBuilder builder)
        {
            this.builder = builder;
            computeProperties();
        }

        private void computeProperties()
        {
            VisibleMemberTable vmt = builder.getVisibleMemberTable();
            List<ExecutableElement> props = ElementFilter.methodsIn(vmt.getVisibleMembers(PROPERTIES));
            for (ExecutableElement propertyMethod : props)
            {
                ExecutableElement getter = vmt.getPropertyGetter(propertyMethod);
                ExecutableElement setter = vmt.getPropertySetter(propertyMethod);
                VariableElement field = vmt.getPropertyField(propertyMethod);

                addToPropertiesMap(propertyMethod, field, getter, setter);
            }
        }

        private void addToPropertiesMap(
            ExecutableElement propertyMethod,
            VariableElement field,
            ExecutableElement getter,
            ExecutableElement setter)
        {
            // determine the preferred element from which to derive the property description
            Element e = field == null || !builder.utils.hasDocCommentTree(field)
                ? propertyMethod : field;

            if (e == field && builder.utils.hasDocCommentTree(propertyMethod))
            {
                BaseConfiguration configuration = builder.configuration;
                configuration.getReporter().print(Diagnostic.Kind.WARNING,
                    propertyMethod, configuration.getDocResources().getText("doclet.duplicate.comment.for.property"));
            }

            addToPropertiesMap(propertyMethod, e);
            addToPropertiesMap(getter, e);
            addToPropertiesMap(setter, e);
        }

        private void addToPropertiesMap(
            Element propertyMethod,
            Element commentSource)
        {
            Objects.requireNonNull(commentSource);
            if (propertyMethod == null)
            {
                return;
            }

            Utils utils = builder.utils;
            DocCommentTree docTree = utils.hasDocCommentTree(propertyMethod)
                ? utils.getDocCommentTree(propertyMethod)
                : null;

            /* The second condition is required for the property buckets. In
             * this case the comment is at the property method (not at the field)
             * and it needs to be listed in the map.
             */
            if ((docTree == null) || propertyMethod.equals(commentSource))
            {
                classPropertiesMap.put(propertyMethod, commentSource);
            }
        }

        /**
         * Returns the element for the property documentation belonging to the given member.
         *
         * @param element the member for which the property documentation is needed.
         * @return the element for the property documentation, null if there is none.
         */
        public Element getPropertyElement(Element element)
        {
            return classPropertiesMap.get(element);
        }
    }
}
