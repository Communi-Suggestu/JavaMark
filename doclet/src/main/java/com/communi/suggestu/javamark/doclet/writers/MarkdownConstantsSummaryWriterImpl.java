package com.communi.suggestu.javamark.doclet.writers;

import com.communi.suggestu.javamark.doclet.content.MarkdownAwareTable;
import com.communi.suggestu.javamark.doclet.content.NoneEncodingContentBuilder;
import com.communi.suggestu.javamark.doclet.content.SectionWrappingContent;
import com.communi.suggestu.javamark.doclet.utils.Constants;
import com.communi.suggestu.javamark.doclet.utils.HtmlIdUtils;
import jdk.javadoc.internal.doclets.formats.html.ConstantsSummaryWriterImpl;
import jdk.javadoc.internal.doclets.formats.html.HtmlConfiguration;
import jdk.javadoc.internal.doclets.formats.html.HtmlIds;
import jdk.javadoc.internal.doclets.formats.html.HtmlLinkInfo;
import jdk.javadoc.internal.doclets.formats.html.Table;
import jdk.javadoc.internal.doclets.formats.html.TableHeader;
import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.formats.html.markup.Entity;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlId;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTree;
import jdk.javadoc.internal.doclets.formats.html.markup.TagName;
import jdk.javadoc.internal.doclets.formats.html.markup.Text;
import jdk.javadoc.internal.doclets.toolkit.Content;
import jdk.javadoc.internal.doclets.toolkit.util.DocFileIOException;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Collection;

public class MarkdownConstantsSummaryWriterImpl extends ConstantsSummaryWriterImpl
{
    private final Content     target = new ContentBuilder();
    private final TableHeader constantsTableHeader;

    /**
     * Construct a ConstantsSummaryWriter.
     *
     * @param configuration the configuration used in this run
     *                      of the standard doclet.
     */
    public MarkdownConstantsSummaryWriterImpl(final HtmlConfiguration configuration)
    {
        super(configuration);
        constantsTableHeader = new TableHeader(
            contents.modifierAndTypeLabel, contents.constantFieldLabel, contents.valueLabel);
    }

    @Override
    public Content getHeader()
    {
        var builder = new NoneEncodingContentBuilder();
        target.add(builder);

        builder.add("# " + getWindowTitle(resources.getText("doclet.Constants_Summary")) + Constants.MARKDOWN_NEW_LINE);
        return builder;
    }

    @Override
    public void addFooter()
    {
        //Noop for now.
    }

    @Override
    public Content getContentsHeader()
    {
        var builder = new NoneEncodingContentBuilder();
        builder.add("## ").add(contents.contentsHeading).add(Constants.MARKDOWN_NEW_LINE);
        return builder;
    }

    @Override
    public void addContentsList(final Content content)
    {
        target.add(content);
    }

    @Override
    public Content getConstantSummaries()
    {
        var summaries = new NoneEncodingContentBuilder();
        target.add(summaries);

        return summaries;
    }

    @Override
    public void addPackageGroup(final String abbrevPackageName, final Content toContent)
    {
        Content headingContent;
        HtmlId anchorName;
        if (abbrevPackageName.isEmpty()) {
            anchorName = HtmlId.of("unnamed-package");
            headingContent = contents.defaultPackageLabel;
        } else {
            anchorName = HtmlId.of(abbrevPackageName);
            headingContent = new ContentBuilder(
                Text.of("## "),
                getPackageLabel(abbrevPackageName),
                Text.of(".*"),
                Text.of(Constants.MARKDOWN_NEW_LINE));
        }
        var section = new SectionWrappingContent(
            anchorName,
            headingContent
        );
        toContent.add(section);
    }

    @Override
    public Content getClassConstantHeader()
    {
        return new NoneEncodingContentBuilder();
    }

    @Override
    public void addConstantMembers(final TypeElement typeElement, final Collection<VariableElement> fields, final Content target)
    {
        //generate links backward only to public classes.
        Content classLink = Text.of(utils.getFullyQualifiedName(typeElement));
        var table = new MarkdownAwareTable<Void>(HtmlStyle.summaryTable)
            .setCaption(classLink)
            .setHeader(constantsTableHeader)
            .setColumnStyles(HtmlStyle.colFirst, HtmlStyle.colSecond, HtmlStyle.colLast);

        for (VariableElement field : fields) {
            table.addRow(getTypeColumn(typeElement, field), getNameColumn(field), getValue(field));
        }
        target.add(table);
    }

    @Override
    public void addClassConstant(final Content fromClassConstant)
    {
        target.add(fromClassConstant);
    }

    @Override
    public void printDocument(final Content content) throws DocFileIOException
    {
        content.add(target);
    }

    /**
     * Get the type column for the constant summary table row.
     *
     * @param member the field to be documented.
     * @return the type column of the constant table row
     */
    private Content getTypeColumn(TypeElement currentTypeElement, VariableElement member) {
        Content typeContent = new ContentBuilder();
        var code = new HtmlTree(TagName.CODE)
            .setId(HtmlIdUtils.forMember(currentTypeElement, member));
        for (Modifier mod : member.getModifiers()) {
            code.add(Text.of(mod.toString()))
                .add(Entity.NO_BREAK_SPACE);
        }
        Content type = getLink(new HtmlLinkInfo(configuration,
            HtmlLinkInfo.Kind.LINK_TYPE_PARAMS_AND_BOUNDS, member.asType()));
        code.add(type);
        typeContent.add(code);
        return typeContent;
    }

    /**
     * Get the name column for the constant summary table row.
     *
     * @param member the field to be documented.
     * @return the name column of the constant table row
     */
    private Content getNameColumn(VariableElement member) {
        Content nameContent = getDocLink(HtmlLinkInfo.Kind.PLAIN,
            member, member.getSimpleName());
        return HtmlTree.CODE(nameContent);
    }

    /**
     * Get the value column for the constant summary table row.
     *
     * @param member the field to be documented.
     * @return the value column of the constant table row
     */
    private Content getValue(VariableElement member) {
        String value = utils.constantValueExpression(member);
        return HtmlTree.CODE(Text.of(value));
    }
}
