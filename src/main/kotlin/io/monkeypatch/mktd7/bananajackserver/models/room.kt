package io.monkeypatch.mktd7.bananajackserver.models

import io.monkeypatch.mktd7.bananajackserver.services.PlayerService

data class PlayerWithStatus(val player: Player, val status: PlayerStatus)

data class Room(
    val id: Int,
    val name: String,
    val players: List<PlayerWithStatus> = emptyList(),
    val bank: PlayerStatus = PlayerStatus(move = InGame)
) {

    val full: Boolean by lazy { players.size >= 4 }

    fun canBeginTurn(): Boolean =
        players.any { it.status.move == InGame }

    fun cleanBeforeTurn(): Room =
        copy(
            bank = bank.copy(hand = Hand()),
            players =
             players.map { (p, _) ->
                 PlayerWithStatus(PlayerService[p.id], PlayerStatus(Hand(), InGame))
             }
        )

    fun join(player: Player): Room =
        if (players.any { it.player.id == player.id }) this
        else this.copy(
            players = players + PlayerWithStatus(player, PlayerStatus())
        )

    fun leave(player: Player): Room =
        this.copy(players = players.filter { it.player.id == player.id })


    fun action(player: Player, action: PlayerMove): Room =
        copy(players = players
            .map { (p, s) ->
                if (p.id == player.id) PlayerWithStatus(p, s.copy(move = action))
                else PlayerWithStatus(p, s)
            })

    fun bankAction(): PlayerMove {
        val baseIA = { score: Int ->
            // What a smart IA ?
            when (score) {
                in 0..16  -> Draw
                in 17..20 -> Stay
                21        -> Stay // BananaJack !
                else      -> Burst
            }
        }

        val anyDraw = players.any { it.status.move == Draw }
        return when (bank.move) {
            is Stay   -> Stay
            is InGame ->
                if (anyDraw) baseIA(bank.hand.score)
                else {
                    val playerBestScore = scores()
                        .filter { it.second <= 21 }
                        .maxBy { it.second }?.second ?: -1
                    when {
                        bank.hand.score > 21              -> Burst
                        bank.hand.score < playerBestScore -> Draw
                        else                              -> Stay
                    }
                }
            else      -> InGame
        }
    }

    fun listDrawing(): List<PlayerWithStatus> =
        players.filter { it.status.move == Draw }

    fun drawHands(hands: List<Hand>): Room =
        copy(
            bank = bank.copy(hand = hands.first()),
            players = hands.drop(1)
                .zip(players.toList())
                .map { (hand, ps) -> ps.copy(status = PlayerStatus(hand)) }
        )

    fun playerDraw(cards: List<Card>, bankDraw: Boolean): Room {
        val (cardPile, newBank) =
                if (bankDraw) (cards.drop(1) to PlayerStatus(bank.hand + cards.first()))
                else (cards to bank.copy(move = Stay))

        val initial = emptyList<PlayerWithStatus>() to cardPile
        return copy(
            bank = newBank,
            players = players
                .fold(initial) { (list, cards), (player, status) ->
                    val (newStatus, newCards) = status.drawOne(cards)

                    (list + PlayerWithStatus(player, newStatus)) to newCards
                }.first
        )
    }

    private fun scores(): List<Pair<String, Int>> =
        players.asSequence()
            .filter { (_, status) -> status.move == Stay || status.move == InGame }
            .map { (player, status) -> player.id to status.hand.score }
            .toList()

    fun winners(): List<Player> {
        val bankScore = bank.hand.score
        val playerScores = scores()
        val playerBestScore = playerScores
            .filter { it.second <= 21 }
            .maxBy { it.second }?.second ?: -1

        return if (bankScore > 21 || playerBestScore >= bankScore) {
            playerScores.asSequence()
                .filter { it.second == playerBestScore }
                .map { PlayerService.increment(it.first, it.second) }
                .toList()
        } else emptyList()
    }

    fun updatePlayers(): Room =
        copy(players = players
            .filterNot { it.status.move == Timeout }
            .map { (player, _) ->
                PlayerWithStatus(PlayerService[player.id], PlayerStatus(move = InGame))
            })

    fun timeout(): Room =
        copy(players = players.map { (p, s) ->
            val newStatus = if (s.move == InGame) s.copy(move = Timeout) else s
            PlayerWithStatus(p, newStatus)
        })
}
