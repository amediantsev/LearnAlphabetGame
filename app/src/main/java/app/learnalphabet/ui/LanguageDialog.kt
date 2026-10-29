package app.learnalphabet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.learnalphabet.R
import app.learnalphabet.alphabet.Language
import app.learnalphabet.alphabet.Languages

@Composable
fun LanguageDialog(current: Language, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language_title)) },
        text = {
            Column {
                Languages.all.forEach { language ->
                    val enabled = language.available
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = enabled) { onSelect(language.code) }
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = language == current,
                            onClick = { onSelect(language.code) },
                            enabled = enabled,
                        )
                        val label = if (enabled) {
                            language.displayName
                        } else {
                            "${language.displayName} (${stringResource(R.string.coming_soon)})"
                        }
                        Text(label, color = if (enabled) Color.Unspecified else Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
    )
}
