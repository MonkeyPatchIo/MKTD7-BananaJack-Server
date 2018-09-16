package io.monkeypatch.mktd7.bananajackserver.models


data class Player(val id: String, val name: String, val score: Int)


data class PlayerStatus(
    val hand: Hand = Hand(),
    val move: PlayerMove = hand.baseMove()
) {
    fun drawOne(cards: List<Card>): Pair<PlayerStatus, List<Card>> =
        if (move == Draw) PlayerStatus(hand + cards.first()) to cards.drop(1)
        else this to cards

    val canDo: List<PlayerMove> by lazy {
        when (move) {
            is Wait -> emptyList()
            InGame  ->
                if (hand.score == 21) emptyList()
                else listOf(Draw, Stay)
            Burst   -> emptyList()
            Draw    -> emptyList()
            Stay    -> emptyList()
            Timeout -> emptyList()
        }
    }
}

sealed class PlayerMove {
    fun name(): String =
        when (this) {
            Wait    -> "wait"
            InGame  -> "in-game"
            Burst   -> "burst"
            Draw    -> "draw"
            Stay    -> "stay"
            Timeout -> "timeout"
        }

    companion object {
        fun fromName(name: String): PlayerMove =
            when (name) {
                "wait"    -> Wait
                "in-game" -> InGame
                "burst"   -> Burst
                "draw"    -> Draw
                "stay"    -> Stay
                "timeout" -> Timeout
                else      -> throw IllegalArgumentException("Name $name does not match any PlayerMove")
            }
    }
}

object Wait : PlayerMove()
object InGame : PlayerMove()
object Burst : PlayerMove()
object Draw : PlayerMove()
object Stay : PlayerMove()
object Timeout : PlayerMove()
