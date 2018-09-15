package io.monkeypatch.mktd7.bananajackserver

import com.fasterxml.jackson.module.kotlin.readValue
import io.javalin.apibuilder.ApiBuilder
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.websocket.WsHandler
import io.javalin.websocket.WsSession
import io.monkeypatch.mktd7.bananajackserver.models.*
import io.monkeypatch.mktd7.bananajackserver.services.PlayerService
import io.monkeypatch.mktd7.bananajackserver.services.RoomService
import org.slf4j.LoggerFactory


val apiRest: () -> Unit = {
    ApiBuilder.path("api") {

        path("auth") {
            post("login") {
                val body = it.body<Login>()
                it.json(PlayerService.login(body.name))
            }
            post("logout") {
                val body = it.body<Logout>()
                it.json(PlayerService.logout(body.playerId))
            }
        }

        path("room") {
            get { it.json(RoomService.rooms) }
            post("join") {
                val event = it.body<PlayerJoin>()
                val room = RoomService.join(event).get()
                it.json(room)
            }
            post("leave") {
                val event = it.body<PlayerLeave>()
                val room = RoomService.leave(event).get()
                it.json(room)
            }
            post("move") {
                val event = it.body<PlayerAction>()
                val room = RoomService.action(event).get()
                it.json(room)
            }
        }

        get("dashboards") {
            it.json(PlayerService.dashboard())
        }
    }
}


val WsSession.roomId: Int
    get() = this.pathParam("room").toInt()

val apiWs: (WsHandler) -> Unit = { ws ->
    val logger = LoggerFactory.getLogger("WS")

    ws.onConnect { logger.info("Connected on ${it.roomId} - ${it.id}") }

    ws.onMessage { session, message ->
        logger.debug("Message for ${session.roomId} - ${session.id}: $message")
        val event = jsonMapper.readValue<Register>(message)
        PlayerService.register(event.playerId, session)
    }

    ws.onClose { session, statusCode, reason ->
        logger.debug("Close [$statusCode] $reason for ${session.roomId} - ${session.id}")

        // Close
        session?.also {
            PlayerService.findPlayerId(it)
                ?.also { playerId ->
                    logger.warn("Player leaving: $playerId, should clean rooms")
                }
        }
    }

    ws.onError { session, throwable ->
        logger.error("Error for ${session.roomId} - ${session.id}", throwable)
    }
}