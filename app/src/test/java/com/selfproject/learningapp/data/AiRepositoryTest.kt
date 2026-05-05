package com.selfproject.learningapp.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for AiRepository prompt building.
 * These tests verify the logic WITHOUT making actual API calls.
 */
class AiRepositoryTest {

    private val repository = AiRepository("test-api-key")

    @Test
    fun `buildPrompt includes selected text`() {
        val selectedText = "Mitochondria is the powerhouse of the cell"
        val context = "Cell biology basics"
        val template = "Explain this clearly"

        val prompt = repository.buildPromptForTest(selectedText, context, template)

        assertTrue(prompt.contains(selectedText))
    }

    @Test
    fun `buildPrompt includes surrounding context`() {
        val selectedText = "selected"
        val context = "paragraph before\n\nselected\n\nparagraph after"
        val template = "Explain"

        val prompt = repository.buildPromptForTest(selectedText, context, template)

        assertTrue(prompt.contains("paragraph before"))
        assertTrue(prompt.contains("paragraph after"))
    }

    @Test
    fun `buildPrompt includes user request template`() {
        val selectedText = "text"
        val context = "ctx"
        val template = "Provide an analogy"

        val prompt = repository.buildPromptForTest(selectedText, context, template)

        assertTrue(prompt.contains("Provide an analogy"))
    }

    @Test
    fun `buildPrompt includes system instructions`() {
        val prompt = repository.buildPromptForTest("text", "ctx", "Explain")

        assertTrue(prompt.contains("study assistant"))
        assertTrue(prompt.contains("Guidelines for responses"))
    }

    @Test
    fun `buildPrompt includes user request in prompt body`() {
        val selectedText = "important concept"
        val prompt = repository.buildPromptForTest(selectedText, "ctx", "Explain")

        assertTrue(prompt.contains(selectedText))
        assertTrue(prompt.contains("Explain"))
    }

    @Test
    fun `buildPrompt sections are in correct order`() {
        val prompt = repository.buildPromptForTest("selected", "context", "template")

        val systemIndex = prompt.indexOf("study assistant")
        val contextIndex = prompt.indexOf("CONTEXT FROM DOCUMENT")
        val selectedIndex = prompt.indexOf("SELECTED TEXT")
        val requestIndex = prompt.indexOf("USER REQUEST")

        assertTrue("System prompt should come first", systemIndex < contextIndex)
        assertTrue("Context should come before selected text", contextIndex < selectedIndex)
        assertTrue("Selected text should come before user request", selectedIndex < requestIndex)
    }

    @Test
    fun `generateFlashcardsPrompt contains proper instructions`() {
        val content = "Photosynthesis converts light energy into chemical energy."
        val prompt = repository.buildFlashcardsPrompt(content)

        assertTrue(prompt.contains("Photosynthesis"))
        assertTrue(prompt.contains("flashcards"))
        assertTrue(prompt.contains("Q:"))
        assertTrue(prompt.contains("A:"))
    }

    @Test
    fun `generateQuizPrompt contains proper instructions`() {
        val content = "The water cycle: evaporation, condensation, precipitation."
        val prompt = repository.buildQuizPrompt(content)

        assertTrue(prompt.contains("water cycle"))
        assertTrue(prompt.contains("self-check questions"))
        assertTrue(prompt.contains("explanations"))
    }

    @Test
    fun `system prompt includes all guideline categories`() {
        val prompt = repository.buildPromptForTest("text", "ctx", "Explain")

        assertTrue("Should mention examples", prompt.contains("examples"))
        assertTrue("Should mention analogies", prompt.contains("analogies"))
        assertTrue("Should mention formatting", prompt.contains("formatting"))
        assertTrue("Should mention key terms", prompt.contains("key terms"))
    }

    @Test
    fun `prompt for explain template includes explanation structure`() {
        val prompt = repository.buildPromptForTest(
            "Natural selection",
            "Evolution theory",
            "Explain this clearly"
        )

        assertTrue(prompt.contains("Explain this clearly"))
        assertTrue(prompt.contains("direct answer"))
        assertTrue(prompt.contains("context and background"))
        assertTrue(prompt.contains("key takeaways"))
    }

    @Test
    fun `prompt for test template includes quiz structure`() {
        val prompt = repository.buildPromptForTest(
            "DNA replication",
            "Genetics chapter",
            "Test me on this"
        )

        assertTrue(prompt.contains("Test me on this"))
        assertTrue(prompt.contains("questions"))
        assertTrue(prompt.contains("answers"))
    }
}
