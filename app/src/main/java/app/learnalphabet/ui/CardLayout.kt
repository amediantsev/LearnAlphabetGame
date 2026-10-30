package app.learnalphabet.ui

import kotlin.math.min

/** Share of the card's height the letter line may take. The letter is the main thing on the card. */
const val LetterHeightShare = 0.4f

/** The picture is a hint, not the focus: at most half the width and about a third of the height. */
private const val PictureWidthShare = 0.5f
private const val PictureHeightShare = 0.32f

/**
 * How much to scale text measured at a reference size so the widest letter pair of the
 * alphabet fits [maxWidth] and its line fits [maxHeight]. All values in the same units (px).
 */
fun letterScale(widest: Float, lineHeight: Float, maxWidth: Float, maxHeight: Float): Float =
    min(maxWidth / widest, maxHeight / lineHeight)

/** Side of the square picture for a card area of [maxWidth] x [maxHeight]. */
fun pictureSide(maxWidth: Float, maxHeight: Float): Float =
    min(maxWidth * PictureWidthShare, maxHeight * PictureHeightShare)
