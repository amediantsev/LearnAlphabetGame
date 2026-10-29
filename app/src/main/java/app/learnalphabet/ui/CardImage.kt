package app.learnalphabet.ui

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.learnalphabet.alphabet.LetterCard
import app.learnalphabet.ui.theme.AbetkaColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/** Asset path of the card's picture, or null when the picture has not been added yet. */
fun imageAssetPath(language: String, card: LetterCard, exists: (String) -> Boolean): String? =
    "images/$language/${card.imageName}.webp".takeIf(exists)

private sealed interface Picture {
    data object Loading : Picture
    data object Missing : Picture
    data class Loaded(val bitmap: ImageBitmap) : Picture
}

private fun AssetManager.load(path: String?): ImageBitmap? = path?.let {
    try {
        open(it).use(BitmapFactory::decodeStream)?.asImageBitmap()
    } catch (e: IOException) {
        null
    }
}

private fun AssetManager.exists(path: String): Boolean =
    try {
        open(path).close()
        true
    } catch (e: IOException) {
        false
    }

@Composable
fun CardImage(languageCode: String, card: LetterCard, modifier: Modifier = Modifier) {
    val assets = LocalContext.current.assets
    val picture by produceState<Picture>(Picture.Loading, languageCode, card.imageName) {
        value = withContext(Dispatchers.IO) {
            assets.load(imageAssetPath(languageCode, card, assets::exists))
        }?.let { Picture.Loaded(it) } ?: Picture.Missing
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        when (val p = picture) {
            Picture.Loading -> Unit
            is Picture.Loaded -> Image(
                bitmap = p.bitmap,
                contentDescription = card.word,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24)),
            )
            Picture.Missing -> Placeholder(card)
        }
    }
}

@Composable
private fun Placeholder(card: LetterCard) {
    if (card.placeholder.isNotEmpty()) {
        Text(card.placeholder, fontSize = 140.sp)
    } else {
        Box(
            Modifier.fillMaxSize(0.7f).clip(RoundedCornerShape(32)).background(AbetkaColors.Tile),
            contentAlignment = Alignment.Center,
        ) {
            Text(card.upper, fontSize = 120.sp, fontWeight = FontWeight.ExtraBold, color = AbetkaColors.Letter)
        }
    }
}
