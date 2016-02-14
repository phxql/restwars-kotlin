package restwars.rest.controller

import restwars.business.clock.RoundService
import restwars.business.config.Config
import restwars.rest.api.RoundResponse
import spark.Route

class RoundController(
        val roundService: RoundService, val config: Config
) : ControllerHelper {
    fun get(): Route {
        return Route { request, response ->
            val currentRound = roundService.currentRound()
            return@Route Json.toJson(response, RoundResponse(currentRound, config.roundTime))
        }
    }
}