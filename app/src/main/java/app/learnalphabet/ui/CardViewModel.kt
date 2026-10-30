package app.learnalphabet.ui

import androidx.lifecycle.ViewModel
import app.learnalphabet.alphabet.Language
import app.learnalphabet.alphabet.Languages
import app.learnalphabet.alphabet.LetterCard
import app.learnalphabet.order.CardNavigator
import app.learnalphabet.order.OrderMode
import app.learnalphabet.order.navigatorFor
import app.learnalphabet.quiz.AutoSpeakGate
import app.learnalphabet.quiz.QuizDeck
import app.learnalphabet.quiz.QuizQuestion
import app.learnalphabet.quiz.newQuestion
import app.learnalphabet.speech.SpeakerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/** What the top switch shows: two ways to go through cards, and the «Знайди» quiz. */
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
) {
    val card: LetterCard get() = language.letters[index]
}

/** Letters the quiz asks about and draws wrong options from. Later: a set chosen by the parent. */
fun quizPool(language: Language): List<Int> = language.letters.indices.toList()

class CardViewModel(private val random: Random = Random.Default) : ViewModel() {

    private val _ui = MutableStateFlow(CardUiState(Languages.default, Mode.ALPHABET, 0))
    val ui: StateFlow<CardUiState> = _ui.asStateFlow()

    private var navigator: CardNavigator = navigatorFor(OrderMode.ALPHABET, Languages.default.letters.size, 0)
    private var deck = QuizDeck(quizPool(Languages.default), random)
    private val autoSpeak = AutoSpeakGate()

    fun next() {
        val state = _ui.value
        if (state.mode == Mode.QUIZ) {
            askNext(state)
        } else {
            _ui.value = state.copy(index = navigator.next(), direction = 1)
        }
    }

    fun previous() {
        if (_ui.value.mode == Mode.QUIZ) return
        val before = navigator.current
        val after = navigator.previous()
        if (after != before) _ui.value = _ui.value.copy(index = after, direction = -1)
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
        deck = QuizDeck(quizPool(language), random)
        if (state.mode == Mode.QUIZ) {
            askNext(state.copy(language = language))
        } else {
            navigator = navigatorFor(state.mode.toOrder(), language.letters.size, 0)
            _ui.value = CardUiState(language, state.mode, 0)
        }
    }

    /** A tap on a letter tile in «Знайди». */
    fun pick(option: Int) {
        val state = _ui.value
        val quiz = state.quiz ?: return
        _ui.value = state.copy(quiz = quiz.pick(option))
    }

    /** True once per new question, when the voice is ready: the screen then speaks it. */
    fun consumeAutoSpeak(state: SpeakerState): Boolean =
        _ui.value.mode == Mode.QUIZ && autoSpeak.consume(state)

    /** The app left the screen; coming back must not speak by itself. */
    fun onBackground() = autoSpeak.onBackground()

    private fun askNext(state: CardUiState, avoid: Int? = null) {
        val answer = deck.next(avoid)
        val question = newQuestion(answer, quizPool(state.language), random)
        _ui.value = state.copy(index = answer, direction = 1, quiz = question, quizSerial = state.quizSerial + 1)
        autoSpeak.onNewQuestion()
    }

    private fun Mode.toOrder(): OrderMode = if (this == Mode.SHUFFLE) OrderMode.SHUFFLE else OrderMode.ALPHABET
}
