package restwars.rest.controller

import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.business.ship.ShipService
import restwars.business.ship.ShipType
import restwars.rest.api.BuildShipRequest
import restwars.rest.api.ErrorResponse
import restwars.rest.api.ShipInConstructionResponse
import restwars.rest.api.ShipsResponse
import restwars.rest.http.StatusCode
import spark.Route
import javax.validation.ValidatorFactory

class ShipController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val shipService: ShipService
) : ControllerHelper {
    fun listOnPlanet(): Route {
        return Route { req, res ->
            val context = RequestContext.build(req, playerService)
            val location = parseLocation(req)

            val planet = planetService.findByLocation(location)
            if (planet == null || planet.owner != context.player.id) {
                res.status(StatusCode.NOT_FOUND)
                return@Route Json.toJson(res, ErrorResponse("No planet at $location found"))
            }

            val ships = shipService.findShipsByPlanet(planet)
            return@Route Json.toJson(res, ShipsResponse.fromShips(ships))
        }
    }

    fun build(): Route {
        return Route { req, res ->
            val context = RequestContext.build(req, playerService)
            val request = validate(validation, Json.fromJson(req, BuildShipRequest::class.java))
            val type = ShipType.parse(request.type)
            val location = parseLocation(req)

            val planet = planetService.findByLocation(location)
            if (planet == null || planet.owner != context.player.id) {
                res.status(StatusCode.NOT_FOUND)
                return@Route Json.toJson(res, ErrorResponse("No planet at $location found"))
            }

            val shipInConstruction = shipService.buildShip(planet, type)

            res.status(StatusCode.CREATED)
            return@Route Json.toJson(res, ShipInConstructionResponse.fromShipInConstruction(shipInConstruction))
        }
    }

}