package app.learnalphabet.ui

import app.learnalphabet.speech.SpeakerState

/**
 * Decides when to show «Немає українського голосу»: only when speech the child or parent asked
 * for (🔊 or a tap on the picture) fails. Speech that started by itself never opens it.
 */
class VoiceHint {

    private var requested = false

    fun onUserSpeak() {
        requested = true
    }

    fun onAutoSpeak() {
        requested = false
    }

    /** The speaker's state changed; true means show the hint. */
    fun onState(state: SpeakerState): Boolean {
        val show = requested && state.isBroken()
        requested = false
        return show
    }
}

fun SpeakerState.isBroken() = this == SpeakerState.MISSING_VOICE || this == SpeakerState.UNAVAILABLE
