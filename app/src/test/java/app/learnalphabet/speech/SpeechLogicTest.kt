package app.learnalphabet.speech

import android.speech.tts.TextToSpeech
import app.learnalphabet.alphabet.Ukrainian
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechLogicTest {

    @Test
    fun chooseEngine_prefersGoogle() =
        assertEquals(GOOGLE_TTS, chooseEngine(listOf("com.samsung.SMT", GOOGLE_TTS), "com.samsung.SMT"))

    @Test
    fun chooseEngine_fallsBackToDefault() = assertEquals("x.tts", chooseEngine(listOf("x.tts"), "x.tts"))

    @Test
    fun chooseEngine_noEngines_isNull() = assertNull(chooseEngine(emptyList(), null))

    @Test
    fun stateFor_available_isReady() =
        assertEquals(SpeakerState.READY, stateFor(TextToSpeech.LANG_COUNTRY_AVAILABLE))

    @Test
    fun stateFor_missingData_isMissingVoice() =
        assertEquals(SpeakerState.MISSING_VOICE, stateFor(TextToSpeech.LANG_MISSING_DATA))

    @Test
    fun stateFor_notSupported_isMissingVoice() =
        assertEquals(SpeakerState.MISSING_VOICE, stateFor(TextToSpeech.LANG_NOT_SUPPORTED))

    @Test
    fun utterances_letterNameThenWord() {
        val card = Ukrainian.letters[1]
        assertEquals(listOf("бе", "банан"), utterancesFor(card).map { it.text })
    }

    @Test
    fun utterances_firstUsesFlush() {
        val u = utterancesFor(Ukrainian.letters[0])
        assertTrue(u[0].flush)
        assertFalse(u[1].flush)
        assertTrue(u[0].pauseAfterMs in 300L..700L)
    }

    @Test
    fun utterances_useSpokenWordOverride() {
        val card = Ukrainian.letters[0].copy(spokenWord = "авто́бус")
        assertEquals("авто́бус", utterancesFor(card)[1].text)
    }

    @Test
    fun stateForError_notInstalled_isMissingVoice() =
        assertEquals(SpeakerState.MISSING_VOICE, stateForError(TextToSpeech.ERROR_NOT_INSTALLED_YET))

    @Test
    fun stateForError_network_isMissingVoice() {
        assertEquals(SpeakerState.MISSING_VOICE, stateForError(TextToSpeech.ERROR_NETWORK))
        assertEquals(SpeakerState.MISSING_VOICE, stateForError(TextToSpeech.ERROR_NETWORK_TIMEOUT))
    }

    @Test
    fun stateForError_transientError_keepsState() = assertNull(stateForError(TextToSpeech.ERROR_SYNTHESIS))

    @Test
    fun utterances_letterOnly_isJustTheLetter() {
        val u = utterancesFor(Ukrainian.letters[1], SpeechMode.LETTER_ONLY)
        assertEquals(listOf("бе"), u.map { it.text })
        assertTrue(u[0].flush)
        assertEquals(0L, u[0].pauseAfterMs)
    }

    @Test
    fun utterances_letterAndWord_isLetterThenWord() =
        assertEquals(listOf("бе", "банан"), utterancesFor(Ukrainian.letters[1], SpeechMode.LETTER_AND_WORD).map { it.text })

    @Test
    fun speechMode_default_isLetterAndWord() = assertEquals(SpeechMode.LETTER_AND_WORD, SpeechMode.DEFAULT)

    @Test
    fun speechMode_fromKey_roundTrips() =
        SpeechMode.entries.forEach { assertEquals(it, SpeechMode.fromKey(it.key)) }

    @Test
    fun speechMode_fromKey_unknownOrMissing_isDefault() {
        assertEquals(SpeechMode.DEFAULT, SpeechMode.fromKey(null))
        assertEquals(SpeechMode.DEFAULT, SpeechMode.fromKey("something-else"))
    }
}
