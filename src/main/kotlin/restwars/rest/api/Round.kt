package restwars.rest.api

import restwars.rest.base.Result

data class RoundResponse(val currentRound: Long, val roundTime: Int) : Result

data class RoundWebsocketResponse(val currentRound: Long) : Result