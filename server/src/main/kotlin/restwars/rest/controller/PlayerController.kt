package restwars.rest.controller

import restwars.business.building.BuildingService
import restwars.business.planet.PlanetService
import restwars.business.planet.UniverseFullException
import restwars.business.player.PlayerService
import restwars.business.player.UsernameNotUniqueException
import restwars.rest.api.*
import restwars.rest.base.*
import restwars.rest.http.StatusCode
import javax.validation.ValidatorFactory

class PlayerController(
        private val validation: ValidatorFactory,
        private val playerService: PlayerService,
        private val planetService: PlanetService,
        private val buildingService: BuildingService
) : ControllerHelper {
    fun create(): RestMethod<SuccessResponse> {
        return PayloadRestMethod(HttpMethod.POST, "/v1/player", SuccessResponse::class.java, CreatePlayerRequest::class.java, validation, { req, res, payload ->
            val player = try {
                playerService.create(payload.username, payload.password)
            } catch(ex: UsernameNotUniqueException) {
                throw StatusCodeException(StatusCode.CONFLICT, ErrorResponse(ErrorReason.USERNAME_ALREADY_EXISTS.name, ex.message ?: ""))
            }
            val planet = try {
                planetService.createStarterPlanet(player)
            } catch(ex: UniverseFullException) {
                throw StatusCodeException(StatusCode.CONFLICT, ErrorResponse(ErrorReason.UNIVERSE_FULL.name, ex.message ?: ""))
            }
            buildingService.createStarterBuildings(planet)

            res.status(StatusCode.CREATED)
            SuccessResponse("Player created")
        })
    }

    fun get(): RestMethod<PlayerResponse> {
        return AuthenticatedRestMethod(HttpMethod.GET, "/v1/player", PlayerResponse::class.java, playerService, { req, res, context ->
            PlayerResponse(context.player.username)
        })
    }
}