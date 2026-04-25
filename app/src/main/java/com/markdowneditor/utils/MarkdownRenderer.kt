package com.markdowneditor.utils

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension
import com.vladsch.flexmark.ext.math.MathExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension
import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension
import com.vladsch.flexmark.util.options.MutableDataSet

class MarkdownRenderer {
    private val options = MutableDataSet().apply {
        set(Parser.EXTENSIONS, listOf(
            TablesExtension.create(),
            FootnoteExtension.create(),
            MathExtension.create(),
            StrikethroughExtension.create(),
            TaskListExtension.create(),
            AutolinkExtension.create(),
            AbbreviationExtension.create()
        ))
        // 表格配置
        set(TablesExtension.COLUMN_SPANS, true)
        set(TablesExtension.APPEND_MISSING_COLUMNS, true)
        set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
        set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
        // 数学公式配置
        set(MathExtension.MATH_JAX_REQUIRED, true)
    }
    
    private val parser = Parser.builder(options).build()
    private val renderer = HtmlRenderer.builder(options).build()

    fun render(markdown: String): String {
        val document = parser.parse(markdown)
        return renderer.render(document)
    }
}
