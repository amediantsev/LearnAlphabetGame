package app.learnalphabet.ui

import app.learnalphabet.quiz.OptionState
import app.learnalphabet.quiz.QuizLetterStore
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

    private class MemoryStore(var saved: MutableMap<String, String> = mutableMapOf()) : QuizLetterStore {
        override fun load(languageCode: String): String? = saved[languageCode]
        override fun save(languageCode: String, raw: String) { saved[languageCode] = raw }
    }

    private val amo = setOf("А", "М", "О")
    private val amoIndices = setOf(0, 16, 18)

    @Test fun quiz_asksOnlyChosenLetters() = repeat(20) { seed ->
        val vm = CardViewModel(Random(seed), MemoryStore())
        vm.setQuizLetters(amo)
        vm.setMode(Mode.QUIZ)
        val answers = List(30) { vm.ui.value.quiz!!.answer.also { vm.next() } }
        assertEquals(amoIndices, answers.toSet())
    }

    @Test fun quiz_wrongOptionsMayComeFromOutsideTheSet() {
        val vm = CardViewModel(Random(1), MemoryStore())
        vm.setQuizLetters(amo)
        vm.setMode(Mode.QUIZ)
        val options = List(30) { vm.ui.value.quiz!!.options.also { assertEquals(4, it.size); vm.next() } }.flatten()
        assertTrue(options.any { it !in amoIndices })
    }

    @Test fun quiz_singleLetterSet_stillHasFourOptions() {
        val vm = CardViewModel(Random(0), MemoryStore())
        vm.setQuizLetters(setOf("М"))
        vm.setMode(Mode.QUIZ)
        repeat(5) {
            val quiz = vm.ui.value.quiz!!
            assertEquals(16, quiz.answer)
            assertEquals(4, quiz.options.toSet().size)
            vm.next()
        }
    }

    @Test fun letters_defaultIsAll() =
        assertEquals(33, CardViewModel(Random(0), MemoryStore()).ui.value.quizLetters.size)

    @Test fun letters_areSavedAndLoaded() {
        val store = MemoryStore()
        CardViewModel(Random(0), store).setQuizLetters(amo)
        assertEquals(amo, CardViewModel(Random(0), store).ui.value.quizLetters)
    }

    @Test fun letters_changedInQuiz_newQuestionFromNewSet() {
        val vm = CardViewModel(Random(0), MemoryStore())
        vm.setMode(Mode.QUIZ)
        val before = vm.ui.value.quizSerial
        vm.setQuizLetters(setOf("Я"))
        assertEquals(before + 1, vm.ui.value.quizSerial)
        assertEquals(32, vm.ui.value.quiz!!.answer)
        assertTrue(vm.consumeAutoSpeak(SpeakerState.READY))
    }

    @Test fun letters_sameSet_changesNothing() {
        val vm = CardViewModel(Random(0), MemoryStore())
        vm.setQuizLetters(amo)
        vm.setMode(Mode.QUIZ)
        val before = vm.ui.value
        vm.setQuizLetters(setOf("О", "М", "А"))
        assertEquals(before, vm.ui.value)
    }

    @Test fun letters_emptySet_isIgnored() {
        val store = MemoryStore()
        val vm = CardViewModel(Random(0), store)
        vm.setQuizLetters(amo)
        vm.setQuizLetters(emptySet())
        assertEquals(amo, vm.ui.value.quizLetters)
    }

    @Test fun letters_changedInCards_staysOnCard() {
        val vm = CardViewModel(Random(0), MemoryStore())
        vm.next()
        val before = vm.ui.value
        vm.setQuizLetters(amo)
        assertEquals(before.copy(quizLetters = amo), vm.ui.value)
    }
}
