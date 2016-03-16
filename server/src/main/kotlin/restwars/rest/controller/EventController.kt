package restwars.rest.controller

import restwars.business.event.EventService
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.EventsResponse
import restwars.rest.api.Result
import restwars.rest.api.fromEvents
import restwars.rest.base.ControllerHelper
import restwars.rest.base.Method
import restwars.rest.base.RequestContext
import spark.Request
import spark.Response
import javax.validation.ValidatorFactory

class EventController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val eventService: EventService
) : ControllerHelper {
    fun byPlayer(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)
                val since = req.queryParams("since")?.toLong()

                val events = eventService.findWithPlayer(context.player, since)
                return EventsResponse.fromEvents(events)
            }
        }
    }
}