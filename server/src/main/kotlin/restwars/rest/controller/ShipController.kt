package restwars.rest.controller

import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.business.resource.NotEnoughResourcesException
import restwars.business.ship.BuildShipException
import restwars.business.ship.ShipService
import restwars.business.ship.ShipType
import restwars.rest.api.*
import restwars.rest.base.*
import restwars.rest.http.StatusCode
import javax.validation.ValidatorFactory

class ShipController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val shipService: ShipService
) : ControllerHelper {
    fun listOnPlanet(): RestMethod<ShipsResponse> {
        return AuthenticatedRestReadMethod(HttpMethod.GET, "/v1/planet/:location/hangar", ShipsResponse::class.java, playerService) { req, _, context ->
            val location = parseLocation(req)

            val planet = getOwnPlanet(planetService, context.player, location)
            val ships = shipService.findShipsByPlanet(planet)

            ShipsResponse.fromShips(ships)
        }
    }

    fun build(): RestMethod<ShipInConstructionResponse> {
        return AuthenticatedPayloadRestWriteMethod(HttpMethod.POST, "/v1/planet/:location/hangar", ShipInConstructionResponse::class.java, BuildShipRequest::class.java, playerService, validation) { req, res, context, payload ->
            val type = ShipType.parse(payload.type)
            val location = parseLocation(req)

            val planet = getOwnPlanet(planetService, context.player, location)
            val buildResult = try {
                shipService.buildShip(planet, type)
            } catch(ex: BuildShipException) {
                throw StatusCodeException(StatusCode.BAD_REQUEST, ErrorResponse(ex.reason.name, ex.message ?: ""))
            } catch(ex: NotEnoughResourcesException) {
                throw StatusCodeException(StatusCode.BAD_REQUEST, ErrorResponse(ErrorReason.NOT_ENOUGH_RESOURCES.name, ex.message ?: ""))
            }

            res.status(StatusCode.CREATED)
            ShipInConstructionResponse.fromShipInConstruction(buildResult.shipInConstruction)
        }
    }

}