package restwars.rest.controller

import restwars.business.building.BuildBuildingException
import restwars.business.building.BuildResult
import restwars.business.building.BuildingService
import restwars.business.building.BuildingType
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.business.resource.NotEnoughResourcesException
import restwars.rest.api.*
import restwars.rest.base.*
import restwars.rest.http.StatusCode
import javax.validation.ValidatorFactory

class BuildingController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val buildingService: BuildingService
) : ControllerHelper {
    fun listOnPlanet(): RestMethod<BuildingsResponse> {
        return AuthenticatedRestReadMethod(HttpMethod.GET, "/v1/planet/:location/building", BuildingsResponse::class.java, playerService, { req, res, context ->
            val location = parseLocation(req)

            val planet = getOwnPlanet(planetService, context.player, location)
            val buildings = buildingService.findBuildingsByPlanet(planet)

            BuildingsResponse.fromBuildings(buildings)
        })
    }

    fun build(): RestMethod<ConstructionSiteResponse> {
        return AuthenticatedPayloadRestWriteMethod(HttpMethod.POST, "/v1/planet/:location/building", ConstructionSiteResponse::class.java, BuildBuildingRequest::class.java, playerService, validation, { req, res, context, payload ->
            val type = BuildingType.parse(payload.type)
            val location = parseLocation(req)

            val planet = getOwnPlanet(planetService, context.player, location)
            val buildResult: BuildResult
            try {
                buildResult = buildingService.build(planet, type)
            } catch(ex: BuildBuildingException) {
                throw StatusCodeException(StatusCode.BAD_REQUEST, ErrorResponse(ex.reason.name, ex.message ?: ""))
            } catch(ex: NotEnoughResourcesException) {
                throw StatusCodeException(StatusCode.BAD_REQUEST, ErrorResponse(ErrorReason.NOT_ENOUGH_RESOURCES.name, ex.message ?: ""))
            }

            res.status(StatusCode.CREATED)
            ConstructionSiteResponse.fromConstructionSite(buildResult.constructionSite)
        })
    }
}