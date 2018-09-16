package io.monkeypatch.mktd7.bananajackserver.services

import io.javalin.websocket.WsSession
import io.monkeypatch.mktd7.bananajackserver.jsonMapper
import io.monkeypatch.mktd7.bananajackserver.models.OutputRoomEvent
import io.monkeypatch.mktd7.bananajackserver.models.Player
import io.monkeypatch.mktd7.bananajackserver.roomId
import io.monkeypatch.mktd7.bananajackserver.sha1
import org.eclipse.jetty.websocket.api.StatusCode
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors


object PlayerService {
    private val ec = Executors.newWorkStealingPool()
    private val logger = LoggerFactory.getLogger("PlayerService")
    private val players = mutableMapOf<String, Player>()
    private val playersIp = mutableMapOf<String, String>()
    private val sessions = mutableMapOf<Pair<String, Int>, WsSession>()

    fun login(name: String, ip: String): Player {
        logger.info("Login [$ip] $name")
        val maybeExists = players.values.find { it.name == name }

        return if (maybeExists != null) {
            val oldIp = playersIp[maybeExists.id]
            if (oldIp != ip) throw IllegalArgumentException("Name '$name' already registered")
            closeSession(maybeExists.id)
            logger.debug("Player already exist: $maybeExists")
            maybeExists
        } else {
            val newPlayer = Player("NaCl-$name-$ip".sha1, name, 0)
            logger.debug("Create a new player: $newPlayer")
            players[newPlayer.id] = newPlayer
            playersIp[newPlayer.id] = ip
            newPlayer
        }
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

    fun increment(playerId: String, score: Int): Player {
        logger.info("Increment $playerId")
        val player = players[playerId] ?: throw NoSuchElementException("Player ID '$playerId' not found")
        val newPlayer = player.copy(score = player.score + (if (score == 21) 2 else 1))
        players[playerId] = newPlayer
        // TODO trigger an event ?
        return newPlayer
    }

    fun register(playerId: String, session: WsSession) {
        logger.info("register $playerId, ${session.id}")
        if (sessions.keys.any { it.first == playerId })
            throw IllegalArgumentException("Player '$playerId' already connected")
        sessions[playerId to session.roomId] = session
    }

    fun findPlayer(playerId: String): Player? =
        players[playerId]

    operator fun get(playerId: String): Player =
        players[playerId] ?: throw NoSuchElementException("Player ID '$playerId' not found")

    private fun removeSession(key: Pair<String, Int>): Boolean =
        sessions.remove(key)
            ?.let {
                it.close(StatusCode.NORMAL, "Leaving Room")
                true
            } ?: false

    fun closeSession(playerId: String, index: Int): Boolean =
        removeSession(playerId to index)

    private fun closeSession(playerId: String): Boolean =
        sessions.keys // <!> could not be lazy (Damn side effects)
            .filter { it.first == playerId }
            .map { removeSession(it) }
            .any { it }

    private fun dispatch(event: OutputRoomEvent, predicate: (Pair<String, Int>) -> Boolean) =
        sessions
            .filterValues { it.isOpen }
            .filterKeys(predicate)
            .forEach { (player, session) ->
                // <!> should be async, we don't care the result
                ec.submit {
                    logger.info("dispatch $event to $player")
                    val message = jsonMapper.writeValueAsString(event)
                    session.send(message)
                }
            }

    fun dispatchRoom(roomId: Int, event: OutputRoomEvent) =
        dispatch(event) { (_, id) -> roomId == id }

    fun dispatchRoomAndNotPlayer(roomId: Int, playerId: String, event: OutputRoomEvent) =
        dispatch(event) { (p, id) ->
            roomId == id && p != playerId
        }

    fun findPlayerId(session: WsSession): String? =
        sessions.filterValues { it == session }
            .toList()
            .map { it.first.first }
            .firstOrNull()

    fun dashboard(): List<Player> =
        players.values
            .sortedByDescending { it.score }
}
