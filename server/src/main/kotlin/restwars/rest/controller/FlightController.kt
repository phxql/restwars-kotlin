package restwars.rest.controller

import restwars.business.flight.FlightException
import restwars.business.flight.FlightService
import restwars.business.flight.FlightType
import restwars.business.planet.InvalidLocationException
import restwars.business.planet.PlanetService
import restwars.business.planet.Resources
import restwars.business.player.PlayerService
import restwars.business.resource.NotEnoughResourcesException
import restwars.rest.api.*
import restwars.rest.base.*
import restwars.rest.http.StatusCode
import javax.validation.ValidatorFactory

class FlightController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val flightService: FlightService
) : ControllerHelper {
    private fun parseFlightType(input: String): FlightType {
        try {
            return FlightType.parse(input)
        } catch (e: IllegalArgumentException) {
            throw BadRequestException(ErrorResponse(ErrorReason.FLIGHT_TYPE_PARSING_FAILED.name, "Unparsable flight type: $input"))
        }
    }

    fun create(): RestMethod<FlightResponse> {
        return AuthenticatedPayloadRestWriteMethod(HttpMethod.POST, "/v1/planet/:location/flight", FlightResponse::class.java, CreateFlightRequest::class.java, playerService, validation, { req, res, context, payload ->
            val location = parseLocation(req)
            val planet = getOwnPlanet(planetService, context.player, location)
            val destination = parseLocation(payload.destination)
            val type = parseFlightType(payload.type)

            val cargo = payload.cargo?.toResources() ?: Resources.none()
            val sendResult = try {
                flightService.sendShipsToPlanet(context.player, planet, destination, payload.ships.toShips(), type, cargo)
            } catch(ex: FlightException) {
                throw StatusCodeException(StatusCode.BAD_REQUEST, ErrorResponse(ex.reason.name, ex.message ?: ""))
            } catch(ex: InvalidLocationException) {
                throw StatusCodeException(StatusCode.BAD_REQUEST, ErrorResponse(ErrorReason.INVALID_LOCATION.name, ex.message ?: ""))
            } catch(ex: NotEnoughResourcesException) {
                throw StatusCodeException(StatusCode.BAD_REQUEST, ErrorResponse(ErrorReason.NOT_ENOUGH_RESOURCES.name, ex.message ?: ""))
            }

            FlightResponse.fromFlight(sendResult.flight)
        })
    }

    fun listFrom(): RestMethod<FlightsResponse> {
        return AuthenticatedRestReadMethod(HttpMethod.GET, "/v1/flight/from/:location", FlightsResponse::class.java, playerService, { req, res, context ->
            val location = parseLocation(req)

            val flights = flightService.findWithPlayerAndStart(context.player, location)
            FlightsResponse.from(flights)
        })
    }

    fun listTo(): RestMethod<FlightsResponse> {
        return AuthenticatedRestReadMethod(HttpMethod.GET, "/v1/flight/to/:location", FlightsResponse::class.java, playerService, { req, res, context ->
            val location = parseLocation(req)

            val flights = flightService.findWithPlayerAndDestination(context.player, location)
            FlightsResponse.from(flights)
        })
    }

    fun list(): RestMethod<FlightsResponse> {
        return AuthenticatedRestReadMethod(HttpMethod.GET, "/v1/flight", FlightsResponse::class.java, playerService, { req, res, context ->
            val flights = flightService.findWithPlayer(context.player)
            FlightsResponse.from(flights)
        })
    }
}


