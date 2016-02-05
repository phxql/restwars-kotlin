package restwars.rest.controller

import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.CreatePlayerRequest
import restwars.rest.api.SuccessResponse
import restwars.rest.http.StatusCode
import spark.Route
import javax.validation.ValidatorFactory

class PlayerController(
        private val validation: ValidatorFactory,
        private val playerService: PlayerService,
        private val planetService: PlanetService
) : ControllerHelper {
    fun create(): Route {
        return Route { req, res ->
            val request = validate(validation, Json.fromJson(req, CreatePlayerRequest::class.java))

            val player = playerService.create(request.username, request.password)
            planetService.createStarterPlanet(player)

            res.status(StatusCode.created)
            Json.toJson(res, SuccessResponse("Player created"))
        }
    }
}