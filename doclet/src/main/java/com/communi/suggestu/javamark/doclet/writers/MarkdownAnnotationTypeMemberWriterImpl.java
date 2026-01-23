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
import jdk.javadoc.internal.doclets.formats.html.AnnotationTypeMemberWriterImpl;
import jdk.javadoc.internal.doclets.formats.html.SubWriterHolderWriter;
import jdk.javadoc.internal.doclets.formats.html.Table;
import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle;
import jdk.javadoc.internal.doclets.formats.html.markup.Text;
import jdk.javadoc.internal.doclets.toolkit.Content;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class MarkdownAnnotationTypeMemberWriterImpl extends AnnotationTypeMemberWriterImpl
{
    public enum Kind {
        OPTIONAL,
        REQUIRED,
        ANY
    }

    private final Kind myKind;

    public MarkdownAnnotationTypeMemberWriterImpl(
        final SubWriterHolderWriter writer,
        final TypeElement annotationType,
        final Kind kind)
    {
        super(writer);
        this.myKind = kind;
        setKindAndTypeElement(this, kind, annotationType);
    }

    /**
     * Uses reflection to set the superclass's final 'kind' field and the super-superclass's final 'typeElement' field.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setKindAndTypeElement(MarkdownAnnotationTypeMemberWriterImpl instance, Kind kind, TypeElement annotationType) {
        try {
            // Set 'kind' field in AnnotationTypeMemberWriterImpl
            Class<?> superClass = AnnotationTypeMemberWriterImpl.class;
            java.lang.reflect.Field kindField = superClass.getDeclaredField("kind");
            kindField.setAccessible(true);
            // Convert our Kind to the superclass Kind via reflection
            Class<?> superKindClass = kindField.getType();
            Object superKind = Enum.valueOf((Class<Enum>) superKindClass, kind.name());
            kindField.set(instance, superKind);

            // Set 'typeElement' field in AbstractMemberWriter (super-superclass)
            Class<?> abstractMemberWriterClass = superClass.getSuperclass();
            java.lang.reflect.Field typeElementField = abstractMemberWriterClass.getDeclaredField("typeElement");
            typeElementField.setAccessible(true);
            typeElementField.set(instance, annotationType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set superclass fields via reflection", e);
        }
    }

    @Override
    public void addSummary(final Content summariesList, final Content content)
    {
        summariesList.add(content);
    }

    @Override
    public void addSummaryLabel(final Content content)
    {
        content.add(switch (myKind) {
            case REQUIRED -> contents.annotateTypeRequiredMemberSummaryLabel;
            case OPTIONAL -> contents.annotateTypeOptionalMemberSummaryLabel;
            case ANY -> throw new UnsupportedOperationException("unsupported member kind");
        }).add(Constants.MARKDOWN_NEW_LINE);
    }

    @Override
    protected Table<Element> createSummaryTable()
    {
        return new MarkdownAwareTable<Element>(HtmlStyle.summaryTable)
            .setCaption(getCaption())
            .setHeader(getSummaryTableHeader(typeElement))
            .setColumnStyles(HtmlStyle.colFirst, HtmlStyle.colSecond, HtmlStyle.colLast);
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
    public Content getAnnotationDetailsHeader()
    {
        var content = new ContentBuilder();
        content.add(contents.annotationTypeDetailsLabel).add(Constants.MARKDOWN_NEW_LINE);
        return content;
    }

    @Override
    public Content getMemberList()
    {
        return new ContentBuilder();
    }

    @Override
    public Content getAnnotationDetails(final Content annotationDetailsHeader, final Content annotationDetails)
    {
        return new ContainerContent(
            annotationDetails,
            annotationDetailsHeader,
            ContainerContent.Type.INFO
        );
    }

    @Override
    public Content getAnnotationHeaderContent(final Element member)
    {
        var body = new NoneEncodingContentBuilder();
        var header = Text.of(name(member));
        var table = new VitepressTableContent();

        table.addTab(header, body);

        var htmlId = HtmlIdUtils.forMember(utils, typeElement, (ExecutableElement) member);
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
