package app.learnalphabet.ui

import app.learnalphabet.quiz.OptionState
import app.learnalphabet.speech.SpeakerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuizViewModelTest {

    private fun vm(seed: Int = 0) = CardViewModel(Random(seed))

    @Test fun quiz_firstQuestion_isNotCurrentCard() = repeat(100) { seed ->
        val vm = vm(seed)
        repeat(3) { vm.next() }
        assertEquals(3, vm.ui.value.index)
        vm.setMode(Mode.QUIZ)
        val ui = vm.ui.value
        val quiz = ui.quiz!!
        assertNotEquals(3, quiz.answer)
        assertEquals(quiz.answer, ui.index)
        assertEquals(4, quiz.options.size)
    }

    @Test fun quiz_next_makesNewQuestionAndBumpsSerial() {
        val vm = vm()
        vm.setMode(Mode.QUIZ)
        val first = vm.ui.value
        vm.pick(first.quizSerial, first.quiz!!.options.first { it != first.quiz!!.answer })
        vm.next()
        val second = vm.ui.value
        assertEquals(first.quizSerial + 1, second.quizSerial)
        assertFalse(second.quiz!!.solved)
        assertTrue(second.quiz!!.wrong.isEmpty())
        assertEquals(second.quiz!!.answer, second.index)
    }

    @Test fun quiz_previous_isNoOp() {
        val vm = vm()
        vm.setMode(Mode.QUIZ)
        vm.next()
        val before = vm.ui.value
        vm.previous()
        assertEquals(before, vm.ui.value)
    }

    @Test fun quiz_pick_wrongThenRight() {
        val vm = vm()
        vm.setMode(Mode.QUIZ)
        val quiz = vm.ui.value.quiz!!
        val serial = vm.ui.value.quizSerial
        val wrong = quiz.options.first { it != quiz.answer }
        vm.pick(serial, wrong)
        assertEquals(OptionState.WRONG, vm.ui.value.quiz!!.stateOf(wrong))
        vm.pick(serial, quiz.answer)
        assertTrue(vm.ui.value.quiz!!.solved)
    }

    @Test fun quiz_reenter_continuesRound() {
        val vm = vm()
        val answers = mutableListOf<Int>()
        vm.setMode(Mode.QUIZ)
        answers += vm.ui.value.quiz!!.answer
        repeat(9) { vm.next(); answers += vm.ui.value.quiz!!.answer }
        vm.setMode(Mode.ALPHABET)
        vm.setMode(Mode.QUIZ)
        answers += vm.ui.value.quiz!!.answer
        repeat(22) { vm.next(); answers += vm.ui.value.quiz!!.answer }
        assertEquals(33, answers.toSet().size)
    }

    @Test fun leavingQuiz_showsCardOfLastQuestion() {
        val vm = vm()
        vm.setMode(Mode.QUIZ)
        val a = vm.ui.value.index
        vm.setMode(Mode.ALPHABET)
        assertEquals(a, vm.ui.value.index)
        assertNull(vm.ui.value.quiz)
        vm.next()
        assertEquals((a + 1) % 33, vm.ui.value.index)
    }

    @Test fun autoSpeak_oncePerQuestion() {
        val vm = vm()
        vm.setMode(Mode.QUIZ)
        assertTrue(vm.consumeAutoSpeak(SpeakerState.READY))
        assertFalse(vm.consumeAutoSpeak(SpeakerState.READY))
        vm.next()
        assertTrue(vm.consumeAutoSpeak(SpeakerState.READY))
    }

    @Test fun autoSpeak_notAfterBackground() {
        val vm = vm()
        vm.setMode(Mode.QUIZ)
        vm.onBackground()
        assertFalse(vm.consumeAutoSpeak(SpeakerState.READY))
    }

    @Test fun autoSpeak_neverInCardModes() = assertFalse(vm().consumeAutoSpeak(SpeakerState.READY))

    @Test fun quiz_pickFromOldQuestion_isIgnored() {
        val vm = vm()
        vm.setMode(Mode.QUIZ)
        val old = vm.ui.value
        vm.next()
        val now = vm.ui.value
        (0 until 33).forEach { vm.pick(old.quizSerial, it) }
        assertEquals(now, vm.ui.value)
    }

    @Test fun quiz_previous_reportsNoMove() {
        val vm = vm()
        vm.setMode(Mode.QUIZ)
        assertFalse(vm.previous())
    }

    @Test fun cards_previous_reportsMove() {
        val vm = vm()
        assertTrue(vm.previous())
    }
}
