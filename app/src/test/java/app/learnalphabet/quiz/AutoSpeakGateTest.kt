package app.learnalphabet.quiz

import app.learnalphabet.speech.SpeakerState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoSpeakGateTest {

    @Test fun ready_speaksOncePerQuestion() {
        val g = AutoSpeakGate()
        g.onNewQuestion()
        assertTrue(g.consume(SpeakerState.READY))
        assertFalse(g.consume(SpeakerState.READY))
    }

    @Test fun initializing_waitsThenSpeaksWhenReady() {
        val g = AutoSpeakGate()
        g.onNewQuestion()
        assertFalse(g.consume(SpeakerState.INITIALIZING))
        assertTrue(g.consume(SpeakerState.READY))
    }

    @Test fun missingVoice_staysSilent() {
        val g = AutoSpeakGate()
        g.onNewQuestion()
        assertFalse(g.consume(SpeakerState.MISSING_VOICE))
    }

    @Test fun failedAttempt_countsAsSpoken() {
        val g = AutoSpeakGate()
        g.onNewQuestion()
        assertTrue(g.consume(SpeakerState.READY))
        assertFalse(g.consume(SpeakerState.MISSING_VOICE))
        assertFalse(g.consume(SpeakerState.READY))
    }

    @Test fun background_clearsPending() {
        val g = AutoSpeakGate()
        g.onNewQuestion()
        g.onBackground()
        assertFalse(g.consume(SpeakerState.READY))
    }

    @Test fun withoutQuestion_neverSpeaks() = assertFalse(AutoSpeakGate().consume(SpeakerState.READY))
}
