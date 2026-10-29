package app.learnalphabet.speech

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.speech.tts.TextToSpeech
import app.learnalphabet.alphabet.LetterCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TtsCardSpeaker(context: Context, private val locale: Locale) : CardSpeaker {

    private val _state = MutableStateFlow(SpeakerState.INITIALIZING)
    override val state: StateFlow<SpeakerState> = _state

    private val tts: TextToSpeech?

    init {
        val app = context.applicationContext
        val engine = chooseEngine(installedEngines(app), defaultEngine(app))
        tts = try {
            TextToSpeech(app, ::onInit, engine)
        } catch (e: RuntimeException) {
            _state.value = SpeakerState.UNAVAILABLE
            null
        }
    }

    private fun onInit(status: Int) {
        // Can run inside the TextToSpeech constructor on immediate bind failure, before `tts` is set.
        if (status != TextToSpeech.SUCCESS) {
            _state.value = SpeakerState.UNAVAILABLE
            return
        }
        val engine = tts ?: return
        engine.setSpeechRate(SPEECH_RATE)
        _state.value = stateFor(engine.setLanguage(locale))
    }

    override fun speak(card: LetterCard) {
        val engine = tts ?: return
        if (_state.value != SpeakerState.READY) return
        utterancesFor(card).forEachIndexed { i, u ->
            val mode = if (u.flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            engine.speak(u.text, mode, null, "card-$i")
            if (u.pauseAfterMs > 0) engine.playSilentUtterance(u.pauseAfterMs, TextToSpeech.QUEUE_ADD, "pause-$i")
        }
    }

    override fun stop() {
        tts?.stop()
    }

    override fun release() {
        tts?.stop()
        tts?.shutdown()
    }

    private companion object {
        const val SPEECH_RATE = 0.85f

        fun installedEngines(context: Context): List<String> =
            context.packageManager
                .queryIntentServices(Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE), 0)
                .map { it.serviceInfo.packageName }

        fun defaultEngine(context: Context): String? =
            Settings.Secure.getString(context.contentResolver, "tts_default_synth")
    }
}
