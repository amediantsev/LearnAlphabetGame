package app.learnalphabet.alphabet

import java.util.Locale

val English = Language(
    code = "en",
    displayName = "English",
    ttsLocale = Locale.ENGLISH,
    available = false,
    letters = emptyList(),
)

object Languages {
    val all: List<Language> = listOf(Ukrainian, English)
    val default: Language = Ukrainian
    fun byCode(code: String): Language? = all.firstOrNull { it.code == code }
}
