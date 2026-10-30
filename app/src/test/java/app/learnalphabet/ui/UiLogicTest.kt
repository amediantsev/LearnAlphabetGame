package app.learnalphabet.ui

import app.learnalphabet.alphabet.Ukrainian
import org.junit.Assert.assertEquals
import org.junit.Test

class UiLogicTest {

    @Test
    fun highlightRange_isSingleCharAtIndex() =
        assertEquals(1..1, highlightRange(Ukrainian.letters.first { it.upper == "И" }))

    @Test
    fun imagePath_isLanguageFolderPlusWebp() =
        assertEquals("images/uk/02-banan.webp", imagePath("uk", Ukrainian.letters[1]))

    @Test
    fun viewModel_setMode_keepsCurrentCard() {
        val vm = CardViewModel()
        vm.next()
        vm.next()
        val before = vm.ui.value.index
        vm.setMode(Mode.SHUFFLE)
        assertEquals(before, vm.ui.value.index)
        assertEquals(Mode.SHUFFLE, vm.ui.value.mode)
    }

    @Test
    fun viewModel_startsOnFirstUkrainianLetter() = assertEquals("А", CardViewModel().ui.value.card.upper)

    @Test
    fun viewModel_previousFromFirst_isLastLetter() {
        val vm = CardViewModel()
        vm.previous()
        assertEquals("Я", vm.ui.value.card.upper)
    }

    @Test
    fun viewModel_setLanguage_ignoresUnavailable() {
        val vm = CardViewModel()
        vm.next()
        vm.setLanguage("en")
        assertEquals("uk", vm.ui.value.language.code)
        assertEquals(1, vm.ui.value.index)
    }
}
