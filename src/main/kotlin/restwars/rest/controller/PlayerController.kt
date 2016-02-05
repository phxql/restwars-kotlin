package restwars.rest.controller

import restwars.business.player.PlayerService
import restwars.rest.api.CreatePlayerRequest
import restwars.rest.api.SuccessResponse
import restwars.rest.http.StatusCode
import spark.Route
import javax.validation.ValidatorFactory

class PlayerController(val validation: ValidatorFactory, val playerService: PlayerService) : ControllerHelper {
    fun create(): Route {
        return Route { req, res ->
            val request = validate(validation, Json.fromJson(req, CreatePlayerRequest::class.java))

            playerService.create(request.username, request.password)

            res.status(StatusCode.created)
            Json.toJson(res, SuccessResponse("Player created"))
        }
    }
}