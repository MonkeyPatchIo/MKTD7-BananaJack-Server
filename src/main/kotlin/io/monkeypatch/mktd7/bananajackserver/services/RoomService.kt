package io.monkeypatch.mktd7.bananajackserver.services

import io.monkeypatch.mktd7.bananajackserver.millis
import io.monkeypatch.mktd7.bananajackserver.models.*
import io.monkeypatch.mktd7.bananajackserver.peek
import io.monkeypatch.mktd7.bananajackserver.schedule
import io.monkeypatch.mktd7.bananajackserver.seconds
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture


class RoomService(var room: Room) {
    private val logger = LoggerFactory.getLogger(room.name)
    private val ec = Executors.newScheduledThreadPool(1)
    private var round = 0
    private var step = 0
    private var deckId: String? = null

    private val longTimeout = 30.seconds
    private val shortTimeout = 2.seconds


    fun join(player: Player): Room {
        logger.info("join $player")
        require(!room.full) { "Room ${room.name} is full !" }
        room = room.join(player)

        // Notify other members
        val event = PlayerJoiningRoom(room.id, player)
        PlayerService.dispatch(event) { (playerId, roomId) ->
            roomId == room.id && playerId != player.id
        }


        ec.schedule(shortTimeout) { this.beginTurn() } // TODO only if not started

        return room
    }

    fun action(player: Player, action: PlayerMove): Room {
        logger.info("$player do $action")
        room = room.action(player, action)

        // Notify other members
        val event = PlayerActionInRoom(room.id, player, action)
        PlayerService.dispatch(event) { (playerId, roomId) ->
            roomId == room.id && playerId != player.id
        }
        // FIXME fast nextSteo
        // room.players.none { it.status.move == InGame }

        return room
    }

    fun leave(player: Player): Room {
        logger.info("leave $player")
        room = room.leave(player)

        // Close session
        PlayerService.closeSession(player.id, room.id)

        // Notify other members
        PlayerService.dispatch(
            PlayerLeavingRoom(
                room.id,
                player
            )
        ) { (playerId, roomId) ->
            roomId == room.id && playerId != player.id
        }
        return room
    }


    private fun beginTurn() {
        room = room.cleanBeforeTurn()
        if (room.canBeginTurn()) {
            // Init
            round += 1
            step = 0
            val id = DeckOfCardsApi.api.newDeck().id
            deckId = id
            logger.info("BeginTurn #$round, deckId: $id")

            // Draw cards
            val cards = DeckOfCardsApi.api.draw(id, (room.players.size + 1) * 2)
                .cards
                .map { it.toCard() }
                .chunked(2)
            room = room.draw(cards)

            // Notify players
            PlayerService.dispatchRoom(
                room.id,
                TurnStarted(
                    round,
                    room
                )
            )

            // Schedule Next
            ec.schedule(longTimeout) { nextStep() }
        } else {
            logger.warn("Skip turn: no player")
        }
    }

    // FIXME optim: nextTimeout (use round/step to check if obsolete)
    // on action if nobbody in InGame => nextStep

    private fun nextStep() {
        step += 1
        logger.info("Turn #$round.$step")

        val listDrawing = room.listDrawing()
        val bankDraw = room.bankAction() == Draw
        val nbCards = listDrawing.size + (if (bankDraw) 1 else 0)
        var cards = DeckOfCardsApi.api.draw(deckId!!, nbCards)
            .cards
            .map { it.toCard() }

        val endTurn = cards.isEmpty()
        if (endTurn) {
            logger.info("Turn #$round.$step -> Finished")
            ec.schedule(shortTimeout) { endTurn() }
        } else {
            // Bank
            if (bankDraw) {
                val (card, tail) = cards.peek()
                cards = tail
                room = room.copy(bank = room.bank + card)
            }
            room = room.playerDraw(cards)

            // Notify
            PlayerService.dispatchRoom(
                room.id,
                TurnEnded(
                    round,
                    room
                )
            )

            // NextStep

            val timeout = if (room.players.none { it.status.move == InGame }) shortTimeout else longTimeout
            ec.schedule(timeout) { nextStep() } // FIXME nextTimeout
        }
    }

    private fun endTurn() {
        val bankScore = room.bank.score
        val playerScores = room.scores()
        val playerBestScore = playerScores
            .filter { it.second <= 21 }
            .maxBy { it.second }?.second ?: -1

        val winners = if (bankScore > 21 || playerBestScore >= bankScore) {
            val w = playerScores.asSequence()
                .filter { it.second == playerBestScore }
                .map { it.first }
                .map { PlayerService.increment(it) }
                .toList()
            room = room.updatePlayers()
            w
        } else emptyList()

        logger.info("End Turn #$round, winner: ${if (winners.isEmpty()) "Bank" else winners.joinToString(",")}")

        // Notify
        PlayerService.dispatchRoom(
            room.id,
            RoundEnded(
                round,
                room,
                winners
            )
        )

        // Next Turn
        ec.schedule(shortTimeout) { beginTurn() }
    }

    companion object {

        private val roomServices: Map<Int, RoomService> =
            IntRange(1, 16)
                .map { id ->
                    id to Room(
                        id,
                        "Room #${id.toString().padStart(
                            2,
                            '0'
                        )}"
                    )
                }
                .map { (index, room) -> index to RoomService(room) }
                .toMap()

        val rooms: List<Room>
            get() = roomServices.values
                .map { it.room }
                .sortedBy { it.name }

        private fun withRoomService(roomId: Int, block: RoomService.() -> Room): ScheduledFuture<Room> {
            val roomService = roomServices[roomId] ?: throw NoSuchElementException("Room '$roomId' not found")
            return roomService.ec.schedule(1.millis) {
                roomService.block()
            }
        }

        fun join(event: PlayerJoin): ScheduledFuture<Room> =
            withRoomService(event.roomId) { join(PlayerService[event.playerId]) }

        fun leave(event: PlayerLeave): ScheduledFuture<Room> =
            withRoomService(event.roomId) {
                leave(
                    PlayerService[event.playerId]
                )
            }

        fun action(event: PlayerAction): ScheduledFuture<Room> =
            withRoomService(event.roomId) {
                action(
                    PlayerService[event.playerId],
                    event.action
                )
            }
    }
}
