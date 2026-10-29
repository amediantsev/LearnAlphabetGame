package app.learnalphabet.order

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import kotlin.random.Random

class CardNavigatorTest {

    @Test
    fun alphabet_startsAt_givenIndex() = assertEquals(5, AlphabetNavigator(33, 5).current)

    @Test
    fun alphabet_wrapsForward() {
        val n = AlphabetNavigator(33, 32)
        assertEquals(0, n.next())
    }

    @Test
    fun alphabet_wrapsBackward() {
        val n = AlphabetNavigator(33, 0)
        assertEquals(32, n.previous())
    }

    @Test
    fun shuffle_firstCard_isGivenStart() = assertEquals(7, ShuffleNavigator(33, 7, Random(1)).current)

    @Test
    fun shuffle_eachRound_showsEveryCardOnce() {
        val n = ShuffleNavigator(33, 0, Random(42))
        val round = listOf(n.current) + List(32) { n.next() }
        assertEquals((0 until 33).toSet(), round.toSet())
    }

    @Test
    fun shuffle_secondRound_alsoShowsEveryCardOnce() {
        val n = ShuffleNavigator(33, 0, Random(42))
        repeat(32) { n.next() }
        val round2 = List(33) { n.next() }
        assertEquals((0 until 33).toSet(), round2.toSet())
    }

    @Test
    fun shuffle_newRound_neverRepeatsLastCard() {
        repeat(200) { seed ->
            val n = ShuffleNavigator(5, 0, Random(seed))
            var last = n.current
            repeat(50) {
                val x = n.next()
                assertNotEquals(last, x)
                last = x
            }
        }
    }

    @Test
    fun shuffle_previous_walksBackThroughHistory() {
        val n = ShuffleNavigator(33, 0, Random(3))
        val a = n.current
        val b = n.next()
        n.next()
        assertEquals(b, n.previous())
        assertEquals(a, n.previous())
    }

    @Test
    fun shuffle_previous_atStart_staysPut() = assertEquals(4, ShuffleNavigator(33, 4, Random(3)).previous())

    @Test
    fun shuffle_nextAfterPrevious_replaysHistory() {
        val n = ShuffleNavigator(33, 0, Random(9))
        val b = n.next()
        n.previous()
        assertEquals(b, n.next())
    }

    @Test
    fun navigatorFor_buildsMatchingType() {
        assertEquals(AlphabetNavigator::class, navigatorFor(OrderMode.ALPHABET, 33, 2)::class)
        assertEquals(ShuffleNavigator::class, navigatorFor(OrderMode.SHUFFLE, 33, 2)::class)
        assertEquals(2, navigatorFor(OrderMode.SHUFFLE, 33, 2).current)
    }
}
