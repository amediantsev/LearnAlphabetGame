package app.learnalphabet.speech

import android.content.Intent
import android.speech.tts.TextToSpeech
import app.learnalphabet.alphabet.LetterCard
import kotlinx.coroutines.flow.StateFlow

enum class SpeakerState { INITIALIZING, READY, MISSING_VOICE, UNAVAILABLE }

/** Speaks a card aloud. Kept behind an interface so recorded audio can replace synthesis later. */
interface CardSpeaker {
    val state: StateFlow<SpeakerState>
    fun speak(card: LetterCard, mode: SpeechMode = SpeechMode.DEFAULT)
    fun stop()
    fun release()

    /** Looks again for the voice, e.g. after the parent installed it in system settings. */
    fun recheck()
}

const val GOOGLE_TTS = "com.google.android.tts"

/** Google's engine has Ukrainian; many vendor engines do not. Otherwise use the phone's default. */
fun chooseEngine(installed: List<String>, default: String?): String? =
    if (GOOGLE_TTS in installed) GOOGLE_TTS else default ?: installed.firstOrNull()

/** Maps the result of [TextToSpeech.setLanguage] to what the UI needs to know. */
fun stateFor(languageResult: Int): SpeakerState = when {
    languageResult >= TextToSpeech.LANG_AVAILABLE -> SpeakerState.READY
    languageResult == TextToSpeech.LANG_MISSING_DATA ||
        languageResult == TextToSpeech.LANG_NOT_SUPPORTED -> SpeakerState.MISSING_VOICE
    else -> SpeakerState.UNAVAILABLE
}

/**
 * Maps an error reported while speaking. Missing voice data or no network for an online-only
 * voice means the parent has to install the voice; other errors are treated as one-offs (null).
 */
fun stateForError(errorCode: Int): SpeakerState? = when (errorCode) {
    TextToSpeech.ERROR_NOT_INSTALLED_YET,
    TextToSpeech.ERROR_NETWORK,
    TextToSpeech.ERROR_NETWORK_TIMEOUT -> SpeakerState.MISSING_VOICE
    else -> null
}

/** What a card says aloud; the parent picks it in settings. [key] is what gets saved. */
enum class SpeechMode(val key: String) {
    /** «А… автобус» */
    LETTER_AND_WORD("letter_and_word"),

    /** «А» */
    LETTER_ONLY("letter_only");

    companion object {
        val DEFAULT = LETTER_AND_WORD

        /** A missing or unknown saved value falls back to [DEFAULT]. */
        fun fromKey(key: String?): SpeechMode = entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}

data class Utterance(val text: String, val flush: Boolean, val pauseAfterMs: Long = 0)

/**
 * Letter name, then (in [SpeechMode.LETTER_AND_WORD]) a short pause and the word.
 * The first one cuts off anything still playing.
 */
fun utterancesFor(card: LetterCard, mode: SpeechMode = SpeechMode.DEFAULT): List<Utterance> = when (mode) {
    SpeechMode.LETTER_ONLY -> listOf(Utterance(card.spokenName, flush = true))
    SpeechMode.LETTER_AND_WORD -> listOf(
        Utterance(card.spokenName, flush = true, pauseAfterMs = 450),
        Utterance(card.spokenWord ?: card.word, flush = false),
    )
}

/** Screens that let the parent install a voice, best first. */
fun voiceSetupIntents(): List<Intent> = listOf(
    Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).setPackage(GOOGLE_TTS),
    Intent("com.android.settings.TTS_SETTINGS"),
    Intent(android.provider.Settings.ACTION_SETTINGS),
).onEach { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
