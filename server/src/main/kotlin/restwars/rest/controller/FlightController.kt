package restwars.rest.controller

import restwars.business.flight.FlightException
import restwars.business.flight.FlightService
import restwars.business.flight.FlightType
import restwars.business.planet.InvalidLocationException
import restwars.business.planet.Location
import restwars.business.planet.PlanetService
import restwars.business.planet.Resources
import restwars.business.player.PlayerService
import restwars.business.resource.NotEnoughResourcesException
import restwars.rest.api.*
import restwars.rest.base.ControllerHelper
import restwars.rest.base.Json
import restwars.rest.base.Method
import restwars.rest.base.RequestContext
import restwars.rest.http.StatusCode
import spark.Request
import spark.Response
import javax.validation.ValidatorFactory

class FlightController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val flightService: FlightService
) : ControllerHelper {
    fun create(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val request = validate(validation, Json.fromJson(req, CreateFlightRequest::class.java))
                val context = RequestContext.build(req, playerService)
                val location = parseLocation(req)
                val planet = getOwnPlanet(planetService, context.player, location)
                val destination = Location.parse(request.destination) // TODO: Exception handling
                val type = FlightType.parse(request.type) // TODO: Exception handling

                val cargo = request.cargo?.toResources() ?: Resources.none()
                val sendResult = try {
                    flightService.sendShipsToPlanet(context.player, planet, destination, request.ships.toShips(), type, cargo)
                } catch(ex: FlightException) {
                    res.status(StatusCode.BAD_REQUEST)
                    return ErrorResponse(ex.message ?: "")
                } catch(ex: InvalidLocationException) {
                    res.status(StatusCode.BAD_REQUEST)
                    return ErrorResponse(ex.message ?: "")
                } catch(ex: NotEnoughResourcesException) {
                    res.status(StatusCode.BAD_REQUEST)
                    return ErrorResponse(ex.message ?: "")
                }

                return FlightResponse.fromFlight(sendResult.flight)
            }
        }
    }

    fun listFrom(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)
                val location = parseLocation(req)

                val flights = flightService.findWithPlayerAndStart(context.player, location)
                return FlightsResponse.from(flights)
            }
        }
    }

    fun listTo(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)
                val location = parseLocation(req)

                val flights = flightService.findWithPlayerAndDestination(context.player, location)
                return FlightsResponse.from(flights)
            }
        }
    }

    fun list(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)

                val flights = flightService.findWithPlayer(context.player)
                return FlightsResponse.from(flights)
            }
        }
    }
}


