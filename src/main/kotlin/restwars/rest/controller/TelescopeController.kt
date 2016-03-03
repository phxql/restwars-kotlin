package restwars.rest.controller

import restwars.business.building.BuildingService
import restwars.business.building.BuildingType
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.ScanResponse
import restwars.rest.base.ControllerHelper
import restwars.rest.base.Method
import restwars.rest.base.RequestContext
import restwars.rest.base.Result
import spark.Request
import spark.Response
import javax.validation.ValidatorFactory

class TelescopeController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val buildingService: BuildingService
) : ControllerHelper {
    fun scan(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)
                val location = parseLocation(req)

                val planet = getOwnPlanet(planetService, context.player, location)
                val telescopeLevel = buildingService.findBuildingByPlanetAndType(planet, BuildingType.TELESCOPE)?.level ?: 0
                // TODO: Prohibit scan if level is 0
                val planets = planetService.findInVicinity(planet, telescopeLevel)

                return ScanResponse.from(planets)
            }
        }
    }
}