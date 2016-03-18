package restwars.rest.controller

import restwars.business.building.BuildingService
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.business.player.UsernameNotUniqueException
import restwars.rest.api.CreatePlayerRequest
import restwars.rest.api.ErrorResponse
import restwars.rest.api.PlayerResponse
import restwars.rest.api.SuccessResponse
import restwars.rest.base.*
import restwars.rest.http.StatusCode
import javax.validation.ValidatorFactory

class PlayerController(
        private val validation: ValidatorFactory,
        private val playerService: PlayerService,
        private val planetService: PlanetService,
        private val buildingService: BuildingService
) : ControllerHelper {
    fun create2(): RestMethod<SuccessResponse> {
        return DefaultRestMethod(SuccessResponse::class.java, HttpMethod.POST, "/v1/player", { req, res ->
            val request = validate(validation, Json.fromJson(req, CreatePlayerRequest::class.java))

            val player = try {
                playerService.create(request.username, request.password)
            } catch(ex: UsernameNotUniqueException) {
                throw StatusCodeException(StatusCode.CONFLICT, ErrorResponse(ex.message ?: ""))
            }
            val planet = planetService.createStarterPlanet(player)
            buildingService.createStarterBuildings(planet)

            res.status(StatusCode.CREATED)
            SuccessResponse("Player created")
        })
    }

    fun get2(): RestMethod<PlayerResponse> {
        return AuthenticatedRestMethod(PlayerResponse::class.java, HttpMethod.GET, "/v1/player", playerService, { req, res, context ->
            PlayerResponse(context.player.username)
        })
    }
}