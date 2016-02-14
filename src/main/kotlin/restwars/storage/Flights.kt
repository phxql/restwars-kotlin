package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.flight.Flight
import restwars.business.flight.FlightRepository
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList

object InMemoryFlightRepository : FlightRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var flights: MutableList<Flight> = CopyOnWriteArrayList()

    override fun insert(flight: Flight) {
        logger.debug("Inserting {}", flight)
        flights.add(flight)

    }

    override fun persist(path: Path) {
        Persister.saveData(path, flights)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(path: Path) {
        flights = Persister.loadData(path) as MutableList<Flight>
    }
}
