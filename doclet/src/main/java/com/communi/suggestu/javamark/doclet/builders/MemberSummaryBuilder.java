package com.communi.suggestu.javamark.doclet.builders;

import com.communi.suggestu.javamark.doclet.utils.Constants;
import com.communi.suggestu.javamark.doclet.writers.MarkdownAnnotationTypeMemberWriterImpl;
import com.communi.suggestu.javamark.doclet.writers.MarkdownConstructorWriterImpl;
import com.communi.suggestu.javamark.doclet.writers.MarkdownEnumConstantsWriterImpl;
import com.communi.suggestu.javamark.doclet.writers.MarkdownFieldWriterImpl;
import com.communi.suggestu.javamark.doclet.writers.MarkdownMethodWriterImpl;
import com.communi.suggestu.javamark.doclet.writers.MarkdownNestedClassWriterImpl;
import com.communi.suggestu.javamark.doclet.writers.MarkdownPropertyWriterImpl;
import jdk.javadoc.internal.doclets.formats.html.AbstractMemberWriter;
import jdk.javadoc.internal.doclets.formats.html.ClassWriter;
import jdk.javadoc.internal.doclets.formats.html.HtmlConfiguration;
import jdk.javadoc.internal.doclets.toolkit.util.ClassTree;
import jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable;
import jdk.javadoc.internal.html.Content;

import javax.lang.model.element.TypeElement;
import java.util.EnumMap;

import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.ANNOTATION_TYPE_MEMBER_OPTIONAL;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.ANNOTATION_TYPE_MEMBER_REQUIRED;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.CONSTRUCTORS;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.ENUM_CONSTANTS;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.FIELDS;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.METHODS;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.NESTED_CLASSES;
import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.PROPERTIES;

public class MemberSummaryBuilder
{
    private final VisibleMemberTable                                visibleMemberTable;
    private final EnumMap<VisibleMemberTable.Kind, AbstractMemberWriter> summaryWriters;

    public MemberSummaryBuilder(
        final HtmlConfiguration configuration,
        final TypeElement typeElement,
        final ClassTree classTree
    )
    {
        this.visibleMemberTable = configuration.getVisibleMemberTable(typeElement);
        this.summaryWriters = new EnumMap<>(VisibleMemberTable.Kind.class);

        var classWriter = new ClassWriter(configuration, typeElement, classTree);
        addWriterIfVisible(NESTED_CLASSES, new MarkdownNestedClassWriterImpl(classWriter));
        addWriterIfVisible(ENUM_CONSTANTS, new MarkdownEnumConstantsWriterImpl(classWriter));
        addWriterIfVisible(FIELDS, new MarkdownFieldWriterImpl(classWriter));
        addWriterIfVisible(CONSTRUCTORS, new MarkdownConstructorWriterImpl(classWriter));
        addWriterIfVisible(METHODS, new MarkdownMethodWriterImpl(classWriter));
        addWriterIfVisible(ANNOTATION_TYPE_MEMBER_REQUIRED,
            new MarkdownAnnotationTypeMemberWriterImpl(classWriter, MarkdownAnnotationTypeMemberWriterImpl.Kind.REQUIRED));
        addWriterIfVisible(ANNOTATION_TYPE_MEMBER_OPTIONAL,
            new MarkdownAnnotationTypeMemberWriterImpl(classWriter, MarkdownAnnotationTypeMemberWriterImpl.Kind.OPTIONAL));
        addWriterIfVisible(PROPERTIES, new MarkdownPropertyWriterImpl(classWriter));
    }

    private void addWriterIfVisible(VisibleMemberTable.Kind kind, AbstractMemberWriter writer)
    {
        if (visibleMemberTable.hasVisibleMembers(kind))
        {
            summaryWriters.put(kind, writer);
        }
    }

    public void build(final Content target)
    {
        addSummary(PROPERTIES, target);
        addSummary(NESTED_CLASSES, target);
        addSummary(ENUM_CONSTANTS, target);
        addSummary(ANNOTATION_TYPE_MEMBER_REQUIRED, target);
        addSummary(ANNOTATION_TYPE_MEMBER_OPTIONAL, target);
        addSummary(FIELDS, target);
        addSummary(CONSTRUCTORS, target);
        addSummary(METHODS, target);
    }

    public boolean hasMembersToDocument()
    {
        return visibleMemberTable.hasVisibleMembers();
    }

    private void addSummary(VisibleMemberTable.Kind kind, Content target)
    {
        var writer = summaryWriters.get(kind);
        if (writer == null)
        {
            return;
        }

        writer.buildSummary(target);
        target.add(Constants.MARKDOWN_NEW_LINE).add(Constants.MARKDOWN_NEW_LINE);
    }
}
