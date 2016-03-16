package restwars.rest.api

import restwars.business.event.EventWithPlanet

fun EventResponse.Companion.fromEvent(event: EventWithPlanet): EventResponse {
    return EventResponse(event.event.id, event.event.type.toString(), event.event.round, LocationResponse.fromLocation(event.planet.location))
}

fun EventsResponse.Companion.fromEvents(events: List<EventWithPlanet>): EventsResponse {
    return EventsResponse(events.map { EventResponse.fromEvent(it) })
}