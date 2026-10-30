package app.learnalphabet.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.learnalphabet.R
import app.learnalphabet.alphabet.Language
import app.learnalphabet.alphabet.LetterCard
import app.learnalphabet.order.OrderMode
import app.learnalphabet.speech.CardSpeaker
import app.learnalphabet.speech.SpeakerState
import app.learnalphabet.ui.theme.AbetkaColors

private val SwipeThreshold = 80.dp
private val CardPadding = 24.dp
private val LetterStyle = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 100.sp)

private fun letterPair(card: LetterCard) = "${card.upper} ${card.lower}"

/**
 * One letter size for every card of the language: as big as the widest pair ("Ж ж", "Щ щ")
 * allows within [maxWidth], and no taller than [maxHeight]. Follows the phone's font scale.
 */
@Composable
private fun rememberLetterSize(language: Language, maxWidth: Dp, maxHeight: Dp): TextUnit {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(language, maxWidth, maxHeight, density) {
        val measured = language.letters.map { measurer.measure(letterPair(it), LetterStyle, softWrap = false).size }
        val scale = with(density) {
            letterScale(
                widest = measured.maxOf { it.width }.toFloat(),
                lineHeight = measured.maxOf { it.height }.toFloat(),
                maxWidth = maxWidth.toPx(),
                maxHeight = maxHeight.toPx(),
            )
        }
        LetterStyle.fontSize * scale
    }
}

@Composable
fun CardScreen(viewModel: CardViewModel, speaker: CardSpeaker, onOpenTtsSettings: () -> Unit) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val speakerState by speaker.state.collectAsStateWithLifecycle()
    var showLanguages by remember { mutableStateOf(false) }
    var showVoiceHelp by remember { mutableStateOf(false) }
    // True between a tap on 🔊 and the speaker's answer, so a failure that is only reported
    // while speaking (voice not downloaded, offline) still brings up the hint.
    var speechRequested by remember { mutableStateOf(false) }
    val assets = LocalContext.current.assets

    LaunchedEffect(ui.language.code) { preloadPictures(assets, ui.language) }
    LaunchedEffect(speakerState) {
        if (speechRequested && speakerState.isBroken()) showVoiceHelp = true
        speechRequested = false
    }

    val speak = {
        when (speakerState) {
            SpeakerState.READY -> {
                speechRequested = true
                speaker.speak(ui.card)
            }
            SpeakerState.MISSING_VOICE, SpeakerState.UNAVAILABLE -> showVoiceHelp = true
            SpeakerState.INITIALIZING -> Unit
        }
    }
    val goNext = { speaker.stop(); viewModel.next() }
    val goPrevious = { speaker.stop(); viewModel.previous() }

    Column(
        Modifier
            .fillMaxSize()
            .background(AbetkaColors.Background)
            .safeDrawingPadding()
            .swipe(onLeft = goNext, onRight = goPrevious),
    ) {
        TopBar(mode = ui.mode, onMode = viewModel::setMode, onSettings = { showLanguages = true })

        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val contentWidth = maxWidth - CardPadding * 2
            val letterSize = rememberLetterSize(ui.language, contentWidth, maxHeight * LetterHeightShare)
            val pictureSize = pictureSide(contentWidth.value, maxHeight.value).dp

            AnimatedContent(
                targetState = ui,
                contentKey = { it.language.code to it.index },
                transitionSpec = {
                    val dir = targetState.direction
                    (slideInHorizontally { w -> dir * w } + fadeIn()) togetherWith
                        (slideOutHorizontally { w -> -dir * w } + fadeOut())
                },
                label = "card",
                modifier = Modifier.fillMaxSize(),
            ) { state ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxSize().padding(horizontal = CardPadding),
                ) {
                    Text(
                        letterPair(state.card),
                        style = LetterStyle.copy(fontSize = letterSize),
                        color = AbetkaColors.Letter,
                        maxLines = 1,
                        softWrap = false,
                    )
                    CardImage(
                        languageCode = state.language.code,
                        card = state.card,
                        modifier = Modifier
                            .size(pictureSize)
                            .clickable(onClick = speak),
                    )
                    WordText(state.card, highlight = AbetkaColors.Letter, fontSize = 40.sp)
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        ) {
            RoundButton("◀", AbetkaColors.Arrow, 52.dp, stringResource(R.string.previous), goPrevious)
            RoundButton("🔊", AbetkaColors.Sound, 64.dp, stringResource(R.string.speak), speak)
            RoundButton("▶", AbetkaColors.Arrow, 52.dp, stringResource(R.string.next), goNext)
        }
    }

    if (showLanguages) {
        LanguageDialog(
            current = ui.language,
            onSelect = { viewModel.setLanguage(it); showLanguages = false },
            onDismiss = { showLanguages = false },
        )
    }
    if (showVoiceHelp) {
        AlertDialog(
            onDismissRequest = { showVoiceHelp = false },
            title = { Text(stringResource(R.string.voice_missing_title)) },
            text = { Text(stringResource(R.string.voice_missing)) },
            confirmButton = {
                TextButton(onClick = { showVoiceHelp = false; onOpenTtsSettings() }) {
                    Text(stringResource(R.string.open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showVoiceHelp = false }) { Text(stringResource(R.string.close)) }
            },
        )
    }
}

private fun SpeakerState.isBroken() = this == SpeakerState.MISSING_VOICE || this == SpeakerState.UNAVAILABLE

@Composable
private fun TopBar(mode: OrderMode, onMode: (OrderMode) -> Unit, onSettings: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .border(1.dp, AbetkaColors.Outline, RoundedCornerShape(50))
                .background(Color.White),
        ) {
            ModeChip(stringResource(R.string.mode_alphabet), mode == OrderMode.ALPHABET) { onMode(OrderMode.ALPHABET) }
            ModeChip(stringResource(R.string.mode_shuffle), mode == OrderMode.SHUFFLE) { onMode(OrderMode.SHUFFLE) }
        }
        Spacer(Modifier.weight(1f))
        val settings = stringResource(R.string.settings)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(onClick = onSettings)
                .semantics { contentDescription = settings },
        ) {
            Text("⚙️", fontSize = 24.sp)
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        fontSize = 16.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) Color.White else AbetkaColors.Text,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) AbetkaColors.Arrow else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
private fun RoundButton(symbol: String, color: Color, size: Dp, description: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
    ) {
        Text(symbol, fontSize = (size.value * 0.42f).sp, color = Color.White)
    }
}

@Composable
private fun Modifier.swipe(onLeft: () -> Unit, onRight: () -> Unit): Modifier {
    val thresholdPx = with(LocalDensity.current) { SwipeThreshold.toPx() }
    val left by rememberUpdatedState(onLeft)
    val right by rememberUpdatedState(onRight)
    return pointerInput(thresholdPx) {
        var total = 0f
        detectHorizontalDragGestures(
            onDragStart = { total = 0f },
            onDragEnd = {
                when {
                    total < -thresholdPx -> left()
                    total > thresholdPx -> right()
                }
            },
        ) { change, dx ->
            change.consume()
            total += dx
        }
    }
}
