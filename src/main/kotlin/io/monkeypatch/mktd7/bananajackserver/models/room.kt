package io.monkeypatch.mktd7.bananajackserver.models

import io.monkeypatch.mktd7.bananajackserver.peek
import io.monkeypatch.mktd7.bananajackserver.services.PlayerService

data class PlayerWithStatus(val player: Player, val status: PlayerStatus)

data class Room(
    val id: Int,
    val name: String,
    val players: List<PlayerWithStatus> = emptyList(),
    val bank: Hand = Hand()
) {

    val full: Boolean by lazy { players.size >= 4 }

    fun canBeginTurn(): Boolean =
        players.any { it.status.move == InGame }

    fun cleanBeforeTurn(): Room =
        copy(players = players.asSequence()
            .filterNot { it.status.move == Timeout }
            .map { (p, _) ->
                PlayerWithStatus(
                    PlayerService[p.id],
                    PlayerStatus(
                        Hand(),
                        InGame
                    )
                )
            }
            .toList()
        )

    fun join(player: Player): Room =
        if (players.any { it.player.id == player.id }) this
        else this.copy(players = players + PlayerWithStatus(
            player,
            PlayerStatus()
        )
        )

    fun leave(player: Player): Room =
        this.copy(players = players.filter { it.player.id == player.id })


    fun action(player: Player, action: PlayerMove): Room =
        copy(players = players
            .map { (p, s) ->
                if (p.id == player.id) PlayerWithStatus(
                    p,
                    s.copy(move = action)
                )
                else PlayerWithStatus(p, s)
            })

    fun bankAction(): PlayerMove {
        // What a smart IA ?
        return if (players.any { it.status.move == Draw }) {
            when (bank.score) {
                in 0..16 -> Draw
                in 17..20 -> Stay
                21 -> Stay // BananaJack !
                else -> Burst
            }
        } else {
            val playerBestScore = scores()
                .filter { it.second <= 21 }
                .maxBy { it.second }?.second ?: -1
            when {
                bank.score < playerBestScore -> Draw
                bank.score > 21              -> Burst
                else                         -> Stay
            }
        }
    }

    fun listDrawing(): List<PlayerWithStatus> =
        players.filter { it.status.move == Draw }

    fun draw(cards: List<List<Card>>): Room =
        copy(bank = Hand(cards.first()),
             players = cards.drop(1)
                 .zip(players.toList())
                 .map { (cards, playerStatus) ->
                     val (player, _) = playerStatus
                     val hand = Hand(cards)
                     PlayerWithStatus(
                         player,
                         PlayerStatus(
                             hand,
                             hand.baseMove()
                         )
                     )
                 }
        )


    fun playerDraw(cardPile: List<Card>): Room =
        copy(
            players = players
                .fold(emptyList<PlayerWithStatus>() to cardPile) { (list, cards), (player, status) ->
                    val (newStatus, newCards) = if (status.move == Draw) {
                        val (card, cardTails) = cards.peek()
                        val newHand = status.hand + card
                        val move = newHand.baseMove()
                        status.copy(hand = newHand, move = move) to cardTails
                    } else {
                        val newMove = when (status.move) {
                            InGame -> Timeout
                            else                                                -> status.move
                        }
                        status.copy(move = newMove) to cards
                    }

                    (list + PlayerWithStatus(player, newStatus)) to newCards
                }.first
        )

    fun scores(): List<Pair<String, Int>> =
        players.asSequence()
            .filter { (_, status) -> status.move == Stay || status.move == InGame }
            .map { (player, status) -> player.id to status.hand.score }
            .toList()

    fun updatePlayers(): Room =
        copy(players = players.map { (player, status) ->
            PlayerWithStatus(
                PlayerService[player.id],
                status.copy(move = InGame)
            )
        })
}
