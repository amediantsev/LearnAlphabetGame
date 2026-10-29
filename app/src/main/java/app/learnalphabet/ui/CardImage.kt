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
import app.learnalphabet.alphabet.Language
import app.learnalphabet.alphabet.LetterCard
import app.learnalphabet.ui.theme.AbetkaColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/** Asset path of the card's picture (the file may not exist yet). */
fun imagePath(language: String, card: LetterCard): String = "images/$language/${card.imageName}.webp"

private sealed interface Picture {
    data object Loading : Picture
    data object Missing : Picture
    data class Loaded(val bitmap: ImageBitmap) : Picture
}

/** Decoded pictures stay in memory (33 × 512² is about 33 MB), so cards slide in already drawn. */
private object Pictures {
    @Volatile
    private var cache: MemoCache<String, ImageBitmap>? = null

    fun of(assets: AssetManager): MemoCache<String, ImageBitmap> =
        cache ?: synchronized(this) {
            cache ?: MemoCache<String, ImageBitmap> { path -> assets.decode(path) }.also { cache = it }
        }

    private fun AssetManager.decode(path: String): ImageBitmap? =
        try {
            open(path).use(BitmapFactory::decodeStream)?.asImageBitmap()
        } catch (e: IOException) {
            null // not added yet
        }
}

/** Decodes every picture of the language in the background so later cards show instantly. */
suspend fun preloadPictures(assets: AssetManager, language: Language) {
    val cache = Pictures.of(assets)
    withContext(Dispatchers.IO) {
        language.letters.forEach { cache.get(imagePath(language.code, it)) }
    }
}

@Composable
fun CardImage(languageCode: String, card: LetterCard, modifier: Modifier = Modifier) {
    val cache = Pictures.of(LocalContext.current.assets)
    val path = imagePath(languageCode, card)
    val initial = cache.peek(path)?.let { Picture.Loaded(it) }
        ?: if (cache.isKnownMissing(path)) Picture.Missing else Picture.Loading
    val picture by produceState(initial, path) {
        value = withContext(Dispatchers.IO) { cache.get(path) }?.let { Picture.Loaded(it) } ?: Picture.Missing
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
