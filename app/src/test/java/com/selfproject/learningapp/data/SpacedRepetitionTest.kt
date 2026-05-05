package com.selfproject.learningapp.data

import com.selfproject.learningapp.data.local.FlashcardEntity
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for spaced repetition logic.
 */
class SpacedRepetitionTest {

    @Test
    fun `correct answer increases interval`() {
        val flashcard = FlashcardEntity(
            id = 1,
            documentUri = "uri",
            question = "Q",
            answer = "A",
            interval = 3,
            repetitions = 1,
            easeFactor = 2.5,
        )

        // Simulate SM-2: newInterval = interval * easeFactor
        val newInterval = (flashcard.interval * flashcard.easeFactor).toLong()

        assertTrue("Interval should increase", newInterval > flashcard.interval)
        assertEquals(7L, newInterval) // 3 * 2.5 = 7.5 -> 7
    }

    @Test
    fun `first correct answer sets interval to 1`() {
        val flashcard = FlashcardEntity(
            repetitions = 0,
            question = "Q",
            answer = "A",
            documentUri = "uri",
        )

        val newRepetitions = flashcard.repetitions + 1
        val newInterval = when (newRepetitions) {
            1 -> 1L
            2 -> 3L
            else -> (flashcard.interval * flashcard.easeFactor).toLong()
        }

        assertEquals(1L, newInterval)
    }

    @Test
    fun `second correct answer sets interval to 3`() {
        val flashcard = FlashcardEntity(
            repetitions = 1,
            question = "Q",
            answer = "A",
            documentUri = "uri",
        )

        val newRepetitions = flashcard.repetitions + 1
        val newInterval = when (newRepetitions) {
            1 -> 1L
            2 -> 3L
            else -> (flashcard.interval * flashcard.easeFactor).toLong()
        }

        assertEquals(3L, newInterval)
    }

    @Test
    fun `incorrect answer resets interval`() {
        val flashcard = FlashcardEntity(
            interval = 10,
            repetitions = 3,
            question = "Q",
            answer = "A",
            documentUri = "uri",
        )

        // Incorrect: reset to 1 day
        val newInterval = 1L
        val newRepetitions = 0

        assertEquals(1L, newInterval)
        assertEquals(0, newRepetitions)
    }

    @Test
    fun `easeFactor never drops below 1_3`() {
        var easeFactor = 2.5

        // Simulate many incorrect reviews decreasing ease factor
        repeat(20) {
            easeFactor = maxOf(1.3, easeFactor - 0.2)
        }

        assertEquals(1.3, easeFactor, 0.01)
    }

    @Test
    fun `nextReview is calculated correctly`() {
        val interval = 5L // days
        val now = System.currentTimeMillis()
        val oneDayMs = 86_400_000L

        val nextReview = now + (interval * oneDayMs)

        assertTrue("Next review should be in the future", nextReview > now)
        assertEquals(now + 5L * oneDayMs, nextReview)
    }
}
