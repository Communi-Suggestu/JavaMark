package com.communi.suggestu.javamark.doclet.writers;

import com.communi.suggestu.javamark.doclet.content.NoneEncodingContentBuilder;
import com.communi.suggestu.javamark.doclet.utils.Constants;
import jdk.javadoc.internal.doclets.formats.html.HtmlConfiguration;
import jdk.javadoc.internal.doclets.toolkit.util.DocFileIOException;
import jdk.javadoc.internal.html.Content;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Collection;

public class MarkdownConstantsSummaryWriterImpl
{
    private final HtmlConfiguration configuration;
    private final Content target = new NoneEncodingContentBuilder();

    public MarkdownConstantsSummaryWriterImpl(final HtmlConfiguration configuration)
    {
        this.configuration = configuration;
    }

    public Content getHeader()
    {
        var builder = new NoneEncodingContentBuilder();
        builder.add("# " + configuration.docResources.getText("doclet.Constants_Summary") + Constants.MARKDOWN_NEW_LINE);
        target.add(builder);
        return builder;
    }

    public void addFooter()
    {
        // No-op.
    }

    public Content getContentsHeader()
    {
        var builder = new NoneEncodingContentBuilder();
        builder.add("## ").add(configuration.contents.contentsHeading).add(Constants.MARKDOWN_NEW_LINE);
        return builder;
    }

    public void addLinkToPackageContent(final String abbrevPackageName, final Content content)
    {
        if (abbrevPackageName.isBlank())
        {
            content.add("- (default)").add(Constants.MARKDOWN_NEW_LINE);
            return;
        }

        content.add("- ").add(abbrevPackageName).add(Constants.MARKDOWN_NEW_LINE);
    }

    public void addContentsList(final Content content)
    {
        target.add(content);
    }

    public Content getConstantSummaries()
    {
        return new NoneEncodingContentBuilder();
    }

    public void addPackageGroup(final String abbrevPackageName, final Content toContent)
    {
        var label = abbrevPackageName.isBlank() ? "default" : abbrevPackageName;
        toContent.add("## ").add(label).add(Constants.MARKDOWN_NEW_LINE);
    }

    public Content getClassConstantHeader()
    {
        return new NoneEncodingContentBuilder();
    }

    public void addConstantMembers(final TypeElement typeElement, final Collection<VariableElement> fields, final Content target)
    {
        target.add("### ").add(typeElement.getQualifiedName().toString()).add(Constants.MARKDOWN_NEW_LINE);
        for (VariableElement field : fields)
        {
            target.add("- `")
                .add(field.getSimpleName().toString())
                .add("` = `")
                .add(String.valueOf(field.getConstantValue()))
                .add("`")
                .add(Constants.MARKDOWN_NEW_LINE);
        }
        target.add(Constants.MARKDOWN_NEW_LINE);
    }

    public void addClassConstant(final Content fromClassConstant)
    {
        target.add(fromClassConstant);
    }

    public void addConstantSummaries(final Content summaries)
    {
        target.add(summaries);
    }

    public void printDocument(final Content content) throws DocFileIOException
    {
        content.add(target);
    }
}
