package app.learnalphabet.quiz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuizQuestionTest {
    private val all = (0 until 33).toList()

    @Test fun newQuestion_hasFourDistinctOptionsIncludingAnswer() = repeat(200) { seed ->
        val q = newQuestion(answer = 5, pool = all, random = Random(seed))
        assertEquals(4, q.options.size)
        assertEquals(4, q.options.toSet().size)
        assertTrue(5 in q.options)
        assertTrue(q.options.all { it in all })
    }

    @Test fun newQuestion_answerLandsInEveryPosition() {
        val positions = (0 until 200).map { newQuestion(5, all, Random(it)).options.indexOf(5) }.toSet()
        assertEquals(setOf(0, 1, 2, 3), positions)
    }

    @Test fun newQuestion_smallPool_usesAllAvailable() {
        val q = newQuestion(answer = 1, pool = listOf(0, 1, 2), random = Random(0))
        assertEquals(setOf(0, 1, 2), q.options.toSet())
    }

    @Test fun pick_wrong_marksWrongAndKeepsGoing() {
        val q = QuizQuestion(answer = 0, options = listOf(0, 1, 2, 3)).pick(2)
        assertEquals(OptionState.WRONG, q.stateOf(2))
        assertFalse(q.solved)
        assertEquals(OptionState.OPEN, q.stateOf(0))
    }

    @Test fun pick_alreadyWrong_isIgnored() {
        val q = QuizQuestion(0, listOf(0, 1, 2, 3)).pick(2)
        assertSame(q, q.pick(2))
    }

    @Test fun threeWrongThenRight_solves() {
        val q = QuizQuestion(0, listOf(0, 1, 2, 3)).pick(1).pick(2).pick(3).pick(0)
        assertTrue(q.solved)
        assertEquals(OptionState.RIGHT, q.stateOf(0))
        assertEquals(OptionState.WRONG, q.stateOf(3))
    }

    @Test fun pick_afterSolved_isIgnored() {
        val q = QuizQuestion(0, listOf(0, 1, 2, 3)).pick(0)
        assertSame(q, q.pick(1))
        assertEquals(OptionState.LOCKED, q.stateOf(1))
    }
}
