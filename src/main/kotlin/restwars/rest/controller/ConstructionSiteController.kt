package restwars.rest.controller

import restwars.business.building.BuildingService
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.BuildingsResponse
import restwars.rest.api.ConstructionSiteResponse
import restwars.rest.api.ConstructionSitesResponse
import restwars.rest.api.ErrorResponse
import restwars.rest.http.StatusCode
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

            val planet = planetService.findByLocation(location)
            if (planet == null || planet.owner != context.player.id) {
                res.status(StatusCode.NOT_FOUND)
                return@Route Json.toJson(res, ErrorResponse("No planet at $location found"))
            }

            val constructionSites = buildingService.findConstructionSitesByPlanet(planet)
            return@Route Json.toJson(res, ConstructionSitesResponse.fromConstructionSites(constructionSites))
        }
    }

}