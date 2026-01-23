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
import jdk.javadoc.internal.doclets.formats.html.MethodWriterImpl;
import jdk.javadoc.internal.doclets.formats.html.SubWriterHolderWriter;
import jdk.javadoc.internal.doclets.formats.html.Table;
import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.formats.html.markup.Entity;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlId;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle;
import jdk.javadoc.internal.doclets.formats.html.markup.Text;
import jdk.javadoc.internal.doclets.toolkit.Content;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class MarkdownMethodWriterImpl extends MethodWriterImpl
{
    public MarkdownMethodWriterImpl(final SubWriterHolderWriter writer, final TypeElement typeElement)
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
        content.add(contents.methodSummary).add(Constants.MARKDOWN_NEW_LINE);
    }

    @Override
    protected Table<Element> createSummaryTable()
    {
        return new MarkdownAwareTable<Element>(HtmlStyle.summaryTable)
            .setHeader(getSummaryTableHeader(typeElement))
            .setColumnStyles(HtmlStyle.colFirst, HtmlStyle.colSecond, HtmlStyle.colLast)
            .setId(HtmlId.of("method-summary-table"))
            .setDefaultTab(contents.getContent("doclet.All_Methods"))
            .addTab(contents.getContent("doclet.Static_Methods"), utils::isStatic)
            .addTab(contents.getContent("doclet.Instance_Methods"), e -> !utils.isStatic(e))
            .addTab(contents.getContent("doclet.Abstract_Methods"), utils::isAbstract)
            .addTab(contents.getContent("doclet.Concrete_Methods"),
                e -> !utils.isAbstract(e) && !utils.isPlainInterface(e.getEnclosingElement()))
            .addTab(contents.getContent("doclet.Default_Methods"), utils::isDefault)
            .addTab(contents.getContent("doclet.Deprecated_Methods"),
                e -> utils.isDeprecated(e) || utils.isDeprecated(typeElement));
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
                ? resources.getText("doclet.Methods_Declared_In_Class")
                : resources.getText("doclet.Methods_Declared_In_Interface"));
        } else {
            label = Text.of(utils.isClass(typeElement)
                ? resources.getText("doclet.Methods_Inherited_From_Class")
                : resources.getText("doclet.Methods_Inherited_From_Interface"));
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
    public Content getMethodDetailsHeader(Content content)
    {
        content = new ContentBuilder();
        content.add(contents.methodDetailLabel).add(Constants.MARKDOWN_NEW_LINE);
        return content;
    }

    @Override
    public Content getMemberList()
    {
        return new ContentBuilder();
    }

    @Override
    public Content getMethodDetails(final Content methodDetailsHeader, final Content methodDetails)
    {
        return new ContainerContent(
            methodDetails,
            methodDetailsHeader,
            ContainerContent.Type.INFO
        );
    }

    @Override
    public Content getMethodHeader(final ExecutableElement method)
    {
        var body = new NoneEncodingContentBuilder();
        var header = Text.of(name(method));
        var table = new VitepressTableContent();

        table.addTab(header, body);

        var htmlId = HtmlIdUtils.forMember(utils, method);
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
