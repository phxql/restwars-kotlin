package restwars.rest.controller

import restwars.business.building.BuildingService
import restwars.business.planet.Location
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.BuildingsResponse
import restwars.rest.api.ErrorResponse
import restwars.rest.http.StatusCode
import spark.Route

class BuildingController(
        val playerService: PlayerService,
        val planetService: PlanetService,
        val buildingService: BuildingService
) : ControllerHelper {
    fun listOnPlanet(): Route {
        return Route { req, res ->
            val locationString = req.params(":location") ?: throw BadRequestException(ErrorResponse("Path variable location is missing"))
            val location = try {
                Location.parse(locationString)
            } catch(e: IllegalArgumentException) {
                throw BadRequestException(ErrorResponse(e.message ?: ""))
            }
            val context = RequestContext.build(req, playerService)

            val planet = planetService.findByLocation(location)
            if (planet == null || planet.owner != context.player.id) {
                res.status(StatusCode.NOT_FOUND)
                return@Route Json.toJson(res, ErrorResponse("No planet at $location found"))
            }

            val buildings = buildingService.findByPlanet(planet)
            return@Route Json.toJson(res, BuildingsResponse.fromBuildings(buildings))
        }
    }
}