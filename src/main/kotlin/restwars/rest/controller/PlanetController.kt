package restwars.rest.controller

import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.PlanetsResponse
import restwars.rest.base.ControllerHelper
import restwars.rest.base.Method
import restwars.rest.base.RequestContext
import restwars.rest.base.Result
import spark.Request
import spark.Response

class PlanetController(
        private val playerService: PlayerService,
        private val planetService: PlanetService
) : ControllerHelper {
    fun list(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)
                val planets = planetService.findByOwner(context.player)

                return PlanetsResponse.fromPlanets(planets)
            }
        }
    }
}