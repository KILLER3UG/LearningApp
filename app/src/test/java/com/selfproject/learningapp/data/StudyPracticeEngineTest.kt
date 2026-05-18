package com.selfproject.learningapp.data

import com.selfproject.learningapp.data.local.FlashcardEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyPracticeEngineTest {
    @Test
    fun `review stats count due and mastered cards`() {
        val now = 1_000_000L
        val cards = listOf(
            FlashcardEntity(id = 1, documentUri = "uri", question = "Q1", answer = "A1", repetitions = 0, nextReview = now - 1),
            FlashcardEntity(id = 2, documentUri = "uri", question = "Q2", answer = "A2", repetitions = 2, nextReview = now + 1),
            FlashcardEntity(id = 3, documentUri = "uri", question = "Q3", answer = "A3", repetitions = 4, nextReview = now + 1)
        )

        val stats = StudyPracticeEngine.buildReviewStats(cards, now)

        assertEquals(3, stats.total)
        assertEquals(1, stats.due)
        assertEquals(1, stats.newCards)
        assertEquals(1, stats.learning)
        assertEquals(1, stats.mastered)
        assertTrue(stats.memoryScore in 0..100)
    }

    @Test
    fun `match tiles include one question and one answer per card`() {
        val cards = listOf(
            FlashcardEntity(id = 10, documentUri = "uri", question = "Cell power", answer = "Mitochondria"),
            FlashcardEntity(id = 11, documentUri = "uri", question = "DNA shape", answer = "Double helix")
        )

        val tiles = StudyPracticeEngine.buildMatchTiles(cards, seed = 7)

        assertEquals(4, tiles.size)
        assertEquals(2, tiles.map { it.pairKey }.distinct().size)
        assertTrue(tiles.any { it.text == "Cell power" && it.side == MatchSide.QUESTION })
        assertTrue(tiles.any { it.text == "Mitochondria" && it.side == MatchSide.ANSWER })
    }

    @Test
    fun `match check requires same pair and opposite sides`() {
        val tiles = StudyPracticeEngine.buildMatchTiles(
            listOf(FlashcardEntity(id = 10, documentUri = "uri", question = "Q", answer = "A")),
            seed = 1
        )
        val question = tiles.first { it.side == MatchSide.QUESTION }
        val answer = tiles.first { it.side == MatchSide.ANSWER }

        assertTrue(StudyPracticeEngine.isMatch(question, answer))
        assertFalse(StudyPracticeEngine.isMatch(question, question))
    }

    @Test
    fun `match round respects max pair limit`() {
        val cards = (1..10).map {
            FlashcardEntity(id = it.toLong(), documentUri = "uri", question = "Q$it", answer = "A$it")
        }

        val tiles = StudyPracticeEngine.buildMatchTiles(cards, maxPairs = 3, seed = 3)

        assertEquals(6, tiles.size)
        assertEquals(3, tiles.map { it.pairKey }.distinct().size)
    }
}
