package restwars.storage

import org.jooq.DSLContext
import org.jooq.Record
import org.slf4j.LoggerFactory
import restwars.business.event.Event
import restwars.business.event.EventRepository
import restwars.business.event.EventType
import restwars.business.event.EventWithPlanet
import restwars.business.planet.PlanetRepository
import restwars.storage.jooq.Tables.EVENTS
import restwars.storage.jooq.Tables.PLANETS
import restwars.storage.jooq.tables.records.EventsRecord
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class JooqEventRepository(private val jooq: DSLContext) : EventRepository {
    override fun insert(event: Event) {
        jooq.insertInto(EVENTS, EVENTS.ID, EVENTS.PLANET_ID, EVENTS.PLAYER_ID, EVENTS.TYPE, EVENTS.ROUND)
                .values(event.id, event.planetId, event.playerId, event.type.name, event.round)
                .execute()
    }

    override fun findWithPlayer(playerId: UUID): List<EventWithPlanet> {
        return jooq.selectFrom(EVENTS.join(PLANETS).on(PLANETS.ID.eq(EVENTS.PLANET_ID)))
                .where(EVENTS.PLAYER_ID.eq(playerId))
                .fetch()
                .map { EventWithPlanet(JooqEventMapper.fromRecord(it), JooqPlanetMapper.fromRecord(it)) }
                .toList()
    }

    override fun findWithPlayerSince(playerId: UUID, since: Long): List<EventWithPlanet> {
        return jooq.selectFrom(EVENTS.join(PLANETS).on(PLANETS.ID.eq(EVENTS.PLANET_ID)))
                .where(EVENTS.PLAYER_ID.eq(playerId).and(EVENTS.ROUND.ge(since)))
                .fetch()
                .map { EventWithPlanet(JooqEventMapper.fromRecord(it), JooqPlanetMapper.fromRecord(it)) }
                .toList()
    }
}

object JooqEventMapper {
    fun fromRecord(record: Record): Event = fromRecord(record.into(EVENTS))

    fun fromRecord(record: EventsRecord): Event {
        return Event(
                record.id, EventType.valueOf(record.type), record.round, record.playerId, record.planetId
        )
    }
}

class InMemoryEventRepository(
        private val planetRepository: PlanetRepository
) : EventRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var events: MutableList<Event> = CopyOnWriteArrayList()

    override fun insert(event: Event) {
        logger.debug("Inserting {}", event)
        events.add(event)
    }

    override fun persist(persister: Persister, path: Path) {
        persister.saveData(path, events)
    }

    override fun findWithPlayer(playerId: UUID): List<EventWithPlanet> {
        return events.filter { it.playerId == playerId }.map {
            val planet = planetRepository.findById(it.planetId) ?: throw AssertionError("Planet with id ${it.planetId} not found")
            EventWithPlanet(it, planet)
        }
    }

    override fun findWithPlayerSince(playerId: UUID, since: Long): List<EventWithPlanet> {
        return events.filter { it.playerId == playerId && it.round >= since }.map {
            val planet = planetRepository.findById(it.planetId) ?: throw AssertionError("Planet with id ${it.planetId} not found")
            EventWithPlanet(it, planet)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(persister: Persister, path: Path) {
        events = persister.loadData(path) as MutableList<Event>
    }

}