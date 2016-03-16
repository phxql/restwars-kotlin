package restwars.business.event

import restwars.business.UUIDFactory
import restwars.business.clock.RoundService
import restwars.business.planet.Planet
import restwars.business.player.Player
import java.io.Serializable
import java.util.*

enum class EventType {
    BUILDING_COMPLETE, SHIP_COMPLETE
}

data class Event(
        val id: UUID,
        val type: EventType,
        val round: Long,
        val playerId: UUID,
        val planet: UUID
) : Serializable

interface EventService {
    fun createBuildingCompleteEvent(player: Player, planet: Planet): Event

    fun createShipCompleteEvent(player: Player, planet: Planet): Event
}

interface EventRepository {
    fun insert(event: Event)
}

class EventServiceImpl(
        private val uuidFactory: UUIDFactory,
        private val roundService: RoundService,
        private val eventRepository: EventRepository
) : EventService {
    override fun createBuildingCompleteEvent(player: Player, planet: Planet): Event {
        return createEvent(planet, player, EventType.BUILDING_COMPLETE)
    }

    override fun createShipCompleteEvent(player: Player, planet: Planet): Event {
        return createEvent(planet, player, EventType.SHIP_COMPLETE)
    }

    private fun createEvent(planet: Planet, player: Player, type: EventType): Event {
        val id = uuidFactory.create()
        val round = roundService.currentRound()

        val event = Event(id, type, round, player.id, planet.id)
        eventRepository.insert(event)
        return event
    }
}