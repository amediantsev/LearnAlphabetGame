package app.learnalphabet.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import app.learnalphabet.alphabet.LetterCard

/** The one character of the word that matches the card's letter. */
fun highlightRange(card: LetterCard): IntRange = card.highlightIndex..card.highlightIndex

@Composable
fun WordText(card: LetterCard, highlight: Color, fontSize: TextUnit, modifier: Modifier = Modifier) {
    val range = highlightRange(card)
    val text = buildAnnotatedString {
        append(card.word.substring(0, range.first))
        withStyle(SpanStyle(color = highlight, fontWeight = FontWeight.ExtraBold)) {
            append(card.word.substring(range.first, range.last + 1))
        }
        append(card.word.substring(range.last + 1))
    }
    Text(text, modifier = modifier, fontSize = fontSize, fontWeight = FontWeight.SemiBold)
}
