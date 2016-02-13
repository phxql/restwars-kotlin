package restwars.rest.controller

import org.eclipse.jetty.http.HttpStatus
import restwars.business.building.BuildingService
import restwars.business.building.BuildingType
import restwars.business.planet.Location
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.*
import restwars.rest.http.StatusCode
import spark.Request
import spark.Route
import javax.validation.ValidatorFactory

class BuildingController(
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

            val buildings = buildingService.findBuildingsByPlanet(planet)
            return@Route Json.toJson(res, BuildingsResponse.fromBuildings(buildings))
        }
    }

    fun build(): Route {
        return Route { req, res ->
            val context = RequestContext.build(req, playerService)
            val request = validate(validation, Json.fromJson(req, BuildBuildingRequest::class.java))
            val type = BuildingType.parse(request.type)
            val location = parseLocation(req)

            val planet = planetService.findByLocation(location)
            if (planet == null || planet.owner != context.player.id) {
                res.status(StatusCode.NOT_FOUND)
                return@Route Json.toJson(res, ErrorResponse("No planet at $location found"))
            }

            val constructionSite = buildingService.build(planet, type)

            res.status(StatusCode.CREATED)
            return@Route Json.toJson(res, ConstructionSiteResponse.fromConstructionSite(constructionSite))
        }
    }
}