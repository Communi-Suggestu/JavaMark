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
import jdk.javadoc.internal.doclets.formats.html.HtmlLinkInfo;
import jdk.javadoc.internal.doclets.formats.html.PropertyWriterImpl;
import jdk.javadoc.internal.doclets.formats.html.SubWriterHolderWriter;
import jdk.javadoc.internal.doclets.formats.html.Table;
import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.formats.html.markup.Entity;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle;
import jdk.javadoc.internal.doclets.formats.html.markup.Text;
import jdk.javadoc.internal.doclets.toolkit.Content;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class MarkdownPropertyWriterImpl extends PropertyWriterImpl
{
    public MarkdownPropertyWriterImpl(final SubWriterHolderWriter writer, final TypeElement typeElement)
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
        content.add(contents.propertySummaryLabel).add(Constants.MARKDOWN_NEW_LINE);
    }

    @Override
    protected Table<Element> createSummaryTable()
    {
        return new MarkdownAwareTable<Element>(HtmlStyle.summaryTable)
            .setCaption(contents.properties)
            .setHeader(getSummaryTableHeader(typeElement))
            .setColumnStyles(HtmlStyle.colFirst, HtmlStyle.colSecond, HtmlStyle.colLast);
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
                ? resources.getText("doclet.Properties_Declared_In_Class")
                : resources.getText("doclet.Properties_Declared_In_Interface"));
        } else {
            label = Text.of(utils.isClass(typeElement)
                ? resources.getText("doclet.Properties_Inherited_From_Class")
                : resources.getText("doclet.Properties_Inherited_From_Interface"));
        }
        content.add(label).add(Entity.NO_BREAK_SPACE).add(classLink);
    }

    @Override
    public Content getPropertyDetailsHeader(Content content)
    {
        content = new ContentBuilder();
        content.add(contents.propertyDetailsLabel).add(Constants.MARKDOWN_NEW_LINE);
        return content;
    }

    @Override
    public Content getMemberList()
    {
        return new ContentBuilder();
    }

    @Override
    public Content getPropertyDetails(final Content annotationDetailsHeader, final Content annotationDetails)
    {
        return new ContainerContent(
            annotationDetails,
            annotationDetailsHeader,
            ContainerContent.Type.INFO
        );
    }

    @Override
    public Content getPropertyHeaderContent(final ExecutableElement member)
    {
        var body = new NoneEncodingContentBuilder();
        var header = Text.of(name(member));
        var table = new VitepressTableContent();

        table.addTab(header, body);

        var htmlId = HtmlIdUtils.forMember(utils, member);
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
