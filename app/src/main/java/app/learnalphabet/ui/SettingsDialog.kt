package app.learnalphabet.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.learnalphabet.R
import app.learnalphabet.alphabet.Language
import app.learnalphabet.alphabet.Languages
import app.learnalphabet.speech.SpeechMode

/** The ⚙️ dialog for the parent: language and what a card says aloud. */
@Composable
fun SettingsDialog(
    language: Language,
    onLanguage: (String) -> Unit,
    speechMode: SpeechMode,
    onSpeechMode: (SpeechMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SectionTitle(stringResource(R.string.language_title))
                Languages.all.forEach { option ->
                    val label = if (option.available) {
                        option.displayName
                    } else {
                        "${option.displayName} (${stringResource(R.string.coming_soon)})"
                    }
                    Choice(label, selected = option == language, enabled = option.available) { onLanguage(option.code) }
                }
                Spacer(Modifier.height(12.dp))
                SectionTitle(stringResource(R.string.speech_title))
                Choice(stringResource(R.string.speech_letter_and_word), speechMode == SpeechMode.LETTER_AND_WORD) {
                    onSpeechMode(SpeechMode.LETTER_AND_WORD)
                }
                Choice(stringResource(R.string.speech_letter_only), speechMode == SpeechMode.LETTER_ONLY) {
                    onSpeechMode(SpeechMode.LETTER_ONLY)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
}

@Composable
private fun Choice(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Text(label, color = if (enabled) Color.Unspecified else Color.Gray, modifier = Modifier.padding(start = 8.dp))
    }
}
