package app.learnalphabet.order

import kotlin.random.Random

enum class OrderMode { ALPHABET, SHUFFLE }

/** Walks over card indices `0 until size`. */
interface CardNavigator {
    val current: Int
    fun next(): Int
    fun previous(): Int
}

fun navigatorFor(mode: OrderMode, size: Int, start: Int): CardNavigator = when (mode) {
    OrderMode.ALPHABET -> AlphabetNavigator(size, start)
    OrderMode.SHUFFLE -> ShuffleNavigator(size, start)
}

/** Alphabet order, wrapping from the last card to the first and back. */
class AlphabetNavigator(private val size: Int, start: Int = 0) : CardNavigator {
    init {
        require(size > 0) { "size must be positive" }
    }

    override var current: Int = start.mod(size)
        private set

    override fun next(): Int {
        current = (current + 1).mod(size)
        return current
    }

    override fun previous(): Int {
        current = (current - 1).mod(size)
        return current
    }
}

/**
 * Random order in rounds: every card appears once per round, and a new round never
 * starts with the card that was just shown. [previous] walks back through what was shown.
 */
class ShuffleNavigator(
    private val size: Int,
    start: Int = 0,
    private val random: Random = Random.Default,
) : CardNavigator {
    init {
        require(size > 0) { "size must be positive" }
    }

    private val history = mutableListOf(start.mod(size))
    private var pos = 0
    private val deck = ArrayDeque((0 until size).filter { it != history[0] }.shuffled(random))

    override val current: Int get() = history[pos]

    override fun next(): Int {
        if (pos < history.lastIndex) {
            pos++
            return current
        }
        if (deck.isEmpty()) newRound(last = current)
        history += deck.removeFirst()
        pos++
        return current
    }

    override fun previous(): Int {
        if (pos > 0) pos--
        return current
    }

    private fun newRound(last: Int) {
        val round = (0 until size).shuffled(random).toMutableList()
        if (size > 1 && round[0] == last) {
            val swapWith = 1 + random.nextInt(size - 1)
            round[0] = round[swapWith].also { round[swapWith] = round[0] }
        }
        deck.addAll(round)
    }
}
