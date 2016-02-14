package restwars.rest.api

data class RoundResponse(val currentRound: Long, val roundTime: Int)

data class RoundWebsocketResponse(val currentRound: Long)