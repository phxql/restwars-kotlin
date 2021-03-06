package restwars.rest.controller

import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.PlanetsResponse
import restwars.rest.api.fromPlanets
import restwars.rest.base.AuthenticatedRestReadMethod
import restwars.rest.base.ControllerHelper
import restwars.rest.base.HttpMethod
import restwars.rest.base.RestMethod

class PlanetController(
        private val playerService: PlayerService,
        private val planetService: PlanetService
) : ControllerHelper {
    fun list(): RestMethod<PlanetsResponse> {
        return AuthenticatedRestReadMethod(HttpMethod.GET, "/v1/planet", PlanetsResponse::class.java, playerService) { _, _, context ->
            val planets = planetService.findByOwner(context.player)

            PlanetsResponse.fromPlanets(planets)
        }
    }
}