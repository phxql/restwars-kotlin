package restwars.rest.api

import java.util.*

data class EventResponse(
        val id: UUID, val type: String, val round: Long, val location: LocationResponse
) : Result {
    companion object {}
}

data class EventsResponse(val events: List<EventResponse>) : Result {
    companion object {}
}