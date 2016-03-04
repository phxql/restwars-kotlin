package restwars.rest.controller

import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.business.ship.ShipService
import restwars.rest.api.Result
import restwars.rest.api.ShipsInConstructionResponse
import restwars.rest.api.fromShipsInConstruction
import restwars.rest.base.ControllerHelper
import restwars.rest.base.Method
import restwars.rest.base.RequestContext
import spark.Request
import spark.Response
import javax.validation.ValidatorFactory

class ShipyardController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val shipService: ShipService
) : ControllerHelper {
    fun listOnPlanet(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)
                val location = parseLocation(req)

                val planet = getOwnPlanet(planetService, context.player, location)
                val shipsInConstruction = shipService.findShipsInConstructionByPlanet(planet)

                return ShipsInConstructionResponse.fromShipsInConstruction(shipsInConstruction)
            }
        }
    }
}