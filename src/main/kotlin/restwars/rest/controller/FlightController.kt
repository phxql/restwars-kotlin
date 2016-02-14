package restwars.rest.controller

import restwars.business.flight.FlightService
import restwars.business.planet.Location
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.CreateFlightRequest
import restwars.rest.api.FlightResponse
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
            val destination = Location.parse(request.destination)

            val flight = flightService.sendShipsToPlanet(context.player, planet, destination, request.ships.toShips())

            return@Route Json.toJson(res, FlightResponse.fromFlight(flight))
        }
    }
}


