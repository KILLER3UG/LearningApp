package com.selfproject.learningapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownContentParserTest {

    @Test
    fun `parses tables without outer pipes`() {
        val content = """
            Intro paragraph.

            Term | Meaning | Score
            :--- | :---: | ---:
            Alpha | First item | 10
            Beta | Second item | 8

            After paragraph.
        """.trimIndent()

        val blocks = MarkdownContentParser.parse(content)
        val table = blocks.filterIsInstance<MarkdownContentBlock.Table>().single().table

        assertEquals(listOf("Term", "Meaning", "Score"), table.headers)
        assertEquals(listOf(MarkdownTableAlignment.Start, MarkdownTableAlignment.Center, MarkdownTableAlignment.End), table.alignments)
        assertEquals(listOf("Alpha", "First item", "10"), table.rows.first())
        assertTrue(blocks.first() is MarkdownContentBlock.Text)
        assertTrue(blocks.last() is MarkdownContentBlock.Text)
    }

    @Test
    fun `does not parse tables inside fenced code blocks`() {
        val content = """
            ```md
            | A | B |
            |---|---|
            | 1 | 2 |
            ```
        """.trimIndent()

        val blocks = MarkdownContentParser.parse(content)

        assertEquals(1, blocks.size)
        assertTrue(blocks.single() is MarkdownContentBlock.Text)
        assertTrue((blocks.single() as MarkdownContentBlock.Text).content.contains("| A | B |"))
    }

    @Test
    fun `keeps escaped and code span pipes inside cells`() {
        val content = """
            | Name | Example |
            | --- | --- |
            | Escaped | a \| b |
            | Code | `a | b` |
        """.trimIndent()

        val table = MarkdownContentParser.parse(content)
            .filterIsInstance<MarkdownContentBlock.Table>()
            .single()
            .table

        assertEquals("a | b", table.rows[0][1])
        assertEquals("`a | b`", table.rows[1][1])
    }

    @Test
    fun `normalizes short and long table rows`() {
        val content = """
            | Name | Detail | Score |
            | --- | --- | --- |
            | Short | only one detail |
            | Long | has | too | many | pipes |
        """.trimIndent()

        val table = MarkdownContentParser.parse(content)
            .filterIsInstance<MarkdownContentBlock.Table>()
            .single()
            .table

        assertEquals(listOf("Short", "only one detail", ""), table.rows[0])
        assertEquals(listOf("Long", "has", "too | many | pipes"), table.rows[1])
    }

    @Test
    fun `does not treat prose pipes as tables`() {
        val content = "Use A | B when explaining alternatives.\nNo separator follows."

        val blocks = MarkdownContentParser.parse(content)

        assertEquals(1, blocks.size)
        assertTrue(blocks.single() is MarkdownContentBlock.Text)
    }

    @Test
    fun `supports tilde fenced code blocks`() {
        val content = """
            ~~~
            | A | B |
            |---|---|
            ~~~
        """.trimIndent()

        val blocks = MarkdownContentParser.parse(content)

        assertEquals(1, blocks.size)
        assertTrue(blocks.single() is MarkdownContentBlock.Text)
    }
}
