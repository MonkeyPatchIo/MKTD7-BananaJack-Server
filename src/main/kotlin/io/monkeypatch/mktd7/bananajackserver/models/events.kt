package io.monkeypatch.mktd7.bananajackserver.models


sealed class PlayerEvents
data class Login(val name: String) : PlayerEvents()
data class Logout(val playerId: String) : PlayerEvents()
data class Register(val playerId: String) : PlayerEvents()

interface RoomEvent

sealed class InputEvent : RoomEvent
data class PlayerJoin(val roomId: Int, val playerId: String) : InputEvent()
data class PlayerLeave(val roomId: Int, val playerId: String) : InputEvent()
data class PlayerAction(val roomId: Int, val playerId: String, val action: PlayerMove) : InputEvent()

sealed class OutputRoomEvent : RoomEvent {
    val type: String by lazy {
        when (this) {
            is TurnStarted        -> "turn-started"
            is TurnEnded          -> "turn-ended"
            is RoundEnded         -> "round-ended"
            is PlayerJoiningRoom  -> "player-joining"
            is PlayerLeavingRoom  -> "player-leaving"
            is PlayerActionInRoom -> "player-action"
        }
    }
}

data class TurnStarted(val round: Int, val step: Int, val room: Room) : OutputRoomEvent()
data class TurnEnded(val round: Int, val step: Int, val room: Room) : OutputRoomEvent()
data class RoundEnded(val round: Int, val room: Room, val winners: String) : OutputRoomEvent()
data class PlayerJoiningRoom(val roomId: Int, val player: Player) : OutputRoomEvent()
data class PlayerLeavingRoom(val roomId: Int, val player: Player) : OutputRoomEvent()
data class PlayerActionInRoom(val roomId: Int, val player: Player, val action: PlayerMove) : OutputRoomEvent()