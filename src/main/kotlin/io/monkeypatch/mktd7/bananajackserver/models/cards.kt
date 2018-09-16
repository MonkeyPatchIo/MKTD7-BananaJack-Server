package io.monkeypatch.mktd7.bananajackserver.models


data class Card(val key: String, val value: CardValue, val suit: CardSuit)

sealed class CardValue {

    abstract fun score(): List<Int>
    abstract val name: String

    companion object {
        operator fun get(s: String): CardValue = when (s) {
            "ACE"   -> Ace
            "KING"  -> King
            "QUEEN" -> Queen
            "JACK"  -> Jack
            "10"    -> BasicValue(10)
            "9"     -> BasicValue(9)
            "8"     -> BasicValue(8)
            "7"     -> BasicValue(7)
            "6"     -> BasicValue(6)
            "5"     -> BasicValue(5)
            "4"     -> BasicValue(4)
            "3"     -> BasicValue(3)
            "2"     -> BasicValue(2)
            else    -> throw IllegalArgumentException("Unexpected card value: $s")
        }
    }
}

object Ace : CardValue() {
    override fun score(): List<Int> = listOf(1, 11)
    override val name: String = "ace"
}

object King : CardValue() {
    override fun score(): List<Int> = listOf(10)
    override val name: String = "king"
}

object Queen : CardValue() {
    override fun score(): List<Int> = listOf(10)
    override val name: String = "queen"
}

object Jack : CardValue() {
    override fun score(): List<Int> = listOf(10)
    override val name: String = "jack"
}

data class BasicValue(val value: Int) : CardValue() {
    init {
        require(value in 2..10)
    }

    override fun score(): List<Int> = listOf(value)
    override val name: String = value.toString()
}


sealed class CardSuit {
    val name: String by lazy {
        when (this) {
            Spades   -> "spades"
            Hearts   -> "hearts"
            Diamonds -> "diamonds"
            Clubs    -> "clubs"
        }
    }

    companion object {

        operator fun get(suit: String): CardSuit =
            when (suit) {
                "SPADES"   -> Spades
                "DIAMONDS" -> Diamonds
                "CLUBS"    -> Clubs
                "HEARTS"   -> Hearts
                else       -> throw IllegalArgumentException("Unexpected card suit: $suit")
            }

    }
}

object Spades : CardSuit()
object Hearts : CardSuit()
object Diamonds : CardSuit()
object Clubs : CardSuit()


data class Hand(val cards: List<Card> = emptyList()) {
    val score: Int by lazy {
        val scores = cards.fold(listOf(0)) { lst, card ->
            lst.flatMap { s -> card.value.score().map { it + s } }
        }
        val (upper, lower) = scores.partition { it > 21 }

        // explains usage of !! here:
        // . scores contains at least an element (fold seed, and card value is size 1 or 2)
        // . so is lower isEmpty the upper is not empty
        if (lower.isEmpty()) upper.min()!! else lower.max()!!
    }

    operator fun plus(card: Card): Hand =
        copy(cards = cards + card)

    fun baseMove(): PlayerMove =
        if (cards.isEmpty()) Wait
        else when (score) {
            in 0..20 -> InGame
            21       -> Stay // BananaJack !
            else     -> Burst
        }
}

