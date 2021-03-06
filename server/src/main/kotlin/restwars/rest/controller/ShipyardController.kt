package restwars.rest.controller

import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.business.ship.ShipService
import restwars.rest.api.ShipsInConstructionResponse
import restwars.rest.api.fromShipsInConstruction
import restwars.rest.base.AuthenticatedRestReadMethod
import restwars.rest.base.ControllerHelper
import restwars.rest.base.HttpMethod
import restwars.rest.base.RestMethod
import javax.validation.ValidatorFactory

class ShipyardController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val shipService: ShipService
) : ControllerHelper {
    fun listOnPlanet(): RestMethod<ShipsInConstructionResponse> {
        return AuthenticatedRestReadMethod(HttpMethod.GET, "/v1/planet/:location/shipyard", ShipsInConstructionResponse::class.java, playerService) { req, _, context ->
            val location = parseLocation(req)

            val planet = getOwnPlanet(planetService, context.player, location)
            val shipsInConstruction = shipService.findShipsInConstructionByPlanet(planet)

            ShipsInConstructionResponse.fromShipsInConstruction(shipsInConstruction)
        }
    }
}