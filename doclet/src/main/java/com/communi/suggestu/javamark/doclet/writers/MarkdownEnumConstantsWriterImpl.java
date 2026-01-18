package com.communi.suggestu.javamark.doclet.writers;

import com.communi.suggestu.javamark.doclet.content.MarkdownAwareContentBuilder;
import com.communi.suggestu.javamark.doclet.content.MarkdownAwareTable;
import com.communi.suggestu.javamark.doclet.utils.Constants;
import jdk.javadoc.internal.doclets.formats.html.EnumConstantWriterImpl;
import jdk.javadoc.internal.doclets.formats.html.SubWriterHolderWriter;
import jdk.javadoc.internal.doclets.formats.html.Table;
import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle;
import jdk.javadoc.internal.doclets.toolkit.Content;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class MarkdownEnumConstantsWriterImpl extends EnumConstantWriterImpl
{
    public MarkdownEnumConstantsWriterImpl(final SubWriterHolderWriter writer, final TypeElement typeElement)
    {
        super(writer, typeElement);
    }

    @Override
    public Content getMemberSummaryHeader(final TypeElement typeElement, final Content content)
    {
        var builder = new ContentBuilder();
        writer.addSummaryHeader(this, builder);
        return builder;
    }

    @Override
    public void addSummary(final Content summariesList, final Content content)
    {
        summariesList.add(content);
    }

    @Override
    public void addSummaryLabel(final Content content)
    {
        content.add("### ").add(contents.enumConstantSummary).add(Constants.MARKDOWN_NEW_LINE);
    }

    @Override
    protected Table<Element> createSummaryTable()
    {
        return new MarkdownAwareTable<Element>(HtmlStyle.summaryTable)
            .setCaption(contents.getContent("doclet.Enum_Constants"))
            .setHeader(getSummaryTableHeader(typeElement))
            .setColumnStyles(HtmlStyle.colFirst, HtmlStyle.colLast);
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
}
