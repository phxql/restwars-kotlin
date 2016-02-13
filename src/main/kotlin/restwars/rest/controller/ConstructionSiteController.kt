package restwars.rest.controller

import restwars.business.building.BuildingService
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.ConstructionSitesResponse
import spark.Route
import javax.validation.ValidatorFactory

class ConstructionSiteController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val buildingService: BuildingService
) : ControllerHelper {
    fun listOnPlanet(): Route {
        return Route { req, res ->
            val context = RequestContext.build(req, playerService)
            val location = parseLocation(req)

            val planet = getOwnPlanet(planetService, context.player, location)
            val constructionSites = buildingService.findConstructionSitesByPlanet(planet)

            return@Route Json.toJson(res, ConstructionSitesResponse.fromConstructionSites(constructionSites))
        }
    }

}