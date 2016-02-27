package restwars.rest.controller

import restwars.business.building.BuildingService
import restwars.business.building.BuildingType
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.ScanResponse
import spark.Route
import javax.validation.ValidatorFactory

class TelescopeController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val buildingService: BuildingService
) : ControllerHelper {
    fun scan(): Route {
        return Route { req, res ->
            val context = RequestContext.build(req, playerService)
            val location = parseLocation(req)

            val planet = getOwnPlanet(planetService, context.player, location)
            val telescopeLevel = buildingService.findBuildingByPlanetAndType(planet, BuildingType.TELESCOPE)?.level ?: 0
            // TODO: Prohibit scan if level is 0
            val planets = planetService.findInVicinity(planet, telescopeLevel)

            return@Route Json.toJson(res, ScanResponse.from(planets))
        }
    }
}