package app.learnalphabet.ui

import androidx.lifecycle.ViewModel
import app.learnalphabet.alphabet.Language
import app.learnalphabet.alphabet.Languages
import app.learnalphabet.alphabet.LetterCard
import app.learnalphabet.order.CardNavigator
import app.learnalphabet.order.OrderMode
import app.learnalphabet.order.navigatorFor
import app.learnalphabet.quiz.AutoSpeakGate
import app.learnalphabet.quiz.MemoryLetterStore
import app.learnalphabet.quiz.QuizDeck
import app.learnalphabet.quiz.QuizLetterStore
import app.learnalphabet.quiz.QuizQuestion
import app.learnalphabet.quiz.allLetters
import app.learnalphabet.quiz.decodeLetters
import app.learnalphabet.quiz.encodeLetters
import app.learnalphabet.quiz.newQuestion
import app.learnalphabet.quiz.quizPool
import app.learnalphabet.speech.SpeakerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/** What a tap on a Find tile did; the screen plays the matching sound. */
enum class PickResult { RIGHT, WRONG, IGNORED }

/** What the top switch shows: two ways to go through cards, and the Find quiz («Знайди»). */
enum class Mode { ALPHABET, SHUFFLE, QUIZ }

data class CardUiState(
    val language: Language,
    val mode: Mode,
    /** Current letter; in [Mode.QUIZ] the answer of [quiz]. */
    val index: Int,
    /** +1 after moving forward, -1 after moving back; drives the slide direction. */
    val direction: Int = 1,
    /** The current question in [Mode.QUIZ], null otherwise. */
    val quiz: QuizQuestion? = null,
    /** Grows with every new question, so the same letter asked again is still a new screen. */
    val quizSerial: Int = 0,
    /** Letters (uppercase) the Find quiz asks about, chosen by the parent; all by default. */
    val quizLetters: Set<String> = allLetters(language),
) {
    val card: LetterCard get() = language.letters[index]
}

class CardViewModel(
    private val random: Random = Random.Default,
    private val letterStore: QuizLetterStore = MemoryLetterStore(),
) : ViewModel() {

    private val _ui = MutableStateFlow(
        CardUiState(Languages.default, Mode.ALPHABET, 0, quizLetters = savedLetters(Languages.default)),
    )
    val ui: StateFlow<CardUiState> = _ui.asStateFlow()

    private var navigator: CardNavigator = navigatorFor(OrderMode.ALPHABET, Languages.default.letters.size, 0)
    private var deck = QuizDeck(quizPool(Languages.default, _ui.value.quizLetters), random)
    private val autoSpeak = AutoSpeakGate()

    /**
     * Goes to the next card or question; false when nothing moved. In Find that is until the
     * right letter is found: a question cannot be skipped.
     */
    fun next(): Boolean {
        val state = _ui.value
        if (state.mode == Mode.QUIZ) {
            if (state.quiz?.solved != true) return false
            askNext(state)
        } else {
            _ui.value = state.copy(index = navigator.next(), direction = 1)
        }
        return true
    }

    /** Goes back one card; false when there is nowhere to go (always in Find). */
    fun previous(): Boolean {
        if (_ui.value.mode == Mode.QUIZ) return false
        val before = navigator.current
        val after = navigator.previous()
        if (after == before) return false
        _ui.value = _ui.value.copy(index = after, direction = -1)
        return true
    }

    fun setMode(mode: Mode) {
        val state = _ui.value
        if (mode == state.mode) return
        if (mode == Mode.QUIZ) {
            askNext(state.copy(mode = Mode.QUIZ), avoid = state.index)
        } else {
            navigator = navigatorFor(mode.toOrder(), state.language.letters.size, state.index)
            _ui.value = state.copy(mode = mode, quiz = null)
        }
    }

    fun setLanguage(code: String) {
        val language = Languages.byCode(code) ?: return
        val state = _ui.value
        if (!language.available || language.letters.isEmpty() || language == state.language) return
        val letters = savedLetters(language)
        deck = QuizDeck(quizPool(language, letters), random)
        if (state.mode == Mode.QUIZ) {
            askNext(state.copy(language = language, quizLetters = letters))
        } else {
            navigator = navigatorFor(state.mode.toOrder(), language.letters.size, 0)
            _ui.value = CardUiState(language, state.mode, 0, quizLetters = letters)
        }
    }

    /**
     * The parent chose which letters Find asks about. Saved at once; in Find the next
     * question comes from the new set right away, without waiting for the round to end.
     */
    fun setQuizLetters(letters: Set<String>) {
        val state = _ui.value
        val chosen = letters.intersect(allLetters(state.language))
        if (chosen.isEmpty() || chosen == state.quizLetters) return
        letterStore.save(state.language.code, encodeLetters(chosen, state.language))
        deck = QuizDeck(quizPool(state.language, chosen), random)
        val updated = state.copy(quizLetters = chosen)
        if (state.mode == Mode.QUIZ) askNext(updated, avoid = state.index) else _ui.value = updated
    }

    /**
     * A tap on a letter tile of question [serial]. Taps on a question that is still sliding
     * out after a skip are ignored, so they cannot land on the new question.
     */
    fun pick(serial: Int, option: Int): PickResult {
        val state = _ui.value
        if (serial != state.quizSerial) return PickResult.IGNORED
        val quiz = state.quiz ?: return PickResult.IGNORED
        val after = quiz.pick(option)
        _ui.value = state.copy(quiz = after)
        return when {
            after.solved && !quiz.solved -> PickResult.RIGHT
            after.wrong.size > quiz.wrong.size -> PickResult.WRONG
            else -> PickResult.IGNORED
        }
    }

    /** True once per new question, when the voice is ready: the screen then speaks it. */
    fun consumeAutoSpeak(state: SpeakerState): Boolean =
        _ui.value.mode == Mode.QUIZ && autoSpeak.consume(state)

    /** The app left the screen; coming back must not speak by itself. */
    fun onBackground() = autoSpeak.onBackground()

    private fun askNext(state: CardUiState, avoid: Int? = null) {
        val answer = deck.next(avoid)
        // Wrong options come from the whole alphabet, not only from the chosen letters.
        val question = newQuestion(answer, state.language.letters.indices.toList(), random)
        _ui.value = state.copy(index = answer, direction = 1, quiz = question, quizSerial = state.quizSerial + 1)
        autoSpeak.onNewQuestion()
    }

    private fun savedLetters(language: Language): Set<String> =
        decodeLetters(letterStore.load(language.code), language) ?: allLetters(language)

    private fun Mode.toOrder(): OrderMode = if (this == Mode.SHUFFLE) OrderMode.SHUFFLE else OrderMode.ALPHABET
}
