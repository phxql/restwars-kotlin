package restwars.business.event

import restwars.business.UUIDFactory
import restwars.business.clock.RoundService
import restwars.business.planet.Planet
import restwars.business.player.Player
import java.io.Serializable
import java.util.*

enum class EventType {
    BUILDING_COMPLETE, SHIP_COMPLETE, FLIGHT_DETECTED, PLANET_COLONIZED, FIGHT_HAPPENED, SHIPS_TRANSFERRED,
    RESOURCES_TRANSFERRED, SHIPS_RETURNED, COLONIZE_FAILED
}

data class Event(
        val id: UUID,
        val type: EventType,
        val round: Long,
        val playerId: UUID,
        val planetId: UUID
) : Serializable

data class EventWithPlanet(val event: Event, val planet: Planet)

interface EventService {
    fun createBuildingCompleteEvent(playerId: UUID, planetId: UUID): Event

    fun createShipCompleteEvent(playerId: UUID, planetId: UUID): Event

    fun createFlightDetectedEvent(playerId: UUID, planetId: UUID): Event

    fun createPlanetColonizedEvent(playerId: UUID, planetId: UUID): Event

    fun createFightHappenedEvent(playerId: UUID, planetId: UUID): Event

    fun createShipsTransferredEvent(playerId: UUID, planetId: UUID): Event

    fun createResourcesTransferredEvent(playerId: UUID, planetId: UUID): Event

    fun createShipsReturnedEvent(playerId: UUID, planetId: UUID): Event

    fun createColonizedFailedEvent(playerId: UUID, planetId: UUID): Event

    fun findWithPlayer(player: Player, since: Long?): List<EventWithPlanet>
}

interface EventRepository {
    fun insert(event: Event)

    fun findWithPlayer(playerId: UUID): List<EventWithPlanet>

    fun findWithPlayerSince(playerId: UUID, since: Long): List<EventWithPlanet>
}

class EventServiceImpl(
        private val uuidFactory: UUIDFactory,
        private val roundService: RoundService,
        private val eventRepository: EventRepository
) : EventService {
    override fun findWithPlayer(player: Player, since: Long?): List<EventWithPlanet> {
        return if (since == null) {
            eventRepository.findWithPlayer(player.id)
        } else {
            val round = if (since <= 0) roundService.currentRound() + since else since
            eventRepository.findWithPlayerSince(player.id, round)
        }

    }

    override fun createBuildingCompleteEvent(playerId: UUID, planetId: UUID): Event {
        return createAndInsertEvent(planetId, playerId, EventType.BUILDING_COMPLETE)
    }

    override fun createShipCompleteEvent(playerId: UUID, planetId: UUID): Event {
        return createAndInsertEvent(planetId, playerId, EventType.SHIP_COMPLETE)
    }

    override fun createFlightDetectedEvent(playerId: UUID, planetId: UUID): Event {
        return createAndInsertEvent(planetId, playerId, EventType.FLIGHT_DETECTED)
    }

    override fun createPlanetColonizedEvent(playerId: UUID, planetId: UUID): Event {
        return createAndInsertEvent(planetId, playerId, EventType.PLANET_COLONIZED)
    }

    override fun createFightHappenedEvent(playerId: UUID, planetId: UUID): Event {
        return createAndInsertEvent(planetId, playerId, EventType.FIGHT_HAPPENED)
    }

    override fun createShipsTransferredEvent(playerId: UUID, planetId: UUID): Event {
        return createAndInsertEvent(planetId, playerId, EventType.SHIPS_TRANSFERRED)
    }

    override fun createResourcesTransferredEvent(playerId: UUID, planetId: UUID): Event {
        return createAndInsertEvent(planetId, playerId, EventType.RESOURCES_TRANSFERRED)
    }

    override fun createShipsReturnedEvent(playerId: UUID, planetId: UUID): Event {
        return createAndInsertEvent(planetId, playerId, EventType.SHIPS_RETURNED)
    }

    override fun createColonizedFailedEvent(playerId: UUID, planetId: UUID): Event {
        return createAndInsertEvent(planetId, playerId, EventType.COLONIZE_FAILED)
    }

    private fun createAndInsertEvent(planetId: UUID, playerId: UUID, type: EventType): Event {
        val id = uuidFactory.create()
        val round = roundService.currentRound()

        val event = Event(id, type, round, playerId, planetId)
        eventRepository.insert(event)
        return event
    }
}