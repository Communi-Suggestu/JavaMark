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
import jdk.javadoc.internal.doclets.formats.html.ConstructorWriter;
import jdk.javadoc.internal.doclets.formats.html.Table;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyles;
import jdk.javadoc.internal.html.Content;
import jdk.javadoc.internal.html.ContentBuilder;
import jdk.javadoc.internal.html.HtmlStyle;
import jdk.javadoc.internal.html.Text;
import jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.List;

import static jdk.javadoc.internal.doclets.toolkit.util.VisibleMemberTable.Kind.CONSTRUCTORS;

public class MarkdownConstructorWriterImpl extends ConstructorWriter
{
    private boolean foundNonPubConstructor = false;

    public MarkdownConstructorWriterImpl(final ClassWriter writer)
    {
        super(writer);

        VisibleMemberTable vmt = configuration.getVisibleMemberTable(this.typeElement);
        List<? extends Element> constructors = vmt.getVisibleMembers(CONSTRUCTORS);

        for (Element constructor : constructors) {
            if (utils.isProtected(constructor) || utils.isPrivate(constructor)) {
                foundNonPubConstructor = true;
            }
        }
    }

    @Override
    public void addSummaryLabel(final Content content)
    {
        content.add(contents.constructorSummaryLabel).add(Constants.MARKDOWN_NEW_LINE);
    }

    @Override
    protected Table<Element> createSummaryTable()
    {
        List<HtmlStyle> bodyRowStyles;

        if (foundNonPubConstructor) {
            bodyRowStyles = Arrays.asList(HtmlStyles.colFirst, HtmlStyles.colConstructorName,
                HtmlStyles.colLast);
        } else {
            bodyRowStyles = Arrays.asList(HtmlStyles.colConstructorName, HtmlStyles.colLast);
        }

        return new MarkdownAwareTable<Element>(
            HtmlStyles.summaryTable)
            .setCaption(contents.constructors)
            .setHeader(getSummaryTableHeader(typeElement))
            .setColumnStyles(bodyRowStyles);
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

        return new ContainerContent(
            new NoneEncodingContentBuilder(),
            builder,
            ContainerContent.Type.TIP
        );
    }

    @Override
    public Content getConstructorDetailsHeader(Content content)
    {
        content = new ContentBuilder();
        content.add(contents.constructorDetailsLabel).add(Constants.MARKDOWN_NEW_LINE);
        return content;
    }

    @Override
    public Content getMemberList()
    {
        return new ContentBuilder();
    }

    @Override
    public Content getConstructorDetails(final Content ConstructorDetailsHeader, final Content ConstructorDetails)
    {
        return new ContainerContent(
            ConstructorDetails,
            ConstructorDetailsHeader,
            ContainerContent.Type.INFO
        );
    }

    @Override
    public Content getConstructorHeaderContent(final ExecutableElement member)
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

    @Override
    public void buildSummary(final Content summariesList, final Content content)
    {
        summariesList.add(content);
    }
}
