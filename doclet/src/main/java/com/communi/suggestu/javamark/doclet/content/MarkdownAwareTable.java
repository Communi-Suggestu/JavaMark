package com.communi.suggestu.javamark.doclet.content;

import com.communi.suggestu.javamark.doclet.builders.HtmlTableBuilder;
import com.communi.suggestu.javamark.doclet.builders.VitepressTabbedEnvironmentBuilder;
import com.communi.suggestu.javamark.doclet.utils.TableHeaderUtils;
import jdk.javadoc.internal.doclets.formats.html.HtmlIds;
import jdk.javadoc.internal.doclets.formats.html.Table;
import jdk.javadoc.internal.doclets.formats.html.TableHeader;
import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlAttr;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlId;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTree;
import jdk.javadoc.internal.doclets.formats.html.markup.TagName;
import jdk.javadoc.internal.doclets.toolkit.Content;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class MarkdownAwareTable<T> extends Table<T>
{
    /**
     * Creates a builder for an HTML element representing a table.
     *
     * @param tableStyle the style class for the top-level {@code <div>} element
     */
    public MarkdownAwareTable(final HtmlStyle tableStyle)
    {
        super(tableStyle);
        this.tableStyle = tableStyle;
        this.bodyRows = new ArrayList<>();
    }

    /**
     * A record containing the data for a table tab.
     */
    record MarkdownTab<T>(
        Content label,
        Predicate<T> predicate,
        List<List<Content>> rows,
        int index) {}

    @Override
    public boolean write(Writer out, String newline, boolean atNewline) throws IOException
    {
        return toContent().write(out, newline, atNewline);
    }

    private final HtmlStyle            tableStyle;
    private       Content              caption;
    private       List<MarkdownTab<T>> tabs;
    private       Set<MarkdownTab<T>>  occurringTabs;
    private       Content              defaultTab;
    private       boolean              renderTabs           = true;
    private       int                  columnCount          = -1;
    private       TableHeader          header;
    private final List<List<Content>>        bodyRows;
    private       HtmlId               id;
    private       boolean              alwaysShowDefaultTab = false;

    /**
     * A record containing the data for a table tab.
     */
    record Tab<T>(
        Content label,
        Predicate<T> predicate,
        int index) {}

    /**
     * Sets the caption for the table.
     * This is ignored if the table is configured to provide tabs to select
     * different subsets of rows within the table.
     *
     * @param captionContent the caption
     * @return this object
     */
    public Table<T> setCaption(Content captionContent)
    {
        caption = getCaption(captionContent);
        return this;
    }

    /**
     * Adds a tab to the table.
     * Tabs provide a way to display subsets of rows, as determined by a
     * predicate for the tab, and an item associated with each row.
     * Tabs will appear left-to-right in the order they are added.
     *
     * @param label     the tab label
     * @param predicate the predicate
     * @return this object
     */
    public Table<T> addTab(Content label, Predicate<T> predicate)
    {
        if (tabs == null)
        {
            tabs = new ArrayList<>();         // preserves order that tabs are added
            occurringTabs = new HashSet<>();  // order not significant
        }
        // Use current size of tabs list as id so we have tab ids that are consistent
        // across tables with the same tabs but different content.
        tabs.add(new MarkdownTab<>(label, predicate, new ArrayList<>(), tabs.size() + 1));
        return this;
    }

    /**
     * Sets the label for the default tab, which displays all the rows in the table.
     * This tab will appear first in the left-to-right list of displayed tabs.
     *
     * @param label the default tab label
     * @return this object
     */
    public Table<T> setDefaultTab(Content label)
    {
        defaultTab = label;
        return this;
    }

    /**
     * Sets whether to display the default tab even if tabs are empty or only contain a single tab.
     *
     * @param showDefaultTab true if default tab should always be shown
     * @return this object
     */
    public Table<T> setAlwaysShowDefaultTab(boolean showDefaultTab)
    {
        this.alwaysShowDefaultTab = showDefaultTab;
        return this;
    }

    /**
     * Allows to set whether tabs should be rendered for this table. Some pages use their
     * own controls to select table categories, in which case the tabs are omitted.
     *
     * @param renderTabs true if table tabs should be rendered
     * @return this object
     */
    public Table<T> setRenderTabs(boolean renderTabs)
    {
        this.renderTabs = renderTabs;
        return this;
    }

    /**
     * Sets the header for the table.
     *
     * <p>Notes:
     * <ul>
     * <li>The column styles are not currently applied to the header, but probably should, eventually
     * </ul>
     *
     * @param header the header
     * @return this object
     */
    public Table<T> setHeader(TableHeader header)
    {
        this.header = header;
        return this;
    }

    /**
     * Sets the styles for be used for the cells in each row.
     *
     * <p>Note:
     * <ul>
     * <li>The column styles are not currently applied to the header, but probably should, eventually
     * </ul>
     *
     * @param styles the styles
     * @return this object
     */
    public Table<T> setColumnStyles(HtmlStyle... styles)
    {
        return setColumnStyles(Arrays.asList(styles));
    }

    /**
     * Sets the styles for be used for the cells in each row.
     *
     * <p>Note:
     * <ul>
     * <li>The column styles are not currently applied to the header, but probably should, eventually
     * </ul>
     *
     * @param styles the styles
     * @return this object
     */
    public Table<T> setColumnStyles(List<HtmlStyle> styles)
    {
        columnCount = styles.size();
        return this;
    }

    /**
     * Sets the style for the table's grid which controls allocation of space among table columns.
     * The style should contain a {@code display: grid;} property and its number of columns must
     * match the number of column styles and content passed to other methods in this class.
     *
     * @param gridStyle the grid style
     * @return this object
     */
    public Table<T> setGridStyle(HtmlStyle gridStyle)
    {
        return this;
    }

    /**
     * Sets the id attribute of the table.
     * This is required if the table has tabs, in which case a subsidiary id
     * will be generated for the tabpanel. This subsidiary id is required for
     * the ARIA support.
     *
     * @param id the id
     * @return this object
     */
    public Table<T> setId(HtmlId id)
    {
        this.id = id;
        return this;
    }

    /**
     * Adds a row of data to the table.
     * Each item of content should be suitable for use as the content of a
     * {@code <th>} or {@code <td>} cell.
     * This method should not be used when the table has tabs: use a method
     * that takes an {@code Element} parameter instead.
     *
     * @param contents the contents for the row
     */
    public void addRow(Content... contents)
    {
        addRow(null, Arrays.asList(contents));
    }

    /**
     * Adds a row of data to the table.
     * Each item of content should be suitable for use as the content of a
     * {@code <th>} or {@code <td> cell}.
     * This method should not be used when the table has tabs: use a method
     * that takes an {@code item} parameter instead.
     *
     * @param contents the contents for the row
     */
    public void addRow(List<Content> contents)
    {
        addRow(null, contents);
    }

    /**
     * Adds a row of data to the table.
     * Each item of content should be suitable for use as the content of a
     * {@code <th>} or {@code <td>} cell.
     * <p>
     * If tabs have been added to the table, the specified item will be used
     * to determine whether the row should be displayed when any particular tab
     * is selected, using the predicate specified when the tab was
     * {@link #addTab(Content, Predicate) added}.
     *
     * @param item     the item
     * @param contents the contents for the row
     * @throws NullPointerException if tabs have previously been added to the table
     *                              and {@code item} is null
     */
    public void addRow(T item, Content... contents)
    {
        addRow(item, Arrays.asList(contents));
    }

    /**
     * Adds a row of data to the table.
     * Each item of content should be suitable for use as the content of a
     * {@code <div>} cell.
     * <p>
     * If tabs have been added to the table, the specified item will be used
     * to determine whether the row should be displayed when any particular tab
     * is selected, using the predicate specified when the tab was
     * {@link #addTab(Content, Predicate) added}.
     *
     * @param item     the item
     * @param contents the contents for the row
     * @throws NullPointerException if tabs have previously been added to the table
     *                              and {@code item} is null
     */
    public void addRow(T item, List<Content> contents)
    {
        if (tabs != null && item == null)
        {
            throw new NullPointerException();
        }
        if (contents.size() != columnCount)
        {
            throw new IllegalArgumentException("row content size does not match number of columns");
        }

        if (tabs != null) {
            for (var tab : tabs) {
                if (tab.predicate().test(item)) {
                    occurringTabs.add(tab);
                    tab.rows.add(contents);
                }
            }
        } else {
            bodyRows.add(contents);
        }
    }

    private HtmlTree getCaption(Content title)
    {
        return HtmlTree.DIV(HtmlStyle.caption, HtmlTree.SPAN(title));
    }

    private Content toContent()
    {
        Content main = new ContentBuilder();
        if ((tabs == null || occurringTabs.size() == 1) && !alwaysShowDefaultTab)
        {
            if (tabs == null)
            {
                main.add(caption);
            }
            else
            {
                main.add(getCaption(occurringTabs.iterator().next().label()));
            }

            main.add(new NoneEncodingContentBuilder().add(createTable(this.bodyRows)));
        }
        else
        {
            var tabs = new VitepressTabbedEnvironmentBuilder();

            if (this.id != null)
                tabs = tabs.withKey(this.id.name());

            for (final MarkdownTab<T> tab : this.tabs)
            {
                tabs.addTab(
                    tab.label().toString(),
                    createTable(tab.rows())
                );
            }

            main.add(new NoneEncodingContentBuilder().add(tabs.build()));
        }
        return main;
    }

    private String createTable(List<List<Content>> bodyRows)
    {
        var table = new HtmlTableBuilder();
        table.withHeaders(
            TableHeaderUtils.extractCellContents(header)
                .stream()
                .map(Content::toString)
                .toList()
        );

        for (final List<Content> bodyRow : bodyRows)
        {
            table.addRow(
                bodyRow.stream()
                    .map(Content::toString)
                    .toList()
            );
        }
        return table.build();
    }
}
