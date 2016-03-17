package restwars.rest.controller

import restwars.business.building.BuildingService
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.business.player.UsernameNotUniqueException
import restwars.rest.api.*
import restwars.rest.base.*
import restwars.rest.http.StatusCode
import spark.Request
import spark.Response
import javax.validation.ValidatorFactory

class PlayerController(
        private val validation: ValidatorFactory,
        private val playerService: PlayerService,
        private val planetService: PlanetService,
        private val buildingService: BuildingService
) : ControllerHelper {
    fun create2(): RestMethod<SuccessResponse> {
        return RestMethodImpl(SuccessResponse::class.java, HttpMethod.POST, "/v1/player", { req, res ->
            SuccessResponse("Player created")
        })
    }

    fun get2(): RestMethod<PlayerResponse> {
        return AuthenticatedRestMethodImpl(PlayerResponse::class.java, HttpMethod.GET, "/v1/player", playerService, { req, res, context ->
            PlayerResponse(context.player.username)
        })
    }

    fun create(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val request = validate(validation, Json.fromJson(req, CreatePlayerRequest::class.java))

                val player = try {
                    playerService.create(request.username, request.password)
                } catch(ex: UsernameNotUniqueException) {
                    res.status(StatusCode.CONFLICT)
                    return ErrorResponse(ex.message ?: "")
                }
                val planet = planetService.createStarterPlanet(player)
                buildingService.createStarterBuildings(planet)

                res.status(StatusCode.CREATED)
                return SuccessResponse("Player created")
            }
        }
    }

    fun get(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)
                return PlayerResponse(context.player.username)
            }
        }
    }
}