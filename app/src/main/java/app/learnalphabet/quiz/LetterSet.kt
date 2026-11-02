package app.learnalphabet.quiz

import app.learnalphabet.alphabet.Language

/*
 * The set of letters the Find quiz asks about, chosen by the parent. Letters are kept as
 * their uppercase form (e.g. "А"), not as indices, so a saved set survives a reordering
 * of the cards. Wrong options are still drawn from the whole alphabet.
 */

/** Every letter of [language]: the set used until the parent chooses one. */
fun allLetters(language: Language): Set<String> = language.letters.map { it.upper }.toSet()

/** Adds [letter] or takes it out. The picker may be empty while choosing, see [canSaveLetters]. */
fun toggleLetter(selected: Set<String>, letter: String): Set<String> =
    if (letter in selected) selected - letter else selected + letter

/** A set is kept only when it has at least one letter; Find always has something to ask. */
fun canSaveLetters(letters: Set<String>): Boolean = letters.isNotEmpty()

/** Saved form: the letters in alphabet order, comma-separated ("А,М,О"). */
fun encodeLetters(letters: Set<String>, language: Language): String =
    language.letters.map { it.upper }.filter { it in letters }.joinToString(",")

/** Reads [encodeLetters] back; null when nothing usable was saved (then use all letters). */
fun decodeLetters(raw: String?, language: Language): Set<String>? {
    val known = allLetters(language)
    return raw?.split(",")?.filter { it in known }?.toSet()?.takeIf { it.isNotEmpty() }
}

/** Indices of [letters] in [language], in alphabet order: the letters questions are about. */
fun quizPool(language: Language, letters: Set<String>): List<Int> =
    language.letters.indices.filter { language.letters[it].upper in letters }

/** Where the chosen set is kept between launches, per language. Holds [encodeLetters] text. */
interface QuizLetterStore {
    fun load(languageCode: String): String?
    fun save(languageCode: String, raw: String)
}

/** Keeps the set only while the app runs; for previews and tests. */
class MemoryLetterStore : QuizLetterStore {
    private val saved = mutableMapOf<String, String>()
    override fun load(languageCode: String): String? = saved[languageCode]
    override fun save(languageCode: String, raw: String) {
        saved[languageCode] = raw
    }
}
