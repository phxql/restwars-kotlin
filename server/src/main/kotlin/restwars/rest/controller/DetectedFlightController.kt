package restwars.rest.controller

import restwars.business.flight.FlightService
import restwars.business.player.PlayerService
import restwars.rest.api.DetectedFlightsResponse
import restwars.rest.api.Result
import restwars.rest.api.fromFlights
import restwars.rest.base.ControllerHelper
import restwars.rest.base.Method
import restwars.rest.base.RequestContext
import spark.Request
import spark.Response

class DetectedFlightController(
        private val flightService: FlightService,
        private val playerService: PlayerService
) : ControllerHelper {
    fun byPlayer(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)
                val since = req.queryParams("since")?.toLong()

                val flights = flightService.findDetectedFlightsWithPlayer(context.player, since)
                return DetectedFlightsResponse.fromFlights(flights)
            }
        }
    }
}