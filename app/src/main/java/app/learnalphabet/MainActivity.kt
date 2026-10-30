package app.learnalphabet

import android.content.ActivityNotFoundException
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import app.learnalphabet.alphabet.Languages
import app.learnalphabet.speech.SpeechMode
import app.learnalphabet.speech.TtsCardSpeaker
import app.learnalphabet.speech.voiceSetupIntents
import app.learnalphabet.ui.CardScreen
import app.learnalphabet.ui.CardViewModel
import app.learnalphabet.ui.theme.AbetkaColors
import app.learnalphabet.ui.theme.AbetkaTheme

class MainActivity : ComponentActivity() {

    private lateinit var speaker: TtsCardSpeaker
    private val cards: CardViewModel by viewModels()
    private val prefs by lazy { getSharedPreferences(PREFS, MODE_PRIVATE) }
    private var speechMode by mutableStateOf(SpeechMode.DEFAULT)

    override fun onCreate(savedInstanceState: Bundle?) {
        val bar = AbetkaColors.Background.toArgb()
        enableEdgeToEdge(SystemBarStyle.light(bar, bar), SystemBarStyle.light(bar, bar))
        super.onCreate(savedInstanceState)
        speaker = TtsCardSpeaker(this, Languages.default.ttsLocale)
        speechMode = SpeechMode.fromKey(prefs.getString(KEY_SPEECH_MODE, null))
        setContent {
            AbetkaTheme {
                CardScreen(
                    cards,
                    speaker,
                    speechMode = speechMode,
                    onSpeechMode = ::saveSpeechMode,
                    onOpenTtsSettings = ::openTtsSettings,
                )
            }
        }
    }

    private fun saveSpeechMode(mode: SpeechMode) {
        speechMode = mode
        prefs.edit().putString(KEY_SPEECH_MODE, mode.key).apply()
    }

    private fun openTtsSettings() {
        for (intent in voiceSetupIntents()) {
            try {
                startActivity(intent)
                return
            } catch (e: ActivityNotFoundException) {
                // try the next screen
            }
        }
    }

    override fun onStart() {
        super.onStart()
        speaker.recheck()
    }

    override fun onStop() {
        super.onStop()
        speaker.stop()
        cards.onBackground()
    }

    override fun onDestroy() {
        speaker.release()
        super.onDestroy()
    }

    private companion object {
        const val PREFS = "settings"
        const val KEY_SPEECH_MODE = "speech_mode"
    }
}
