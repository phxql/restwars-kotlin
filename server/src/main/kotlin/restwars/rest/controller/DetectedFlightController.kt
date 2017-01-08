package restwars.rest.controller

import restwars.business.flight.FlightService
import restwars.business.player.PlayerService
import restwars.rest.api.DetectedFlightsResponse
import restwars.rest.api.fromFlights
import restwars.rest.base.AuthenticatedRestMethod
import restwars.rest.base.ControllerHelper
import restwars.rest.base.HttpMethod
import restwars.rest.base.RestMethod

class DetectedFlightController(
        private val flightService: FlightService,
        private val playerService: PlayerService
) : ControllerHelper {
    fun byPlayer(): RestMethod<DetectedFlightsResponse> {
        return AuthenticatedRestMethod(HttpMethod.GET, "/v1/flight/detected", DetectedFlightsResponse::class.java, playerService, { req, res, context ->
            val since = req.queryParams("since")?.toLong()

            DetectedFlightsResponse.fromFlights(listOf())

            // TODO: Fix bug #51
//            val flights = flightService.findDetectedFlightsWithPlayer(context.player, since)
//            DetectedFlightsResponse.fromFlights(flights)
        })
    }
}