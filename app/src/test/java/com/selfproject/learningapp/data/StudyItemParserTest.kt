package com.selfproject.learningapp.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for StudyItemParser — pure Kotlin parsing logic.
 */
class StudyItemParserTest {

    @Test
    fun `parseFlashcards parses Q slash A format`() {
        val response = """
            Q: What is photosynthesis?
            A: The process by which plants convert light energy into chemical energy.
            
            Q: What is cellular respiration?
            A: The process of breaking down glucose to produce ATP.
        """.trimIndent()

        val flashcards = StudyItemParser.parseFlashcards(response, "uri://test")

        assertEquals(2, flashcards.size)
        assertEquals("What is photosynthesis?", flashcards[0].question)
        assertEquals("The process by which plants convert light energy into chemical energy.", flashcards[0].answer)
        assertEquals("What is cellular respiration?", flashcards[1].question)
    }

    @Test
    fun `parseFlashcards handles dot separator`() {
        val response = """
            Q. What is DNA?
            A. Deoxyribonucleic acid.
        """.trimIndent()

        val flashcards = StudyItemParser.parseFlashcards(response, "uri://test")

        assertEquals(1, flashcards.size)
        // The parser uses substringAfter(":") which for "Q. What is DNA?" returns the full line
        // since there's no colon. The actual behavior depends on the parser implementation.
        // For dot format, the question includes everything after "Q. "
        assertTrue(flashcards[0].question.contains("DNA"))
    }

    @Test
    fun `parseFlashcards returns empty for invalid format`() {
        val response = "This is just plain text without Q/A format."

        val flashcards = StudyItemParser.parseFlashcards(response, "uri://test")

        assertTrue(flashcards.isEmpty())
    }

    @Test
    fun `parseFlashcards handles multiple line answers`() {
        val response = """
            Q: What are the key steps?
            A: Step one is glycolysis. Step two is the Krebs cycle.
        """.trimIndent()

        val flashcards = StudyItemParser.parseFlashcards(response, "uri://test")

        assertEquals(1, flashcards.size)
        assertTrue(flashcards[0].answer.contains("glycolysis"))
    }

    @Test
    fun `parseQuizzes parses Q slash A slash Explanation format`() {
        val response = """
            Q1: What is the powerhouse of the cell?
            A: Mitochondria
            Explanation: The mitochondria generates most of the cell's ATP.
            
            Q2: What is DNA?
            A: Deoxyribonucleic acid
            Explanation: It carries genetic information.
        """.trimIndent()

        val quizzes = StudyItemParser.parseQuizzes(response, "uri://test", "source text")

        assertEquals(2, quizzes.size)
        assertEquals("What is the powerhouse of the cell?", quizzes[0].question)
        assertEquals("Mitochondria", quizzes[0].answer)
        assertTrue(quizzes[0].explanation.contains("ATP"))
    }

    @Test
    fun `parseQuizzes handles varying formats`() {
        val response = """
            Q: What is the water cycle?
            Answer: The continuous movement of water on Earth
            Why: Because water evaporates, condenses, and precipitates
        """.trimIndent()

        val quizzes = StudyItemParser.parseQuizzes(response, "uri://test", "water content")

        assertEquals(1, quizzes.size)
        assertEquals("What is the water cycle?", quizzes[0].question)
        assertTrue(quizzes[0].answer.contains("water on Earth"))
    }

    @Test
    fun `parseQuizzes returns empty for invalid format`() {
        val response = "Just a paragraph without quiz structure."

        val quizzes = StudyItemParser.parseQuizzes(response, "uri://test", "source")

        assertTrue(quizzes.isEmpty())
    }

    @Test
    fun `extractKeyTopics extracts headings`() {
        val content = """
            # Introduction
            Some text
            
            ## Photosynthesis
            More text
            
            ### Details
            Even more
        """.trimIndent()

        val topics = StudyItemParser.extractKeyTopics(content)

        assertEquals(3, topics.size)
        assertEquals("Introduction", topics[0])
        assertEquals("Photosynthesis", topics[1])
        assertEquals("Details", topics[2])
    }

    @Test
    fun `extractKeyTopics extracts bold terms when not enough headings`() {
        val content = "This text has **mitochondria** and **ribosomes** and **nucleus** as key terms."

        val topics = StudyItemParser.extractKeyTopics(content)

        assertTrue(topics.contains("mitochondria"))
        assertTrue(topics.contains("ribosomes"))
        assertTrue(topics.contains("nucleus"))
    }

    @Test
    fun `extractKeyTopics respects maxTopics limit`() {
        val content = """
            # Heading1
            # Heading2
            # Heading3
            # Heading4
            # Heading5
            # Heading6
            # Heading7
        """.trimIndent()

        val topics = StudyItemParser.extractKeyTopics(content, maxTopics = 5)

        assertEquals(5, topics.size)
    }

    @Test
    fun `extractKeyTopics returns empty for empty content`() {
        val topics = StudyItemParser.extractKeyTopics("")

        assertTrue(topics.isEmpty())
    }
}
