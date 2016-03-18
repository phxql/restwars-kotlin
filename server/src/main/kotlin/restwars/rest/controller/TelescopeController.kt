package restwars.rest.controller

import restwars.business.building.BuildingService
import restwars.business.building.BuildingType
import restwars.business.planet.NoTelescopeException
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.ErrorResponse
import restwars.rest.api.ScanResponse
import restwars.rest.api.from
import restwars.rest.base.*
import restwars.rest.http.StatusCode
import javax.validation.ValidatorFactory

class TelescopeController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val buildingService: BuildingService
) : ControllerHelper {
    fun scan(): RestMethod<ScanResponse> {
        return AuthenticatedRestMethod(HttpMethod.POST, "/v1/planet/:location/telescope/scan", ScanResponse::class.java, playerService, { req, res, context ->
            val location = parseLocation(req)

            val planet = getOwnPlanet(planetService, context.player, location)
            val telescopeLevel = buildingService.findBuildingByPlanetAndType(planet, BuildingType.TELESCOPE)?.level ?: 0
            try {
                val planets = planetService.findInVicinity(planet, telescopeLevel)
                ScanResponse.from(planets)
            } catch(ex: NoTelescopeException) {
                throw StatusCodeException(StatusCode.BAD_REQUEST, ErrorResponse(ex.message ?: ""))
            }
        })
    }
}