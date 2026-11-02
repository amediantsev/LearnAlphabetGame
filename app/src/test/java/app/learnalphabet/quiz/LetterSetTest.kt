package app.learnalphabet.quiz

import app.learnalphabet.alphabet.Ukrainian
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LetterSetTest {

    @Test fun allLetters_isEveryUppercaseLetter() {
        val all = allLetters(Ukrainian)
        assertEquals(33, all.size)
        assertEquals(Ukrainian.letters.map { it.upper }.toSet(), all)
    }

    @Test fun toggle_addsMissingLetter() =
        assertEquals(setOf("А", "М"), toggleLetter(setOf("А"), "М"))

    @Test fun toggle_removesPresentLetter() =
        assertEquals(setOf("А"), toggleLetter(setOf("А", "М"), "М"))

    // The picker may be emptied while choosing; it just cannot be saved empty.
    @Test fun toggle_canRemoveTheLastLetter() =
        assertEquals(emptySet<String>(), toggleLetter(setOf("А"), "А"))

    @Test fun canSave_onlyNonEmpty() {
        assertTrue(canSaveLetters(setOf("А")))
        assertFalse(canSaveLetters(emptySet()))
    }

    @Test fun encode_usesAlphabetOrder() =
        assertEquals("А,М,О", encodeLetters(setOf("О", "А", "М"), Ukrainian))

    @Test fun decode_roundTrips() =
        assertEquals(setOf("А", "М", "О"), decodeLetters(encodeLetters(setOf("О", "А", "М"), Ukrainian), Ukrainian))

    @Test fun decode_nothingSaved_isNull() = assertNull(decodeLetters(null, Ukrainian))

    @Test fun decode_dropsUnknownLetters() =
        assertEquals(setOf("А"), decodeLetters("А,Q,", Ukrainian))

    @Test fun decode_nothingUsable_isNull() {
        assertNull(decodeLetters("", Ukrainian))
        assertNull(decodeLetters("Q,W", Ukrainian))
    }

    @Test fun pool_isIndicesOfChosenLettersInOrder() =
        assertEquals(listOf(0, 16, 18), quizPool(Ukrainian, setOf("О", "А", "М")))
}
