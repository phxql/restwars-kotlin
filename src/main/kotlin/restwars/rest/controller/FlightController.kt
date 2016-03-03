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
import restwars.rest.api.CreateFlightRequest
import restwars.rest.api.ErrorResponse
import restwars.rest.api.FlightResponse
import restwars.rest.api.FlightsResponse
import restwars.rest.http.StatusCode
import spark.Route
import javax.validation.ValidatorFactory

class FlightController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val flightService: FlightService
) : ControllerHelper {
    fun create(): Route {
        return Route { req, res ->
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
                return@Route Json.toJson(res, ErrorResponse(ex.message ?: ""))
            } catch(ex: InvalidLocationException) {
                res.status(StatusCode.BAD_REQUEST)
                return@Route Json.toJson(res, ErrorResponse(ex.message ?: ""))
            } catch(ex: NotEnoughResourcesException) {
                res.status(StatusCode.BAD_REQUEST)
                return@Route Json.toJson(res, ErrorResponse(ex.message ?: ""))
            }

            return@Route Json.toJson(res, FlightResponse.fromFlight(sendResult.flight))
        }
    }

    fun listFrom(): Route {
        return Route { req, res ->
            val context = RequestContext.build(req, playerService)
            val location = parseLocation(req)

            val flights = flightService.findWithPlayerAndStart(context.player, location)
            return@Route Json.toJson(FlightsResponse.from(flights))
        }
    }

    fun listTo(): Route {
        return Route { req, res ->
            val context = RequestContext.build(req, playerService)
            val location = parseLocation(req)

            val flights = flightService.findWithPlayerAndDestination(context.player, location)
            return@Route Json.toJson(FlightsResponse.from(flights))
        }
    }

    fun list(): Route {
        return Route { req, res ->
            val context = RequestContext.build(req, playerService)

            val flights = flightService.findWithPlayer(context.player)
            return@Route Json.toJson(FlightsResponse.from(flights))
        }
    }
}


