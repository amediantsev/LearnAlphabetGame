package app.learnalphabet.speech

import android.content.Intent
import android.speech.tts.TextToSpeech
import app.learnalphabet.alphabet.LetterCard
import kotlinx.coroutines.flow.StateFlow

enum class SpeakerState { INITIALIZING, READY, MISSING_VOICE, UNAVAILABLE }

/** Speaks a card aloud. Kept behind an interface so recorded audio can replace synthesis later. */
interface CardSpeaker {
    val state: StateFlow<SpeakerState>
    fun speak(card: LetterCard)
    fun stop()
    fun release()
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

data class Utterance(val text: String, val flush: Boolean, val pauseAfterMs: Long = 0)

/** Letter name, a short pause, then the word. The first one cuts off anything still playing. */
fun utterancesFor(card: LetterCard): List<Utterance> = listOf(
    Utterance(card.spokenName, flush = true, pauseAfterMs = 450),
    Utterance(card.spokenWord ?: card.word, flush = false),
)

fun ttsSettingsIntent(): Intent =
    Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
