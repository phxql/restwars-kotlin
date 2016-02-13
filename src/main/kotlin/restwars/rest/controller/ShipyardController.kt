package restwars.rest.controller

import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.business.ship.ShipService
import restwars.rest.api.ShipsInConstructionResponse
import spark.Route
import javax.validation.ValidatorFactory

class ShipyardController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val shipService: ShipService
) : ControllerHelper {
    fun listOnPlanet(): Route {
        return Route { req, res ->
            val context = RequestContext.build(req, playerService)
            val location = parseLocation(req)

            val planet = getOwnPlanet(planetService, context.player, location)
            val shipsInConstruction = shipService.findShipsInConstructionByPlanet(planet)

            return@Route Json.toJson(res, ShipsInConstructionResponse.fromShipsInConstruction(shipsInConstruction))
        }
    }
}