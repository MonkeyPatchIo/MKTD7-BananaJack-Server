package io.monkeypatch.mktd7.bananajackserver.services

import io.monkeypatch.mktd7.bananajackserver.jsonMapper
import io.monkeypatch.mktd7.bananajackserver.models.*
import io.monkeypatch.mktd7.bananajackserver.seconds
import io.monkeypatch.mktd7.bananajackserver.services.EventLoop.immediate
import io.monkeypatch.mktd7.bananajackserver.services.EventLoop.longTimeout
import io.monkeypatch.mktd7.bananajackserver.services.EventLoop.schedule
import io.monkeypatch.mktd7.bananajackserver.services.EventLoop.shortTimeout
import io.monkeypatch.mktd7.bananajackserver.services.PlayerService.dispatchRoomAndNotPlayer
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture


class RoomService(var room: Room) {

    private val logger = LoggerFactory.getLogger("room.${room.id}")
    private var state = RoomState()

    fun join(player: Player): Room {
        logger.info("join $player")
        require(!room.full) { "Room ${room.name} is full !" }
        room = room.join(player)

        // Notify other members
        val event = PlayerJoiningRoom(room.id, player)
        dispatchRoomAndNotPlayer(room.id, player.id, event)

        if (!state.started)
            schedule(shortTimeout) { this.beginTurn() }

        return room
    }

    fun action(player: Player, action: PlayerMove): Room {
        logger.info("$player do $action")
        room = room.action(player, action)

        // Notify other members
        val event = PlayerActionInRoom(room.id, player, action)
        dispatchRoomAndNotPlayer(room.id, player.id, event)

        // Maybe everybody played
        this.waitingPlayer(state.round, state.step)

        return room
    }

    fun leave(player: Player): Room {
        logger.info("leave $player")
        room = room.leave(player)

        // Close session
        PlayerService.closeSession(player.id, room.id)

        // Notify other members
        val event = PlayerLeavingRoom(room.id, player)
        dispatchRoomAndNotPlayer(room.id, player.id, event)

        // Maybe everybody played
        this.waitingPlayer(state.round, state.step)

        return room
    }

    private fun beginTurn() {
        room = room.cleanBeforeTurn()
        if (!room.canBeginTurn()) {
            logger.warn("Skip turn: no player")
            return
        }

        val id = DeckOfCardsApi.api.newDeck().id
        state = state.start(id)
        val (_, currentRound, currentStep) = state

        logger.info("BeginTurn #${state.round}, deckId: $id")

        // Draw cards
        val hands = drawHands(id, room.players.size + 1)
        room = room.drawHands(hands)

        // Notify players
        PlayerService.dispatchRoom(room.id, TurnStarted(currentRound, currentStep, room))

        // Scheduling
        schedule(shortTimeout) { waitingPlayer(currentRound, currentStep) }
        schedule(longTimeout) { timeoutPlayers(currentRound, currentStep) }
    }

    private fun timeoutPlayers(roundExpected: Int, stepExpected: Int) {
        if (state.round != roundExpected || stepExpected != state.step) {
            logger.warn("Skipped timeoutPlayers for #$roundExpected.$stepExpected")
            return
        }

        logger.info("Timeout #$roundExpected.$stepExpected")
        room = room.timeout()
        immediate { endTurn(roundExpected, stepExpected) }
    }

    private fun waitingPlayer(roundExpected: Int, stepExpected: Int) {
        if (state.round != roundExpected || stepExpected != state.step) {
            logger.warn("Skipped waitingPlayer for #$roundExpected.$stepExpected")
            return
        }

        logger.debug("waitingPlayer #$roundExpected.$stepExpected")
        val waitingForPlayer = room.players.none { it.status.move == InGame }
        if (waitingForPlayer)
            schedule(shortTimeout) { nextStep(roundExpected, stepExpected) }
    }

    private fun nextStep(roundExpected: Int, stepExpected: Int) {
        if (state.round != roundExpected || stepExpected != state.step) {
            logger.warn("Skipped nextStep for #$roundExpected.$stepExpected")
            return
        }

        val listDrawing = room.listDrawing()
        val bankDraw = room.bankAction() == Draw
        val nbCards = listDrawing.size + (if (bankDraw) 1 else 0)
        val cards = drawCards(state.deckId!!, nbCards)

        val endTurn = cards.isEmpty()
        if (endTurn) {
            logger.info("Turn #$roundExpected.$stepExpected -> Finished")
            schedule(shortTimeout) { endTurn(roundExpected, stepExpected) }
        } else {
            state = state.step()
            val (_, newRound, newStep) = state

            logger.info("Turn #$newRound.$newStep")
            room = room.playerDraw(cards, bankDraw)

            // Notify
            PlayerService.dispatchRoom(room.id, TurnEnded(newRound, newStep, room))

            // NextStep
            schedule(shortTimeout) { waitingPlayer(newRound, newStep) }
            schedule(longTimeout) { timeoutPlayers(newRound, newStep) }
        }
    }

    private fun endTurn(roundExpected: Int, stepExpected: Int) {
        if (state.round != roundExpected || stepExpected != state.step) {
            logger.warn("Skipped endTurn for #$roundExpected.$stepExpected")
            return
        }

        state = state.stop()
        val winners = room.winners()
        room = room.updatePlayers()

        val sWinner = if (winners.isEmpty()) "Bank" else winners.joinToString(", ") { it.name }
        logger.info("End Turn #$roundExpected, winners: $sWinner")

        // Notify
        PlayerService.dispatchRoom(room.id, RoundEnded(roundExpected, room, sWinner))

        // Next Turn
        schedule(5.seconds) { beginTurn() }
    }

    companion object {

        private val roomServices: Map<Int, RoomService> =
            IntRange(1, 16)
                .map { id -> id to Room(id, "Room #${id.toString().padStart(2, '0')}") }
                .map { (index, room) -> index to RoomService(room) }
                .toMap()

        val rooms: List<Room>
            get() = roomServices.values
                .map { it.room }
                .sortedBy { it.name }

        private fun withRoomService(roomId: Int, block: RoomService.() -> Room): CompletableFuture<String> {
            val roomService = roomServices[roomId] ?: throw NoSuchElementException("Room '$roomId' not found")
            return immediate { roomService.block() }
                .thenApply { jsonMapper.writeValueAsString(it) }
        }

        fun join(event: PlayerJoin) =
            withRoomService(event.roomId) {
                join(PlayerService[event.playerId])
            }

        fun leave(event: PlayerLeave) =
            withRoomService(event.roomId) {
                leave(PlayerService[event.playerId])
            }

        fun action(event: PlayerAction) =
            withRoomService(event.roomId) {
                action(PlayerService[event.playerId], event.action)
            }


        private fun drawHands(deckId: String, nbHands: Int): List<Hand> =
            DeckOfCardsApi.api.draw(deckId, nbHands * 2)
                .cards.asSequence()
                .map { it.toCard() }
                .chunked(2)
                .map { Hand(it) }
                .toList()

        private fun drawCards(deckId: String, nbCards: Int): List<Card> =
            DeckOfCardsApi.api.draw(deckId, nbCards)
                .cards.asSequence()
                .map { it.toCard() }
                .toList()

        fun leaveAll(playerId: String) =
            PlayerService.findPlayer(playerId)
                ?.also {
                    roomServices.values
                        .forEach { roomService ->
                            immediate { roomService.leave(it) }
                        }
                }

        private data class RoomState(
            val started: Boolean = false,
            val round: Int = 0,
            val step: Int = 0,
            val deckId: String? = null
        ) {
            fun start(deckId: String) =
                copy(
                    started = true,
                    round = round + 1,
                    step = 0,
                    deckId = deckId
                )

            fun step() =
                copy(step = step + 1)

            fun stop() =
                copy(started = false)
        }
    }
}
