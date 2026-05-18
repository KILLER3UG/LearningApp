package com.selfproject.learningapp.data

import com.selfproject.learningapp.data.local.FlashcardEntity
import kotlin.math.roundToInt
import kotlin.random.Random

data class ReviewStats(
    val total: Int,
    val due: Int,
    val newCards: Int,
    val learning: Int,
    val mastered: Int,
    val memoryScore: Int
)

enum class MatchSide {
    QUESTION,
    ANSWER
}

data class MatchTile(
    val id: String,
    val pairKey: String,
    val text: String,
    val side: MatchSide
)

object StudyPracticeEngine {
    fun buildReviewStats(
        flashcards: List<FlashcardEntity>,
        nowMillis: Long = System.currentTimeMillis()
    ): ReviewStats {
        if (flashcards.isEmpty()) {
            return ReviewStats(
                total = 0,
                due = 0,
                newCards = 0,
                learning = 0,
                mastered = 0,
                memoryScore = 0
            )
        }

        val due = flashcards.count { it.nextReview <= nowMillis }
        val newCards = flashcards.count { it.repetitions == 0 }
        val learning = flashcards.count { it.repetitions in 1..2 }
        val mastered = flashcards.count { it.repetitions >= 3 && it.nextReview > nowMillis }
        val average = flashcards.map { card ->
            val repetitionScore = (card.repetitions.coerceAtMost(5) / 5f)
            val duePenalty = if (card.nextReview <= nowMillis) 0.2f else 0f
            (repetitionScore - duePenalty).coerceIn(0f, 1f)
        }.average()

        return ReviewStats(
            total = flashcards.size,
            due = due,
            newCards = newCards,
            learning = learning,
            mastered = mastered,
            memoryScore = (average * 100).roundToInt().coerceIn(0, 100)
        )
    }

    fun buildMatchTiles(
        flashcards: List<FlashcardEntity>,
        maxPairs: Int = 6,
        seed: Int = 0
    ): List<MatchTile> {
        val pairs = flashcards
            .filter { it.question.isNotBlank() && it.answer.isNotBlank() }
            .take(maxPairs.coerceAtLeast(1))

        val tiles = pairs.flatMapIndexed { index, card ->
            val pairKey = pairKey(card, index)
            listOf(
                MatchTile(
                    id = "$pairKey-question",
                    pairKey = pairKey,
                    text = card.question,
                    side = MatchSide.QUESTION
                ),
                MatchTile(
                    id = "$pairKey-answer",
                    pairKey = pairKey,
                    text = card.answer,
                    side = MatchSide.ANSWER
                )
            )
        }

        return tiles.shuffled(Random(seed))
    }

    fun isMatch(first: MatchTile, second: MatchTile): Boolean =
        first.pairKey == second.pairKey && first.side != second.side && first.id != second.id

    private fun pairKey(card: FlashcardEntity, index: Int): String {
        return if (card.id != 0L) {
            "card-${card.id}"
        } else {
            "card-${index}-${card.question.hashCode()}-${card.answer.hashCode()}"
        }
    }
}
