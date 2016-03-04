package restwars.rest.controller

import restwars.business.building.BuildingService
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.ConstructionSitesResponse
import restwars.rest.api.Result
import restwars.rest.api.fromConstructionSites
import restwars.rest.base.ControllerHelper
import restwars.rest.base.Method
import restwars.rest.base.RequestContext
import spark.Request
import spark.Response
import javax.validation.ValidatorFactory

class ConstructionSiteController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val buildingService: BuildingService
) : ControllerHelper {
    fun listOnPlanet(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)
                val location = parseLocation(req)

                val planet = getOwnPlanet(planetService, context.player, location)
                val constructionSites = buildingService.findConstructionSitesByPlanet(planet)

                return ConstructionSitesResponse.fromConstructionSites(constructionSites)
            }
        }
    }

}