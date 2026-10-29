package app.learnalphabet.alphabet

import java.util.Locale

/** One card: a letter, how to say it, and the word + picture that go with it. */
data class LetterCard(
    val upper: String,
    val lower: String,
    /** Text given to speech synthesis for the letter's name, e.g. "бе". */
    val spokenName: String,
    /** Lowercase word shown under the picture. */
    val word: String,
    /** Speech override when the plain word is pronounced wrong. */
    val spokenWord: String? = null,
    /** Index of the character in [word] to highlight (not always 0, e.g. "кит" for И). */
    val highlightIndex: Int = 0,
    /** Picture file base name without extension, e.g. "02-banan". */
    val imageName: String,
    /** Emoji shown until the picture exists; empty means "show a letter tile". */
    val placeholder: String,
)

data class Language(
    val code: String,
    val displayName: String,
    val ttsLocale: Locale,
    val available: Boolean,
    val letters: List<LetterCard>,
)
