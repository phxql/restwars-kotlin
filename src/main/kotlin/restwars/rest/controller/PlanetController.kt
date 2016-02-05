package restwars.rest.controller

import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.PlanetsResponse
import spark.Route

class PlanetController(
        private val playerService: PlayerService,
        private val planetService: PlanetService
) : ControllerHelper {
    fun list(): Route {
        return Route { request, response ->
            val context = RequestContext.build(request, playerService)
            val planets = planetService.findByOwner(context.player)

            Json.toJson(response, PlanetsResponse.fromPlanets(planets))
        }
    }
}