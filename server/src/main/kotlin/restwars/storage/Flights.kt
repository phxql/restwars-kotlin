package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.flight.Flight
import restwars.business.flight.FlightDirection
import restwars.business.flight.FlightRepository
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

}
