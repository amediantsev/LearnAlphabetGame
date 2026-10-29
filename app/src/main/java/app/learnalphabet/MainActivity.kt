package app.learnalphabet

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewmodel.compose.viewModel
import app.learnalphabet.alphabet.Languages
import app.learnalphabet.speech.TtsCardSpeaker
import app.learnalphabet.speech.ttsSettingsIntent
import app.learnalphabet.ui.CardScreen
import app.learnalphabet.ui.CardViewModel
import app.learnalphabet.ui.theme.AbetkaColors
import app.learnalphabet.ui.theme.AbetkaTheme

class MainActivity : ComponentActivity() {

    private lateinit var speaker: TtsCardSpeaker

    override fun onCreate(savedInstanceState: Bundle?) {
        val bar = AbetkaColors.Background.toArgb()
        enableEdgeToEdge(SystemBarStyle.light(bar, bar), SystemBarStyle.light(bar, bar))
        super.onCreate(savedInstanceState)
        speaker = TtsCardSpeaker(this, Languages.default.ttsLocale)
        setContent {
            AbetkaTheme {
                val vm: CardViewModel = viewModel()
                CardScreen(vm, speaker, onOpenTtsSettings = ::openTtsSettings)
            }
        }
    }

    private fun openTtsSettings() {
        try {
            startActivity(ttsSettingsIntent())
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    override fun onStop() {
        super.onStop()
        speaker.stop()
    }

    override fun onDestroy() {
        speaker.release()
        super.onDestroy()
    }
}
