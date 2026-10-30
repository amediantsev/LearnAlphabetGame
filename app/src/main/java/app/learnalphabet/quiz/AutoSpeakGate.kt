package app.learnalphabet.quiz

import app.learnalphabet.speech.SpeakerState

/**
 * Decides when a «Знайди» question speaks by itself: once per new question, as soon as the
 * voice is ready. Going to the background cancels it, so returning to the app stays quiet.
 */
class AutoSpeakGate {

    private var pending = false

    fun onNewQuestion() {
        pending = true
    }

    fun onBackground() {
        pending = false
    }

    /** True if the question should be spoken now. The attempt counts even if speech then fails. */
    fun consume(state: SpeakerState): Boolean {
        if (!pending || state != SpeakerState.READY) return false
        pending = false
        return true
    }
}
