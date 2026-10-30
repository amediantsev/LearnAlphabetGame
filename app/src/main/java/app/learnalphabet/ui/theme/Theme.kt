package app.learnalphabet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AbetkaColors {
    val Background = Color(0xFFFFF8EC)
    val Letter = Color(0xFFE4572E)
    val Arrow = Color(0xFF4C9F70)
    val Sound = Color(0xFFF4A259)
    val Tile = Color(0xFFFFE4C4)
    val Outline = Color(0xFFE3D6BF)
    val Text = Color(0xFF333333)
    val Right = Color(0xFF4C9F70)
    val WrongFill = Color(0xFFFDE2DD)
    val Wrong = Color(0xFFE4572E)
    val Disabled = Color(0xFFCFC6B6)
}

/** Always light: a bright picture-book look regardless of the phone's dark mode. */
@Composable
fun AbetkaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AbetkaColors.Arrow,
            secondary = AbetkaColors.Sound,
            background = AbetkaColors.Background,
            surface = Color.White,
            onBackground = AbetkaColors.Text,
            onSurface = AbetkaColors.Text,
        ),
        content = content,
    )
}
