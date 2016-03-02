package restwars.rest.controller

import restwars.business.building.BuildingService
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.business.player.UsernameNotUniqueException
import restwars.rest.api.CreatePlayerRequest
import restwars.rest.api.ErrorResponse
import restwars.rest.api.SuccessResponse
import restwars.rest.http.StatusCode
import spark.Route
import javax.validation.ValidatorFactory

class PlayerController(
        private val validation: ValidatorFactory,
        private val playerService: PlayerService,
        private val planetService: PlanetService,
        private val buildingService: BuildingService
) : ControllerHelper {
    fun create(): Route {
        return Route { req, res ->
            val request = validate(validation, Json.fromJson(req, CreatePlayerRequest::class.java))

            val player = try {
                playerService.create(request.username, request.password)
            } catch(ex: UsernameNotUniqueException) {
                res.status(StatusCode.CONFLICT)
                return@Route Json.toJson(res, ErrorResponse(ex.message ?: ""))
            }
            val planet = planetService.createStarterPlanet(player)
            buildingService.createStarterBuildings(planet)

            res.status(StatusCode.CREATED)
            return@Route Json.toJson(res, SuccessResponse("Player created"))
        }
    }
}