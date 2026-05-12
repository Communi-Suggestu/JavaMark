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
import jdk.javadoc.internal.doclets.formats.html.ClassWriter;
import jdk.javadoc.internal.doclets.formats.html.EnumConstantWriter;
import jdk.javadoc.internal.doclets.formats.html.Table;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyles;
import jdk.javadoc.internal.html.Content;
import jdk.javadoc.internal.html.ContentBuilder;
import jdk.javadoc.internal.html.HtmlStyle;
import jdk.javadoc.internal.html.Text;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class MarkdownEnumConstantsWriterImpl extends EnumConstantWriter
{
    public MarkdownEnumConstantsWriterImpl(final ClassWriter writer)
    {
        super(writer);
    }

    @Override
    public Content getMemberSummaryHeader(final Content content)
    {
        var builder = new ContentBuilder();
        writer.addSummaryHeader(this, builder);
        return new ContainerContent(new ContentBuilder(), builder, ContainerContent.Type.INFO);
    }

    @Override
    public void addSummaryLabel(final Content content)
    {
        content.add(contents.enumConstantSummary).add(Constants.MARKDOWN_NEW_LINE);
    }

    @Override
    protected Table<Element> createSummaryTable()
    {
        return new MarkdownAwareTable<Element>(HtmlStyles.summaryTable)
            .setCaption(contents.getContent("doclet.Enum_Constants"))
            .setHeader(getSummaryTableHeader(typeElement))
            .setColumnStyles(HtmlStyles.colFirst, HtmlStyles.colLast);
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
    public Content getEnumConstantsDetailsHeader(Content content)
    {
        content = new ContentBuilder();
        content.add(contents.enumConstantDetailLabel).add(Constants.MARKDOWN_NEW_LINE);
        return content;
    }

    @Override
    public Content getMemberList()
    {
        return new ContentBuilder();
    }

    @Override
    public Content getEnumConstantsDetails(final Content enumConstantsDetailsHeader, final Content enumConstantsDetails)
    {
        return new ContainerContent(
            enumConstantsDetails,
            enumConstantsDetailsHeader,
            ContainerContent.Type.INFO
        );
    }

    @Override
    public Content getEnumConstantsHeader(final VariableElement enumConstant)
    {
        var body = new NoneEncodingContentBuilder();
        var header = Text.of(name(enumConstant));
        var table = new VitepressTableContent();

        table.addTab(header, body);

        var htmlId = HtmlIdUtils.forMember(enumConstant);
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

    @Override
    public void buildSummary(final Content summariesList, final Content content)
    {
        summariesList.add(content);
    }
}
