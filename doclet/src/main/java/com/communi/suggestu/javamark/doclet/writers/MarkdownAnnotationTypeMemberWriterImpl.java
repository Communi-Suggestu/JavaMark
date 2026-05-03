package com.communi.suggestu.javamark.doclet.writers;

import com.communi.suggestu.javamark.doclet.content.MarkdownAwareContentBuilder;
import com.communi.suggestu.javamark.doclet.content.MarkdownAwareTable;
import com.communi.suggestu.javamark.doclet.content.NoneEncodingContentBuilder;
import com.communi.suggestu.javamark.doclet.content.SectionWrappingContent;
import com.communi.suggestu.javamark.doclet.utils.Constants;
import com.communi.suggestu.javamark.doclet.content.ContainerContent;
import com.communi.suggestu.javamark.doclet.content.ContentWrapper;
import com.communi.suggestu.javamark.doclet.utils.HtmlIdUtils;
import com.communi.suggestu.javamark.doclet.content.VitepressTableContent;
import jdk.javadoc.internal.doclets.formats.html.AnnotationTypeMemberWriter;
import jdk.javadoc.internal.doclets.formats.html.ClassWriter;
import jdk.javadoc.internal.doclets.formats.html.Table;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyles;
import jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable;
import jdk.javadoc.internal.html.Content;
import jdk.javadoc.internal.html.ContentBuilder;
import jdk.javadoc.internal.html.HtmlStyle;
import jdk.javadoc.internal.html.Text;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class MarkdownAnnotationTypeMemberWriterImpl extends AnnotationTypeMemberWriter
{
    public enum Kind {
        OPTIONAL,
        REQUIRED,
        ANY
    }

    private final Kind myKind;

    public MarkdownAnnotationTypeMemberWriterImpl(
        final ClassWriter writer,
        final Kind kind)
    {
        super(writer, mapKind(kind));
        this.myKind = kind;
    }

    private static VisibleMemberTable.Kind mapKind(Kind kind)
    {
        return switch (kind) {
            case REQUIRED -> VisibleMemberTable.Kind.ANNOTATION_TYPE_MEMBER_REQUIRED;
            case OPTIONAL -> VisibleMemberTable.Kind.ANNOTATION_TYPE_MEMBER_OPTIONAL;
            case ANY -> VisibleMemberTable.Kind.ANNOTATION_TYPE_MEMBER;
        };
    }

    @Override
    public void addSummaryLabel(final Content content)
    {
        content.add(switch (myKind) {
            case REQUIRED -> contents.annotateTypeRequiredMemberSummaryLabel;
            case OPTIONAL -> contents.annotateTypeOptionalMemberSummaryLabel;
            case ANY -> throw new UnsupportedOperationException("unsupported member kind");
        }).add(Constants.MARKDOWN_NEW_LINE);
    }

    @Override
    protected Table<Element> createSummaryTable()
    {
        return new MarkdownAwareTable<Element>(HtmlStyles.summaryTable)
            .setCaption(getCaption())
            .setHeader(getSummaryTableHeader(typeElement))
            .setColumnStyles(HtmlStyles.colFirst, HtmlStyles.colSecond, HtmlStyles.colLast);
    }

    @Override
    public Content getInheritedSummaryLinks()
    {
        return new MarkdownAwareContentBuilder();
    }


    @Override
    public Content getInheritedSummaryHeader(final TypeElement tElement)
    {
        var builder = new ContentBuilder();
        writer.addInheritedSummaryHeader(this, tElement, builder);
        return builder;
    }

    @Override
    public Content getAnnotationDetailsHeader()
    {
        var content = new ContentBuilder();
        content.add(contents.annotationTypeDetailsLabel).add(Constants.MARKDOWN_NEW_LINE);
        return content;
    }

    @Override
    public Content getMemberList()
    {
        return new ContentBuilder();
    }

    @Override
    public Content getAnnotationDetails(final Content annotationDetailsHeader, final Content annotationDetails)
    {
        return new ContainerContent(
            annotationDetails,
            annotationDetailsHeader,
            ContainerContent.Type.INFO
        );
    }

    @Override
    public Content getAnnotationHeaderContent(final Element member)
    {
        var body = new NoneEncodingContentBuilder();
        var header = Text.of(name(member));
        var table = new VitepressTableContent();

        table.addTab(header, body);

        var htmlId = HtmlIdUtils.forMember(utils, typeElement, (ExecutableElement) member);
        var anchoredTable = new SectionWrappingContent(htmlId, table);

        return new ContentWrapper(body, anchoredTable);
    }

    @Override
    public Content getMemberListItem(final Content memberContent)
    {
        if (!(memberContent instanceof ContentWrapper wrapper))
            return memberContent;

        return wrapper.getWrapper();
    }
}
