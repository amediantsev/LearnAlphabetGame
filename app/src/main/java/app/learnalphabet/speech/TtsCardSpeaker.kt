package app.learnalphabet.speech

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import app.learnalphabet.alphabet.LetterCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TtsCardSpeaker(context: Context, private val locale: Locale) : CardSpeaker {

    private val app = context.applicationContext
    private val _state = MutableStateFlow(SpeakerState.INITIALIZING)
    override val state: StateFlow<SpeakerState> = _state

    private var tts: TextToSpeech? = null

    init {
        connect()
    }

    private fun connect() {
        _state.value = SpeakerState.INITIALIZING
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
        engine.setOnUtteranceProgressListener(ErrorListener())
        _state.value = stateFor(engine.setLanguage(locale))
    }

    override fun speak(card: LetterCard, mode: SpeechMode) {
        val engine = tts ?: return
        if (_state.value != SpeakerState.READY) return
        utterancesFor(card, mode).forEachIndexed { i, u ->
            val mode = if (u.flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            if (engine.speak(u.text, mode, null, "card-$i") == TextToSpeech.ERROR) {
                _state.value = SpeakerState.UNAVAILABLE
                return
            }
            if (u.pauseAfterMs > 0) engine.playSilentUtterance(u.pauseAfterMs, TextToSpeech.QUEUE_ADD, "pause-$i")
        }
    }

    override fun recheck() {
        when (_state.value) {
            SpeakerState.MISSING_VOICE -> tts?.let { _state.value = stateFor(it.setLanguage(locale)) }
            SpeakerState.UNAVAILABLE -> {
                tts?.shutdown()
                connect()
            }
            SpeakerState.READY, SpeakerState.INITIALIZING -> Unit
        }
    }

    override fun stop() {
        tts?.stop()
    }

    override fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    /** Runs on a binder thread; MutableStateFlow is thread-safe. */
    private inner class ErrorListener : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit
        override fun onDone(utteranceId: String?) = Unit

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) = Unit

        override fun onError(utteranceId: String?, errorCode: Int) {
            stateForError(errorCode)?.let { _state.value = it }
        }
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
