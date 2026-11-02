package app.learnalphabet

import android.content.ActivityNotFoundException
import android.content.SharedPreferences
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
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.learnalphabet.alphabet.Languages
import app.learnalphabet.quiz.QuizLetterStore
import app.learnalphabet.speech.SoundPoolQuizSounds
import app.learnalphabet.speech.SpeechMode
import app.learnalphabet.speech.TtsCardSpeaker
import app.learnalphabet.speech.voiceSetupIntents
import app.learnalphabet.ui.CardScreen
import app.learnalphabet.ui.CardViewModel
import app.learnalphabet.ui.theme.AbetkaColors
import app.learnalphabet.ui.theme.AbetkaTheme

class MainActivity : ComponentActivity() {

    private lateinit var speaker: TtsCardSpeaker
    private lateinit var sounds: SoundPoolQuizSounds
    private val prefs by lazy { getSharedPreferences(PREFS, MODE_PRIVATE) }
    private val cards: CardViewModel by viewModels {
        viewModelFactory { initializer { CardViewModel(letterStore = PrefsLetterStore(prefs)) } }
    }
    private var speechMode by mutableStateOf(SpeechMode.DEFAULT)

    override fun onCreate(savedInstanceState: Bundle?) {
        val bar = AbetkaColors.Background.toArgb()
        enableEdgeToEdge(SystemBarStyle.light(bar, bar), SystemBarStyle.light(bar, bar))
        super.onCreate(savedInstanceState)
        speaker = TtsCardSpeaker(this, Languages.default.ttsLocale)
        sounds = SoundPoolQuizSounds(this)
        speechMode = SpeechMode.fromKey(prefs.getString(KEY_SPEECH_MODE, null))
        setContent {
            AbetkaTheme {
                CardScreen(
                    cards,
                    speaker,
                    sounds,
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
        sounds.release()
        super.onDestroy()
    }

    private companion object {
        const val PREFS = "settings"
        const val KEY_SPEECH_MODE = "speech_mode"
    }
}

/** Keeps the Find letter set in the app's settings file, one key per language. */
private class PrefsLetterStore(private val prefs: SharedPreferences) : QuizLetterStore {
    override fun load(languageCode: String): String? = prefs.getString(key(languageCode), null)

    override fun save(languageCode: String, raw: String) {
        prefs.edit().putString(key(languageCode), raw).apply()
    }

    private fun key(languageCode: String) = "quiz_letters_$languageCode"
}
