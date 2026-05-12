package com.communi.suggestu.javamark.doclet.writers;

import com.communi.suggestu.javamark.doclet.content.ContainerContent;
import com.communi.suggestu.javamark.doclet.content.ContentWrapper;
import com.communi.suggestu.javamark.doclet.content.MarkdownAwareContentBuilder;
import com.communi.suggestu.javamark.doclet.content.MarkdownAwareTable;
import com.communi.suggestu.javamark.doclet.utils.Constants;
import com.sun.source.doctree.DocTree;
import jdk.javadoc.internal.doclets.formats.html.ClassWriter;
import jdk.javadoc.internal.doclets.formats.html.HtmlLinkInfo;
import jdk.javadoc.internal.doclets.formats.html.NestedClassWriter;
import jdk.javadoc.internal.doclets.formats.html.Table;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyles;
import jdk.javadoc.internal.html.Content;
import jdk.javadoc.internal.html.ContentBuilder;
import jdk.javadoc.internal.html.Entity;
import jdk.javadoc.internal.html.HtmlStyle;
import jdk.javadoc.internal.html.Text;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.List;

public class MarkdownNestedClassWriterImpl extends NestedClassWriter
{
    public MarkdownNestedClassWriterImpl(final ClassWriter writer)
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
    public void addSummaryLabel(final Content content)
    {
        content.add(contents.nestedClassSummary).add(Constants.MARKDOWN_NEW_LINE);
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
        content.add(label).add(Entity.NO_BREAK_SPACE).add(classLink);
    }

    @Override
    protected Table<Element> createSummaryTable()
    {
        List<HtmlStyle> bodyRowStyles = Arrays.asList(HtmlStyles.colFirst, HtmlStyles.colSecond,
            HtmlStyles.colLast);

        return new MarkdownAwareTable<Element>(HtmlStyles.summaryTable)
            .setCaption(contents.getContent("doclet.Nested_Classes"))
            .setHeader(getSummaryTableHeader(typeElement))
            .setColumnStyles(bodyRowStyles);
    }



    @Override
    public void buildSummary(final Content summariesList, final Content content)
    {
        summariesList.add(content);
    }
}
