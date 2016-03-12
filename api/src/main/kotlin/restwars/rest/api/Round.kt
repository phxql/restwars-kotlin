package restwars.rest.api

data class RoundWithRoundTimeResponse(val currentRound: Long, val roundTime: Int) : Result

data class RoundResponse(val currentRound: Long) : Result