package com.communi.suggestu.javamark.doclet.writers;

import com.communi.suggestu.javamark.doclet.content.MarkdownAwareContentBuilder;
import com.communi.suggestu.javamark.doclet.content.MarkdownAwareTable;
import com.communi.suggestu.javamark.doclet.utils.Constants;
import com.sun.source.doctree.DocTree;
import jdk.javadoc.internal.doclets.formats.html.HtmlLinkInfo;
import jdk.javadoc.internal.doclets.formats.html.NestedClassWriterImpl;
import jdk.javadoc.internal.doclets.formats.html.SubWriterHolderWriter;
import jdk.javadoc.internal.doclets.formats.html.Table;
import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle;
import jdk.javadoc.internal.doclets.formats.html.markup.Text;
import jdk.javadoc.internal.doclets.toolkit.Content;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.List;

public class MarkdownNestedClassWriterImpl extends NestedClassWriterImpl
{
    public MarkdownNestedClassWriterImpl(final SubWriterHolderWriter writer, final TypeElement typeElement)
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
    public Content getInheritedSummaryHeader(final TypeElement tElement)
    {
        var builder = new ContentBuilder();
        writer.addInheritedSummaryHeader(this, tElement, builder);
        return builder;
    }

    @Override
    public Content getInheritedSummaryLinks()
    {
        return new MarkdownAwareContentBuilder();
    }

    @Override
    public void addSummary(final Content summariesList, final Content content)
    {
        summariesList.add(content);
    }

    @Override
    public void addSummaryLabel(final Content content)
    {
        content.add("### ").add(contents.nestedClassSummary).add(Constants.MARKDOWN_NEW_LINE);
    }

    @Override
    public void addInheritedSummaryLabel(final TypeElement typeElement, final Content content)
    {
        Content classLink = writer.getPreQualifiedClassLink(HtmlLinkInfo.Kind.PLAIN, typeElement);
        Content label;
        if (options.summarizeOverriddenMethods()) {
            label = Text.of(utils.isPlainInterface(typeElement)
                ? resources.getText("doclet.Nested_Classes_Interfaces_Declared_In_Interface")
                : resources.getText("doclet.Nested_Classes_Interfaces_Declared_In_Class"));
        } else {
            label = Text.of(utils.isPlainInterface(typeElement)
                ? resources.getText("doclet.Nested_Classes_Interfaces_Inherited_From_Interface")
                : resources.getText("doclet.Nested_Classes_Interfaces_Inherited_From_Class"));
        }

        content.add("#### ").add(label).add(" - ").add(classLink).add(Constants.MARKDOWN_NEW_LINE);
    }

    @Override
    protected Table<Element> createSummaryTable()
    {
        List<HtmlStyle> bodyRowStyles = Arrays.asList(HtmlStyle.colFirst, HtmlStyle.colSecond,
            HtmlStyle.colLast);

        return new MarkdownAwareTable<Element>(HtmlStyle.summaryTable)
            .setCaption(contents.getContent("doclet.Nested_Classes"))
            .setHeader(getSummaryTableHeader(typeElement))
            .setColumnStyles(bodyRowStyles);
    }
}
