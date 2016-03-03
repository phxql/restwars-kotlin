package restwars.rest.controller

import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.business.resource.NotEnoughResourcesException
import restwars.business.ship.BuildShipException
import restwars.business.ship.ShipService
import restwars.business.ship.ShipType
import restwars.rest.api.BuildShipRequest
import restwars.rest.api.ErrorResponse
import restwars.rest.api.ShipInConstructionResponse
import restwars.rest.api.ShipsResponse
import restwars.rest.base.*
import restwars.rest.http.StatusCode
import spark.Request
import spark.Response
import javax.validation.ValidatorFactory

class ShipController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val shipService: ShipService
) : ControllerHelper {
    fun listOnPlanet(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)
                val location = parseLocation(req)

                val planet = getOwnPlanet(planetService, context.player, location)
                val ships = shipService.findShipsByPlanet(planet)

                return ShipsResponse.fromShips(ships)
            }
        }
    }

    fun build(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)
                val request = validate(validation, Json.fromJson(req, BuildShipRequest::class.java))
                val type = ShipType.parse(request.type)
                val location = parseLocation(req)

                val planet = getOwnPlanet(planetService, context.player, location)
                val buildResult = try {
                    shipService.buildShip(planet, type)
                } catch(ex: BuildShipException) {
                    res.status(StatusCode.BAD_REQUEST)
                    return ErrorResponse(ex.message ?: "")
                } catch(ex: NotEnoughResourcesException) {
                    res.status(StatusCode.BAD_REQUEST)
                    return ErrorResponse(ex.message ?: "")
                }

                res.status(StatusCode.CREATED)
                return ShipInConstructionResponse.fromShipInConstruction(buildResult.shipInConstruction)
            }
        }
    }

}