package io.monkeypatch.mktd7.bananajackserver.services

import io.javalin.websocket.WsSession
import io.monkeypatch.mktd7.bananajackserver.jsonMapper
import io.monkeypatch.mktd7.bananajackserver.models.OutputRoomEvent
import io.monkeypatch.mktd7.bananajackserver.models.Player
import io.monkeypatch.mktd7.bananajackserver.roomId
import org.eclipse.jetty.websocket.api.StatusCode
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import kotlin.NoSuchElementException


object PlayerService {
    private val ec = Executors.newWorkStealingPool()
    private val logger = LoggerFactory.getLogger("PlayerService")
    private val players = mutableMapOf<String, Player>()
    private val sessions = mutableMapOf<Pair<String, Int>, WsSession>()


    fun login(name: String): Player {
        logger.info("Login $name")
        if (players.values.any { it.name == name })
            throw IllegalArgumentException("Name '$name' already registered")
        val newPlayer = Player(UUID.randomUUID().toString(), name, 0)
        players[newPlayer.id] = newPlayer
        return newPlayer
    }

    fun logout(playerId: String): Player {
        logger.info("Logout $playerId")
        val player = players[playerId] ?: throw NoSuchElementException("Player ID '$playerId' not found")
        players.remove(playerId)
        val keys = sessions.keys.filter { it.first == playerId }
        keys.forEach { sessions.remove(it) }

        // Close session
        closeSession(playerId)

        return player
    }

    fun increment(playerId: String): Player {
        logger.info("Increment $playerId")
        val player = players[playerId] ?: throw NoSuchElementException("Player ID '$playerId' not found")
        val newPlayer = player.copy(score = player.score + 1)
        players[playerId] = newPlayer
        return newPlayer
    }

    fun register(playerId: String, session: WsSession) {
        logger.info("register $playerId, ${session.id}")
        if (sessions.keys.any { it.first == playerId })
            throw IllegalArgumentException("Player '$playerId' already connected")
        sessions[playerId to session.roomId] = session
    }

    operator fun get(playerId: String): Player =
        players[playerId] ?: throw NoSuchElementException("Player ID '$playerId' not found")

    private fun removeSession(key: Pair<String, Int>) =
        sessions.remove(key)
            ?.close(StatusCode.NORMAL, "Leaving Room")

    fun closeSession(playerId: String, index: Int) =
        removeSession(playerId to index)

    private fun closeSession(playerId: String) {
        val toClose = sessions.keys.filter { it.first == playerId }
        toClose.forEach { removeSession(it) }
    }

    fun dispatch(event: OutputRoomEvent, predicate: (Pair<String, Int>) -> Boolean) =
        sessions
            .filterValues { it.isOpen }
            .filterKeys(predicate)
            .forEach { (player, session) ->
                ec.submit {
                    logger.info("dispatch $event to $player")
                    val message = jsonMapper.writeValueAsString(event)
                    session.send(message)
                }
            }

    fun dispatchRoom(roomId: Int, event: OutputRoomEvent) =
        dispatch(event) { (_, id) -> roomId == id }

    fun findPlayerId(session: WsSession): String? =
        sessions.filterValues { it == session }
            .toList()
            .map { it.first.first }
            .firstOrNull()

    fun dashboard(): List<Player> =
        players.values
            .sortedByDescending { it.score }
}
