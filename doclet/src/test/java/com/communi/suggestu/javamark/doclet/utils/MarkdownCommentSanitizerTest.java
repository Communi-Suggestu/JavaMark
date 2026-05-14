package com.communi.suggestu.javamark.doclet.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownCommentSanitizerTest
{
    @Test
    void stripsBlankLinesInsideDivBlock()
    {
        final String input = "<div> something or another\n\nand this is the rest </div>";

        assertThat(MarkdownCommentSanitizer.stripBlankLinesInsideDivBlocks(input))
            .isEqualTo("<div> something or another\nand this is the rest </div>");
    }

    @Test
    void stripsBlankLinesOutsideDivBlock()
    {
        final String input = "<section>\n\n::: tabs\n";

        assertThat(MarkdownCommentSanitizer.stripBlankLinesInsideDivBlocks(input))
            .isEqualTo("<section>\n::: tabs");
    }

    @Test
    void stripsBlankLinesWithinAndOutsideDivs()
    {
        final String input = "<div>\nouter\n\n<div>\ninner\n\n</div>\n\n</div>\n\nafter";

        assertThat(MarkdownCommentSanitizer.stripBlankLinesInsideDivBlocks(input))
            .isEqualTo("<div>\nouter\n<div>\ninner\n</div>\n</div>\nafter");
    }

    @Test
    void doesNotTreatDivInCodeFenceAsHtml()
    {
        final String input = "```md\n<div>\n\ntext\n</div>\n```\n\n<section>\n\n::: tabs";

        assertThat(MarkdownCommentSanitizer.stripBlankLinesInsideDivBlocks(input))
            .isEqualTo("```md\n<div>\n\ntext\n</div>\n```\n<section>\n::: tabs");
    }
}

