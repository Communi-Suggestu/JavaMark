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
import jdk.javadoc.internal.doclets.formats.html.FieldWriterImpl;
import jdk.javadoc.internal.doclets.formats.html.HtmlLinkInfo;
import jdk.javadoc.internal.doclets.formats.html.SubWriterHolderWriter;
import jdk.javadoc.internal.doclets.formats.html.Table;
import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.formats.html.markup.Entity;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle;
import jdk.javadoc.internal.doclets.formats.html.markup.Text;
import jdk.javadoc.internal.doclets.toolkit.Content;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Arrays;
import java.util.List;

public class MarkdownFieldWriterImpl extends FieldWriterImpl
{
    public MarkdownFieldWriterImpl(final SubWriterHolderWriter writer, final TypeElement typeElement)
    {
        super(writer, typeElement);
    }

    @Override
    public void addSummary(final Content summariesList, final Content content)
    {
        summariesList.add(content);
    }

    @Override
    public void addSummaryLabel(final Content content)
    {
        content.add(contents.fieldSummaryLabel).add(Constants.MARKDOWN_NEW_LINE);
    }

    @Override
    protected Table<Element> createSummaryTable()
    {
        List<HtmlStyle> bodyRowStyles = Arrays.asList(HtmlStyle.colFirst, HtmlStyle.colSecond,
            HtmlStyle.colLast);

        return new MarkdownAwareTable<Element>(HtmlStyle.summaryTable)
            .setCaption(contents.fields)
            .setHeader(getSummaryTableHeader(typeElement))
            .setColumnStyles(bodyRowStyles);
    }

    @Override
    public Content getInheritedSummaryLinks()
    {
        return new MarkdownAwareContentBuilder();
    }

    @Override
    public void addInheritedSummaryLabel(final TypeElement typeElement, final Content content)
    {
        Content classLink = writer.getPreQualifiedClassLink(
            HtmlLinkInfo.Kind.PLAIN, typeElement);
        Content label;
        if (options.summarizeOverriddenMethods()) {
            label = Text.of(utils.isClass(typeElement)
                ? resources.getText("doclet.Fields_Declared_In_Class")
                : resources.getText("doclet.Fields_Declared_In_Interface"));
        } else {
            label = Text.of(utils.isClass(typeElement)
                ? resources.getText("doclet.Fields_Inherited_From_Class")
                : resources.getText("doclet.Fields_Inherited_From_Interface"));
        }
        content.add(label).add(Entity.NO_BREAK_SPACE).add(classLink);
    }

    @Override
    public Content getInheritedSummaryHeader(final TypeElement tElement)
    {
        var builder = new ContentBuilder();
        writer.addInheritedSummaryHeader(this, tElement, builder);
        return builder;
    }

    @Override
    public Content getFieldDetailsHeader(Content content)
    {
        content = new ContentBuilder();
        content.add(contents.fieldDetailsLabel).add(Constants.MARKDOWN_NEW_LINE);
        return content;
    }

    @Override
    public Content getMemberList()
    {
        return new ContentBuilder();
    }

    @Override
    public Content getFieldDetails(final Content memberDetailsHeaderContent, final Content memberContent)
    {
        return new ContainerContent(
            memberContent,
            memberDetailsHeaderContent,
            ContainerContent.Type.INFO
        );
    }

    @Override
    public Content getFieldHeaderContent(final VariableElement field)
    {
        var body = new NoneEncodingContentBuilder();
        var header = Text.of(name(field));
        var table = new VitepressTableContent();

        table.addTab(header, body);

        var htmlId = HtmlIdUtils.forMember(field);
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
