package app.learnalphabet.speech

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import app.learnalphabet.R

/** The two short Find sounds: a right answer and a wrong tile. */
interface QuizSounds {
    fun right()
    fun wrong()
    fun release()
}

/**
 * Plays res/raw/quiz_success and quiz_fail (made by tools/make-quiz-sounds.py). SoundPool keeps
 * them decoded in memory, so they start at once and mix with speech instead of cutting it off.
 * They follow the phone's media volume, like the speech.
 */
class SoundPoolQuizSounds(context: Context) : QuizSounds {

    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    // Loaded when the app starts, long before the first tap, so the first sound is not lost.
    private val rightId = pool.load(context, R.raw.quiz_success, 1)
    private val wrongId = pool.load(context, R.raw.quiz_fail, 1)

    override fun right() {
        pool.play(rightId, 1f, 1f, 1, 0, 1f)
    }

    override fun wrong() {
        pool.play(wrongId, 1f, 1f, 1, 0, 1f)
    }

    override fun release() = pool.release()
}
