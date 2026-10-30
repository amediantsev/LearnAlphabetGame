package app.learnalphabet.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import app.learnalphabet.R
import app.learnalphabet.quiz.OptionState
import app.learnalphabet.ui.theme.AbetkaColors

private val TileGap = 16.dp
private val WordSlot = 56.dp
private val TileShape = RoundedCornerShape(24)

/** One «Знайди» question: picture, the word slot (filled once solved) and four letter tiles. */
@Composable
fun QuizContent(state: CardUiState, onPick: (Int) -> Unit, onSpeak: () -> Unit, modifier: Modifier = Modifier) {
    val quiz = state.quiz ?: return
    BoxWithConstraints(modifier.padding(horizontal = CardPadding, vertical = 8.dp)) {
        val picture = min(maxWidth * 0.45f, maxHeight * 0.28f)
        val gridHeight = maxHeight - picture - WordSlot - 24.dp
        val tile = min((maxWidth - TileGap) / 2, (gridHeight - TileGap) / 2)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize(),
        ) {
            CardImage(
                languageCode = state.language.code,
                card = state.card,
                modifier = Modifier.size(picture).clickable(onClick = onSpeak),
            )
            WordSlot(state, visible = quiz.solved)
            Column(verticalArrangement = Arrangement.spacedBy(TileGap)) {
                quiz.options.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(TileGap)) {
                        row.forEach { option ->
                            LetterTile(
                                letter = state.language.letters[option].upper,
                                state = quiz.stateOf(option),
                                faded = quiz.solved && option != quiz.answer,
                                side = tile,
                                onClick = { onPick(option) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Keeps the word's space from the start so nothing moves when the word appears. */
@Composable
private fun WordSlot(state: CardUiState, visible: Boolean) {
    Box(Modifier.height(WordSlot), contentAlignment = Alignment.Center) {
        AnimatedVisibility(visible = visible, enter = fadeIn()) {
            WordText(state.card, highlight = AbetkaColors.Letter, fontSize = 40.sp)
        }
    }
}

@Composable
private fun LetterTile(letter: String, state: OptionState, faded: Boolean, side: Dp, onClick: () -> Unit) {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(state) {
        if (state == OptionState.WRONG) {
            shake.animateTo(0f, keyframes {
                durationMillis = 300
                -8f at 50
                8f at 100
                -6f at 150
                6f at 200
                -3f at 250
            })
        }
    }
    val scale by animateFloatAsState(if (state == OptionState.RIGHT) 1.06f else 1f, label = "tileScale")
    val alpha = when {
        faded -> 0.3f
        state == OptionState.WRONG -> 0.55f
        else -> 1f
    }
    val (fill, border, text) = when (state) {
        OptionState.WRONG -> Triple(AbetkaColors.WrongFill, AbetkaColors.Wrong, AbetkaColors.Wrong)
        OptionState.RIGHT -> Triple(AbetkaColors.Right, AbetkaColors.Right, Color.White)
        OptionState.OPEN, OptionState.LOCKED -> Triple(Color.White, AbetkaColors.Outline, AbetkaColors.Text)
    }
    // Sized from the tile, not from sp, so a large system font cannot push it out of the tile.
    val letterSize = with(LocalDensity.current) { (side * 0.6f).toSp() }
    val description = stringResource(R.string.option_letter, letter)

    Box(
        Modifier
            .size(side)
            .offset { IntOffset(shake.value.dp.roundToPx(), 0) }
            .scale(scale),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                // Alpha per part, not on the whole tile: a faded layer would clip the badge.
                .alpha(alpha)
                .clip(TileShape)
                .background(fill)
                .border(2.dp, border, TileShape)
                .clickable(enabled = state == OptionState.OPEN, onClick = onClick)
                .semantics { contentDescription = description },
        ) {
            Text(letter, fontSize = letterSize, fontWeight = FontWeight.ExtraBold, color = text, maxLines = 1, softWrap = false)
        }
        when (state) {
            OptionState.WRONG -> Badge("✕", AbetkaColors.Wrong, Color.White, alpha)
            OptionState.RIGHT -> Badge("✓", Color.White, AbetkaColors.Right, 1f)
            else -> Unit
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.Badge(symbol: String, background: Color, color: Color, alpha: Float) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 8.dp, y = (-8).dp)
            .size(30.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(background)
            .border(2.dp, AbetkaColors.Right.takeIf { background == Color.White } ?: background, CircleShape),
    ) {
        Text(symbol, color = color, fontWeight = FontWeight.ExtraBold, fontSize = with(LocalDensity.current) { 16.dp.toSp() })
    }
}
