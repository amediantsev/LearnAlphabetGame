package app.learnalphabet.quiz

import kotlin.random.Random

/** How one letter tile of a question looks and whether it still takes taps (only [OPEN] does). */
enum class OptionState { OPEN, WRONG, RIGHT, LOCKED }

/**
 * One «Знайди» question. Values are letter indices into the language's letters.
 * A wrong pick only removes that option; the question ends only when the answer is picked.
 */
data class QuizQuestion(
    val answer: Int,
    val options: List<Int>,
    val wrong: Set<Int> = emptySet(),
    val solved: Boolean = false,
) {
    fun pick(option: Int): QuizQuestion = when {
        solved || option in wrong || option !in options -> this
        option == answer -> copy(solved = true)
        else -> copy(wrong = wrong + option)
    }

    fun stateOf(option: Int): OptionState = when {
        solved && option == answer -> OptionState.RIGHT
        option in wrong -> OptionState.WRONG
        solved -> OptionState.LOCKED
        else -> OptionState.OPEN
    }
}

/** The answer plus up to [count] - 1 different distractors from [pool], in random places. */
fun newQuestion(answer: Int, pool: List<Int>, random: Random, count: Int = 4): QuizQuestion {
    val distractors = pool.distinct().filter { it != answer }.shuffled(random).take(count - 1)
    return QuizQuestion(answer, (distractors + answer).shuffled(random))
}
