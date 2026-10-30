package app.learnalphabet.quiz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import kotlin.random.Random

class QuizDeckTest {
    private val all = (0 until 33).toList()

    @Test fun round_coversEveryLetterOnce() = repeat(50) { seed ->
        val d = QuizDeck(all, Random(seed))
        assertEquals(all.toSet(), List(33) { d.next() }.toSet())
    }

    @Test fun newRound_doesNotRepeatLastLetter() = repeat(200) { seed ->
        val d = QuizDeck(all, Random(seed))
        val seq = List(66) { d.next() }
        assertNotEquals(seq[32], seq[33])
    }

    @Test fun avoid_isSkippedWhileOthersRemain() = repeat(200) { seed ->
        val d = QuizDeck(all, Random(seed))
        val first = d.next(avoid = 7)
        assertNotEquals(7, first)
        // 7 is not lost from the round
        assertEquals(all.toSet(), (listOf(first) + List(32) { d.next() }).toSet())
    }

    @Test fun singleItemPool_alwaysReturnsIt() {
        val d = QuizDeck(listOf(4), Random(0))
        assertEquals(4, d.next(avoid = 4))
        assertEquals(4, d.next())
    }
}
