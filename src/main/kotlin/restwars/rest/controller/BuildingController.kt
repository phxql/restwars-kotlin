package restwars.rest.controller

import restwars.business.building.BuildBuildingException
import restwars.business.building.BuildResult
import restwars.business.building.BuildingService
import restwars.business.building.BuildingType
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.business.resource.NotEnoughResourcesException
import restwars.rest.api.BuildBuildingRequest
import restwars.rest.api.BuildingsResponse
import restwars.rest.api.ConstructionSiteResponse
import restwars.rest.api.ErrorResponse
import restwars.rest.base.*
import restwars.rest.http.StatusCode
import spark.Request
import spark.Response
import javax.validation.ValidatorFactory

class BuildingController(
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
                val buildings = buildingService.findBuildingsByPlanet(planet)

                return BuildingsResponse.fromBuildings(buildings)
            }
        }
    }

    fun build(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)
                val request = validate(validation, Json.fromJson(req, BuildBuildingRequest::class.java))
                val type = BuildingType.parse(request.type)
                val location = parseLocation(req)

                val planet = getOwnPlanet(planetService, context.player, location)
                val buildResult: BuildResult
                try {
                    buildResult = buildingService.build(planet, type)
                } catch(ex: BuildBuildingException) {
                    res.status(StatusCode.BAD_REQUEST)
                    return ErrorResponse(ex.message ?: "")
                } catch(ex: NotEnoughResourcesException) {
                    res.status(StatusCode.BAD_REQUEST)
                    return ErrorResponse(ex.message ?: "")
                }

                res.status(StatusCode.CREATED)
                return ConstructionSiteResponse.fromConstructionSite(buildResult.constructionSite)
            }
        }
    }
}