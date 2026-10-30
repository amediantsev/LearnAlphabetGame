package app.learnalphabet.ui

import app.learnalphabet.speech.SpeakerState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceHintTest {

    @Test fun tappedSpeechThatFails_showsHint() {
        val h = VoiceHint()
        h.onUserSpeak()
        assertTrue(h.onState(SpeakerState.MISSING_VOICE))
    }

    @Test fun autoSpeechAfterSuccessfulTap_failureShowsNoHint() {
        val h = VoiceHint()
        h.onUserSpeak() // worked: the state stays READY, so no state change arrives
        h.onAutoSpeak()
        assertFalse(h.onState(SpeakerState.MISSING_VOICE))
    }

    @Test fun failureWithoutAnyTap_showsNoHint() = assertFalse(VoiceHint().onState(SpeakerState.UNAVAILABLE))
}
