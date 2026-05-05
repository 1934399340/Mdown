package com.markdowneditor.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

object HtmlToMarkdown {

    fun convert(html: String): String {
        // Replace non-breaking spaces with regular spaces for cleaner output
        val cleaned = html.replace(" ", " ").replace("&nbsp;", " ")
        if (cleaned.isBlank()) return ""
        val doc = Jsoup.parseBodyFragment(cleaned)
        val body = doc.body()
        return convertChildren(body).trimEnd()
    }

    private fun convertChildren(element: Element): String {
        val sb = StringBuilder()
        for (node in element.childNodes()) {
            sb.append(convertNode(node))
        }
        return sb.toString()
    }

    private fun convertNode(node: Node): String {
        return when (node) {
            is TextNode -> node.text()
            is Element -> convertElement(node)
            else -> ""
        }
    }

    private fun convertElement(el: Element): String {
        val inner = convertChildren(el)
        return when (el.tagName().lowercase()) {
            "h1" -> "# $inner\n\n"
            "h2" -> "## $inner\n\n"
            "h3" -> "### $inner\n\n"
            "h4" -> "#### $inner\n\n"
            "h5" -> "##### $inner\n\n"
            "h6" -> "###### $inner\n\n"
            "p" -> "$inner\n\n"
            "br" -> "\n"
            "strong", "b" -> "**$inner**"
            "em", "i" -> "*$inner*"
            "del", "s" -> "~~$inner~~"
            "code" -> {
                if (el.parent()?.tagName()?.lowercase() == "pre") inner
                else "`$inner`"
            }
            "pre" -> {
                val lang = el.selectFirst("code")?.classNames()?.firstOrNull { it.startsWith("language-") }
                    ?.removePrefix("language-") ?: ""
                "```$lang\n${inner.trimEnd()}\n```\n\n"
            }
            "blockquote" -> {
                inner.trimEnd().lines().joinToString("\n") { "> $it" } + "\n\n"
            }
            "ul" -> convertListItems(el, ordered = false)
            "ol" -> convertListItems(el, ordered = true)
            "li" -> inner.trimEnd()
            "input" -> {
                if (el.attr("type").lowercase() == "checkbox") {
                    if (el.hasAttr("checked")) "[x] " else "[ ] "
                } else ""
            }
            "a" -> {
                val href = el.attr("href").ifBlank { "https://" }
                "[$inner]($href)"
            }
            "img" -> {
                val src = el.attr("src")
                val alt = el.attr("alt").ifBlank { "图片" }
                "![$alt]($src)"
            }
            "hr" -> "\n---\n\n"
            "table" -> convertTable(el)
            "div" -> "$inner\n"
            "span" -> inner
            "u" -> "<u>$inner</u>"
            else -> inner
        }
    }

    private fun convertListItems(listEl: Element, ordered: Boolean): String {
        val sb = StringBuilder()
        var index = 1
        for (li in listEl.children()) {
            if (li.tagName().lowercase() != "li") continue
            val cb = li.selectFirst("input[type=checkbox]")
            // Remove checkbox from DOM before converting children to avoid double prefix
            if (cb != null) cb.remove()
            val content = convertChildren(li).trimEnd()
            val prefix = if (cb != null) {
                if (cb.hasAttr("checked")) "- [x] " else "- [ ] "
            } else if (ordered) {
                "${index++}. "
            } else {
                "- "
            }
            sb.append("$prefix$content\n")
        }
        return sb.toString() + "\n"
    }

    private fun convertTable(tableEl: Element): String {
        val sb = StringBuilder()
        val rows = tableEl.select("tr")
        if (rows.isEmpty()) return ""

        for ((i, row) in rows.withIndex()) {
            val cells = row.select("th, td")
            sb.append("| ")
            sb.append(cells.joinToString(" | ") { convertChildren(it).trim() })
            sb.append(" |\n")
            if (i == 0) {
                sb.append("| ")
                sb.append(cells.joinToString(" | ") { " --- " })
                sb.append(" |\n")
            }
        }
        return sb.toString() + "\n"
    }
}
