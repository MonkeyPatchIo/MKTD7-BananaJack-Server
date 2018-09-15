package io.monkeypatch.mktd7.bananajackserver

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.monkeypatch.mktd7.bananajackserver.models.*

// Card
private val cardModule: Module = SimpleModule().apply {
    addSerializer { card: Card, gen ->
        gen.writeString(card.key)
    }
}

// PlayerMove
private val playerMoveModule: Module = SimpleModule().apply {
    addSerializer { value: PlayerMove, gen ->
        gen.writeString(value.name())
    }
    addDeserializer { jsonParser ->
        val node = jsonParser.codec.readTree<JsonNode>(jsonParser)
        PlayerMove.fromName(node.asText())
    }
}

// Events
private val eventModule: Module = SimpleModule().apply {
    addSerializer { event: OutputRoomEvent, gen ->
        gen.apply {
            writeStartObject()
            writeStringField("type", event.type)
            when (event) {
                is TurnStarted        -> {
                    writeNumberField("round", event.round)
                    writeObjectField("room", event.room)
                }
                is TurnEnded          -> {
                    writeNumberField("round", event.round)
                    writeObjectField("room", event.room)
                }
                is RoundEnded         -> {
                    writeNumberField("round", event.round)
                    writeObjectField("room", event.room)
                    writeObjectField("winners", event.winners)
                }
                is PlayerJoiningRoom  -> {
                    writeNumberField("roomId", event.roomId)
                    writeObjectField("player", event.player)
                }
                is PlayerLeavingRoom  -> {
                    writeNumberField("roomId", event.roomId)
                    writeObjectField("player", event.player)
                }
                is PlayerActionInRoom -> {
                    writeNumberField("roomId", event.roomId)
                    writeObjectField("player", event.player)
                    writeObjectField("move", event.action)
                }
            }
            writeEndObject()
        }
    }

    addDeserializer { jsonParser ->
        val node = jsonParser.codec.readTree<JsonNode>(jsonParser)
        val type = node.getString("type")
        val room = node.getInt("roomId")
        val playerId = node.getString("playerId")
        when (type) {
            "join"  -> PlayerJoin(room, playerId)
            "leave" -> PlayerLeave(room, playerId)
            "move"  -> PlayerAction(
                room,
                playerId,
                PlayerMove.fromName(
                    node.getString("move")
                )
            )
            else    ->
                throw IllegalArgumentException("Unexpected type $type for InputEvent")
        }
    }
}

val jsonMapper: ObjectMapper =
    ObjectMapper()
        .registerKotlinModule()
        .registerModules(
            playerMoveModule,
            cardModule,
            eventModule
        )
