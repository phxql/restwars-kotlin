package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.flight.*
import restwars.business.planet.Location
import restwars.business.planet.Resources
import restwars.business.ship.Ships
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

object InMemoryFlightRepository : FlightRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var flights: MutableList<Flight> = CopyOnWriteArrayList()

    override fun insert(flight: Flight) {
        logger.debug("Inserting {}", flight)
        flights.add(flight)

    }

    override fun persist(persister: Persister, path: Path) {
        persister.saveData(path, flights)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(persister: Persister, path: Path) {
        flights = persister.loadData(path) as MutableList<Flight>
    }

    override fun findWithId(flightId: UUID): Flight? {
        return flights.firstOrNull { it.id == flightId }
    }

    override fun update(id: UUID, ships: Ships, arrivalInRound: Long, direction: FlightDirection, cargo: Resources) {
        val index = flights.indexOfFirst { it.id == id }

        val flight = flights[index]
        flights[index] = flight.copy(ships = ships, arrivalInRound = arrivalInRound, direction = direction, cargo = cargo)
    }

    override fun findWithPlayerAndDestination(playerId: UUID, destination: Location): List<Flight> {
        return flights.filter { it.playerId == playerId && it.destination == destination }
    }

    override fun findWithPlayerAndStart(playerId: UUID, start: Location): List<Flight> {
        return flights.filter { it.playerId == playerId && it.start == start }
    }

    override fun findWithPlayer(playerId: UUID): List<Flight> {
        return flights.filter { it.playerId == playerId }
    }

    override fun delete(id: UUID) {
        flights.removeAll { it.id == id }
    }

    override fun findByArrivalInRound(arrivalInRound: Long): List<Flight> {
        return flights.filter { it.arrivalInRound == arrivalInRound }
    }

    override fun findUndetectedFlights(): List<Flight> {
        return flights.filter { !it.detected }
    }

    override fun updateDetected(flightId: UUID, detected: Boolean) {
        val index = flights.indexOfFirst { it.id == flightId }

        val flight = flights[index]
        flights[index] = flight.copy(detected = detected)
    }
}

class InMemoryDetectedFlightRepository(
        private val flightRepository: FlightRepository
) : DetectedFlightRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var detectedFlights: MutableList<DetectedFlight> = CopyOnWriteArrayList()

    override fun insert(detectedFlight: DetectedFlight) {
        logger.debug("Inserting {}", detectedFlight)
        detectedFlights.add(detectedFlight)
    }

    override fun findWithPlayer(playerId: UUID): List<DetectedFlightWithFlight> {
        return detectedFlights.filter { it.playerId == playerId }.map {
            val flight = flightRepository.findWithId(it.flightId) ?: throw AssertionError("Flight with id ${it.flightId} not found")
            DetectedFlightWithFlight(it, flight)
        }
    }

    override fun findWithPlayerSince(playerId: UUID, since: Long): List<DetectedFlightWithFlight> {
        return detectedFlights.filter { it.playerId == playerId && it.detectedInRound >= since }.map {
            val flight = flightRepository.findWithId(it.flightId) ?: throw AssertionError("Flight with id ${it.flightId} not found")
            DetectedFlightWithFlight(it, flight)
        }
    }

    override fun deleteWithFlightId(flightId: UUID) {
        detectedFlights.removeAll { it.flightId == flightId }
    }

    override fun persist(persister: Persister, path: Path) {
        persister.saveData(path, detectedFlights)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(persister: Persister, path: Path) {
        detectedFlights = persister.loadData(path) as MutableList<DetectedFlight>
    }
}


