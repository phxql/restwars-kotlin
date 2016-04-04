package restwars.storage

import org.jooq.DSLContext
import org.jooq.Record
import restwars.business.flight.*
import restwars.business.planet.Location
import restwars.business.planet.Resources
import restwars.business.ship.Ship
import restwars.business.ship.ShipType
import restwars.business.ship.Ships
import restwars.storage.jooq.Tables.*
import restwars.storage.jooq.tables.records.DetectedFlightsRecord
import java.util.*

class JooqFlightRepository(private val jooq: DSLContext) : FlightRepository {
    override fun insert(flight: Flight) {
        jooq
                .insertInto(
                        FLIGHTS, FLIGHTS.ID, FLIGHTS.PLAYER_ID, FLIGHTS.START_GALAXY, FLIGHTS.START_SYSTEM, FLIGHTS.START_PLANET,
                        FLIGHTS.DESTINATION_GALAXY, FLIGHTS.DESTINATION_SYSTEM, FLIGHTS.DESTINATION_PLANET, FLIGHTS.STARTED_IN_ROUND,
                        FLIGHTS.ARRIVAL_IN_ROUND, FLIGHTS.DIRECTION, FLIGHTS.TYPE, FLIGHTS.CARGO_CRYSTAL, FLIGHTS.CARGO_GAS,
                        FLIGHTS.DETECTED, FLIGHTS.SPEED
                )
                .values(
                        flight.id, flight.playerId, flight.start.galaxy, flight.start.system, flight.start.planet,
                        flight.destination.galaxy, flight.destination.system, flight.destination.planet, flight.startedInRound,
                        flight.arrivalInRound, flight.direction.name, flight.type.name, flight.cargo.crystal, flight.cargo.gas,
                        flight.detected, flight.speed
                )
                .execute()

        insertFlightShips(flight.id, flight.ships)
    }

    private fun insertFlightShips(flightId: UUID, ships: Ships) {
        for (ship in ships.ships) {
            if (ship.amount > 0) {
                jooq
                        .insertInto(FLIGHT_SHIPS, FLIGHT_SHIPS.FLIGHT_ID, FLIGHT_SHIPS.TYPE, FLIGHT_SHIPS.AMOUNT)
                        .values(flightId, ship.type.name, ship.amount)
                        .execute()
            }
        }
    }

    override fun update(id: UUID, ships: Ships, arrivalInRound: Long, direction: FlightDirection, cargo: Resources) {
        jooq
                .update(FLIGHTS)
                .set(FLIGHTS.ARRIVAL_IN_ROUND, arrivalInRound)
                .set(FLIGHTS.DIRECTION, direction.name)
                .set(FLIGHTS.CARGO_CRYSTAL, cargo.crystal)
                .set(FLIGHTS.CARGO_GAS, cargo.gas)
                .where(FLIGHTS.ID.eq(id))
                .execute()

        // Delete ships
        jooq.deleteFrom(FLIGHT_SHIPS)
                .where(FLIGHT_SHIPS.FLIGHT_ID.eq(id))
                .execute()

        // Insert updated ships
        insertFlightShips(id, ships)
    }

    override fun delete(id: UUID) {
        jooq
                .deleteFrom(FLIGHT_SHIPS)
                .where(FLIGHT_SHIPS.FLIGHT_ID.eq(id))
                .execute()

        jooq
                .deleteFrom(DETECTED_FLIGHTS)
                .where(DETECTED_FLIGHTS.FLIGHT_ID.eq(id))
                .execute()

        jooq
                .deleteFrom(FLIGHTS)
                .where(FLIGHTS.ID.eq(id))
                .execute()

    }

    override fun findByArrivalInRound(arrivalInRound: Long): List<Flight> {
        val records = jooq
                .selectFrom(FLIGHTS.leftJoin(FLIGHT_SHIPS).on(FLIGHT_SHIPS.FLIGHT_ID.eq(FLIGHTS.ID)))
                .where(FLIGHTS.ARRIVAL_IN_ROUND.eq(arrivalInRound))
                .fetchGroups(FLIGHTS.ID)

        return records.values.map { JooqFlightMapper.fromRecords(it) }
    }

    override fun findWithPlayerAndDestination(playerId: UUID, destination: Location): List<Flight> {
        val records = jooq
                .selectFrom(FLIGHTS.leftJoin(FLIGHT_SHIPS).on(FLIGHT_SHIPS.FLIGHT_ID.eq(FLIGHTS.ID)))
                .where(FLIGHTS.PLAYER_ID.eq(playerId)
                        .and(FLIGHTS.DESTINATION_GALAXY.eq(destination.galaxy))
                        .and(FLIGHTS.DESTINATION_SYSTEM.eq(destination.system))
                        .and(FLIGHTS.DESTINATION_PLANET.eq(destination.planet))
                )
                .fetchGroups(FLIGHTS.ID)

        return records.values.map { JooqFlightMapper.fromRecords(it) }
    }

    override fun findWithPlayerAndStart(playerId: UUID, start: Location): List<Flight> {
        val records = jooq
                .selectFrom(FLIGHTS.leftJoin(FLIGHT_SHIPS).on(FLIGHT_SHIPS.FLIGHT_ID.eq(FLIGHTS.ID)))
                .where(FLIGHTS.PLAYER_ID.eq(playerId)
                        .and(FLIGHTS.START_GALAXY.eq(start.galaxy))
                        .and(FLIGHTS.START_SYSTEM.eq(start.system))
                        .and(FLIGHTS.START_PLANET.eq(start.planet))
                )
                .fetchGroups(FLIGHTS.ID)

        return records.values.map { JooqFlightMapper.fromRecords(it) }
    }

    override fun findWithPlayer(playerId: UUID): List<Flight> {
        val records = jooq
                .selectFrom(FLIGHTS.leftJoin(FLIGHT_SHIPS).on(FLIGHT_SHIPS.FLIGHT_ID.eq(FLIGHTS.ID)))
                .where(FLIGHTS.PLAYER_ID.eq(playerId))
                .fetchGroups(FLIGHTS.ID)

        return records.values.map { JooqFlightMapper.fromRecords(it) }
    }

    override fun findUndetectedFlights(): List<Flight> {
        val records = jooq
                .selectFrom(FLIGHTS.leftJoin(FLIGHT_SHIPS).on(FLIGHT_SHIPS.FLIGHT_ID.eq(FLIGHTS.ID)))
                .where(FLIGHTS.DETECTED.eq(false))
                .fetchGroups(FLIGHTS.ID)

        return records.values.map { JooqFlightMapper.fromRecords(it) }
    }

    override fun updateDetected(flightId: UUID, detected: Boolean) {
        jooq
                .update(FLIGHTS)
                .set(FLIGHTS.DETECTED, detected)
                .where(FLIGHTS.ID.eq(flightId))
                .execute()
    }

    override fun findWithId(flightId: UUID): Flight? {
        val records = jooq
                .selectFrom(FLIGHTS.leftJoin(FLIGHT_SHIPS).on(FLIGHT_SHIPS.FLIGHT_ID.eq(FLIGHTS.ID)))
                .where(FLIGHTS.ID.eq(flightId))
                .fetch().toList()

        if (records.isEmpty()) return null

        return JooqFlightMapper.fromRecords(records)
    }
}

object JooqFlightMapper {
    fun fromRecords(records: List<Record>): Flight {
        val flightRecords = records.map { it.into(FLIGHTS) }
        val flightShipsRecords = records.map { it.into(FLIGHT_SHIPS) }
        val flight = flightRecords[0]

        val ships = if (flightShipsRecords[0].type == null) {
            // Happens if the LEFT JOIN has no rows in flight_ships
            Ships.none()
        } else {
            Ships(flightShipsRecords.map { Ship(ShipType.valueOf(it.type), it.amount) })
        }

        return Flight(
                flight.id, flight.playerId, Location(flight.startGalaxy, flight.startSystem, flight.startPlanet),
                Location(flight.destinationGalaxy, flight.destinationSystem, flight.destinationPlanet),
                flight.startedInRound, flight.arrivalInRound, ships, FlightDirection.valueOf(flight.direction),
                FlightType.valueOf(flight.type), Resources(flight.cargoCrystal, flight.cargoGas, 0), flight.detected,
                flight.speed
        )
    }
}

class JooqDetectedFlightRepository(private val jooq: DSLContext) : DetectedFlightRepository {
    override fun insert(detectedFlight: DetectedFlight) {
        jooq
                .insertInto(DETECTED_FLIGHTS, DETECTED_FLIGHTS.ID, DETECTED_FLIGHTS.FLIGHT_ID, DETECTED_FLIGHTS.PLAYER_ID, DETECTED_FLIGHTS.DETECTED_IN_ROUND, DETECTED_FLIGHTS.APPROXIMATED_FLIGHT_SIZE)
                .values(detectedFlight.id, detectedFlight.flightId, detectedFlight.playerId, detectedFlight.detectedInRound, detectedFlight.approximatedFleetSize)
                .execute()
    }

    override fun findWithPlayer(playerId: UUID): List<DetectedFlightWithFlight> {
        val records = jooq.selectFrom(DETECTED_FLIGHTS.join(FLIGHTS).on(FLIGHTS.ID.eq(DETECTED_FLIGHTS.FLIGHT_ID)))
                .where(DETECTED_FLIGHTS.PLAYER_ID.eq(playerId))
                .fetchGroups(FLIGHTS.ID)

        return records.values
                .map { DetectedFlightWithFlight(JooqDetectedFlightMapper.fromRecord(it[0]), JooqFlightMapper.fromRecords(it)) }
    }

    override fun findWithPlayerSince(playerId: UUID, since: Long): List<DetectedFlightWithFlight> {
        val records = jooq.selectFrom(DETECTED_FLIGHTS.join(FLIGHTS).on(FLIGHTS.ID.eq(DETECTED_FLIGHTS.FLIGHT_ID)))
                .where(DETECTED_FLIGHTS.PLAYER_ID.eq(playerId).and(DETECTED_FLIGHTS.DETECTED_IN_ROUND.ge(since)))
                .fetchGroups(FLIGHTS.ID)

        return records.values
                .map { DetectedFlightWithFlight(JooqDetectedFlightMapper.fromRecord(it[0]), JooqFlightMapper.fromRecords(it)) }
    }
}

object JooqDetectedFlightMapper {
    fun fromRecord(record: Record): DetectedFlight = fromRecord(record.into(DETECTED_FLIGHTS))

    fun fromRecord(record: DetectedFlightsRecord): DetectedFlight {
        return DetectedFlight(
                record.id, record.flightId, record.playerId, record.detectedInRound, record.approximatedFlightSize
        )
    }
}


