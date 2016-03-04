package restwars.rest.api

data class RoundResponse(val currentRound: Long, val roundTime: Int) : Result

data class RoundWebsocketResponse(val currentRound: Long) : Result