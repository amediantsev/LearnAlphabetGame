package app.learnalphabet.quiz

import kotlin.random.Random

/**
 * Order of «Знайди» questions: random rounds where every letter of [pool] comes once,
 * and a new round never starts with the letter that was just asked.
 */
class QuizDeck(pool: List<Int>, private val random: Random) {

    private val pool = pool.distinct()
    private val round = ArrayDeque<Int>()
    private var last: Int? = null

    init {
        require(this.pool.isNotEmpty()) { "pool must not be empty" }
    }

    /**
     * The next letter to ask. [avoid] (e.g. the card the child just saw) is skipped while
     * other letters are left in this round; it stays in the round for later.
     */
    fun next(avoid: Int? = null): Int {
        if (round.isEmpty()) newRound()
        val at = round.indexOfFirst { it != avoid }.takeIf { it >= 0 } ?: 0
        return round.removeAt(at).also { last = it }
    }

    private fun newRound() {
        val items = pool.shuffled(random).toMutableList()
        if (items.size > 1 && items[0] == last) {
            val swapWith = 1 + random.nextInt(items.size - 1)
            items[0] = items[swapWith].also { items[swapWith] = items[0] }
        }
        round.addAll(items)
    }
}
