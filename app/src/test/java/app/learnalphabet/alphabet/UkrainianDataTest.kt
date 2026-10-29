package app.learnalphabet.alphabet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UkrainianDataTest {

    @Test
    fun ukrainian_has33Letters_inAlphabetOrder() {
        assertEquals(
            "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯ",
            Ukrainian.letters.joinToString("") { it.upper },
        )
    }

    @Test
    fun everyHighlight_pointsAtTheCardsLetter() {
        Ukrainian.letters.forEach {
            assertEquals(it.upper, it.lower, it.word[it.highlightIndex].toString())
        }
    }

    @Test
    fun specialLetters_useInnerHighlight() {
        val byUpper = Ukrainian.letters.associateBy { it.upper }
        assertEquals("кит" to 1, byUpper.getValue("И").let { it.word to it.highlightIndex })
        assertEquals("кінь" to 3, byUpper.getValue("Ь").let { it.word to it.highlightIndex })
        assertEquals("рюкзак" to 1, byUpper.getValue("Ю").let { it.word to it.highlightIndex })
    }

    @Test
    fun imageNames_areUnique_andNumbered() {
        val names = Ukrainian.letters.map { it.imageName }
        assertEquals(33, names.toSet().size)
        names.forEachIndexed { i, n -> assertTrue(n, n.startsWith("%02d-".format(i + 1))) }
    }

    @Test
    fun spokenNames_matchSpec() {
        assertEquals(
            listOf(
                "а", "бе", "ве", "ге", "ґе", "де", "е", "є", "же", "зе", "и", "і", "ї", "йот",
                "ка", "ел", "ем", "ен", "о", "пе", "ер", "ес", "те", "у", "еф", "ха", "це", "че",
                "ша", "ща", "м'який знак", "ю", "я",
            ),
            Ukrainian.letters.map { it.spokenName },
        )
    }

    @Test
    fun languages_defaultIsUkrainian_englishIsComingSoon() {
        assertEquals("uk", Languages.default.code)
        assertFalse(Languages.byCode("en")!!.available)
    }

    @Test
    fun shapka_placeholder_isNotABaseballCap() {
        val shapka = Ukrainian.letters.first { it.upper == "Ш" }
        assertTrue(shapka.placeholder != "🧢")
    }
}
