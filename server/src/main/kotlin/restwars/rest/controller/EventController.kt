package restwars.rest.controller

import restwars.business.event.EventService
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.EventsResponse
import restwars.rest.api.fromEvents
import restwars.rest.base.AuthenticatedRestMethod
import restwars.rest.base.ControllerHelper
import restwars.rest.base.HttpMethod
import restwars.rest.base.RestMethod
import javax.validation.ValidatorFactory

class EventController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val eventService: EventService
) : ControllerHelper {
    fun byPlayer(): RestMethod<EventsResponse> {
        return AuthenticatedRestMethod(HttpMethod.GET, "/v1/event", EventsResponse::class.java, playerService, { req, res, context ->
            val since = req.queryParams("since")?.toLong()

            val events = eventService.findWithPlayer(context.player, since)
            EventsResponse.fromEvents(events)
        })
    }
}