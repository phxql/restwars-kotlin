package restwars.storage

import org.jooq.DSLContext
import org.jooq.Record
import restwars.business.event.Event
import restwars.business.event.EventRepository
import restwars.business.event.EventType
import restwars.business.event.EventWithPlanet
import restwars.storage.jooq.Tables.EVENTS
import restwars.storage.jooq.Tables.PLANETS
import restwars.storage.jooq.tables.records.EventsRecord
import java.util.*

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