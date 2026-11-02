package app.learnalphabet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.learnalphabet.R
import app.learnalphabet.alphabet.Language
import app.learnalphabet.quiz.allLetters
import app.learnalphabet.quiz.canSaveLetters
import app.learnalphabet.quiz.toggleLetter
import app.learnalphabet.ui.theme.AbetkaColors

private const val Columns = 5
private val Gap = 8.dp
private val LetterTileShape = RoundedCornerShape(14.dp)

/**
 * Full-screen picker for the letters Find asks about. Edits a draft; «Готово» and the
 * system back button both hand the draft to [onDone], so leaving never loses a change.
 * An empty draft is allowed while choosing («Очистити»), but «Готово» is off then and
 * back leaves the previous set as it was.
 */
@Composable
fun LetterSetDialog(language: Language, selected: Set<String>, onDone: (Set<String>) -> Unit) {
    var draft by remember { mutableStateOf(selected) }
    val total = language.letters.size

    Dialog(
        onDismissRequest = { onDone(draft) },
        // decorFitsSystemWindows = false hands the system bar insets to safeDrawingPadding.
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(AbetkaColors.Background)
                .safeDrawingPadding()
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.quiz_letters_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AbetkaColors.Text,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { draft = emptySet() }) {
                    Text(stringResource(R.string.clear), color = AbetkaColors.Right, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { draft = allLetters(language) }) {
                    Text(stringResource(R.string.all), color = AbetkaColors.Right, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                if (canSaveLetters(draft)) {
                    stringResource(R.string.quiz_letters_count, draft.size, total)
                } else {
                    stringResource(R.string.quiz_letters_empty)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = AbetkaColors.Text.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(12.dp))
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val tile = (maxWidth - Gap * (Columns - 1)) / Columns
                Column(
                    verticalArrangement = Arrangement.spacedBy(Gap),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    language.letters.chunked(Columns).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(Gap)) {
                            row.forEach { card ->
                                LetterChoice(
                                    letter = card.upper,
                                    on = card.upper in draft,
                                    side = tile,
                                    onToggle = { draft = toggleLetter(draft, card.upper) },
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onDone(draft) },
                enabled = canSaveLetters(draft),
                colors = ButtonDefaults.buttonColors(containerColor = AbetkaColors.Right),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(stringResource(R.string.done), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LetterChoice(letter: String, on: Boolean, side: androidx.compose.ui.unit.Dp, onToggle: () -> Unit) {
    val description = stringResource(R.string.option_letter, letter)
    // Sized from the tile, not from sp, so a large system font cannot push it out of the tile.
    val fontSize = with(LocalDensity.current) { (side * 0.45f).toSp() }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(side)
            .clip(LetterTileShape)
            .background(if (on) AbetkaColors.Right else Color.White)
            .border(2.dp, if (on) AbetkaColors.Right else AbetkaColors.Outline, LetterTileShape)
            .toggleable(value = on, role = Role.Checkbox, onValueChange = { onToggle() })
            .semantics { contentDescription = description },
    ) {
        Text(
            letter,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = if (on) Color.White else AbetkaColors.Text,
            maxLines = 1,
            softWrap = false,
        )
    }
}
