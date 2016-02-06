package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.planet.Location
import restwars.business.planet.Planet
import restwars.business.planet.PlanetRepository
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

object InMemoryPlanetRepository : PlanetRepository {
    private val logger = LoggerFactory.getLogger(InMemoryPlanetRepository::class.java)
    private val planets: MutableList<Planet> = CopyOnWriteArrayList()

    override fun findAtLocation(location: Location): Planet? {
        return planets.firstOrNull() { it.location == location }
    }

    override fun insert(planet: Planet) {
        logger.info("Inserting planet {}", planet)
        planets.add(planet)
    }

    override fun findByOwnerId(ownerId: UUID?): List<Planet> {
        return planets.filter { it.owner == ownerId }
    }

    override fun findByLocation(location: Location): Planet? {
        return planets.firstOrNull { it.location == location }
    }
}