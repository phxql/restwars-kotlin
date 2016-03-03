package restwars.rest.api

import restwars.rest.base.Result
import java.util.*

data class FightResponse(
        val id: UUID,
        val attacker: String,
        val defender: String,
        val location: LocationResponse,
        val attackerShips: ShipsResponse,
        val defenderShips: ShipsResponse,
        val remainingAttackerShips: ShipsResponse,
        val remainingDefenderShips: ShipsResponse
) : Result

data class FightsResponse(val fights: List<FightResponse>) : Result