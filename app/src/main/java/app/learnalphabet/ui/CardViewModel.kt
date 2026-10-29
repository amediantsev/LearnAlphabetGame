package app.learnalphabet.ui

import androidx.lifecycle.ViewModel
import app.learnalphabet.alphabet.Language
import app.learnalphabet.alphabet.Languages
import app.learnalphabet.alphabet.LetterCard
import app.learnalphabet.order.CardNavigator
import app.learnalphabet.order.OrderMode
import app.learnalphabet.order.navigatorFor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CardUiState(
    val language: Language,
    val mode: OrderMode,
    val index: Int,
    /** +1 after moving forward, -1 after moving back; drives the slide direction. */
    val direction: Int = 1,
) {
    val card: LetterCard get() = language.letters[index]
}

class CardViewModel : ViewModel() {

    private val _ui = MutableStateFlow(CardUiState(Languages.default, OrderMode.ALPHABET, 0))
    val ui: StateFlow<CardUiState> = _ui.asStateFlow()

    private var navigator: CardNavigator = navigatorFor(OrderMode.ALPHABET, Languages.default.letters.size, 0)

    fun next() {
        _ui.value = _ui.value.copy(index = navigator.next(), direction = 1)
    }

    fun previous() {
        val before = navigator.current
        val after = navigator.previous()
        if (after != before) _ui.value = _ui.value.copy(index = after, direction = -1)
    }

    fun setMode(mode: OrderMode) {
        val state = _ui.value
        if (mode == state.mode) return
        navigator = navigatorFor(mode, state.language.letters.size, state.index)
        _ui.value = state.copy(mode = mode)
    }

    fun setLanguage(code: String) {
        val language = Languages.byCode(code) ?: return
        val state = _ui.value
        if (!language.available || language.letters.isEmpty() || language == state.language) return
        navigator = navigatorFor(state.mode, language.letters.size, 0)
        _ui.value = CardUiState(language, state.mode, 0)
    }
}
