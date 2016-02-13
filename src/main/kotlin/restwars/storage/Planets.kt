package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.planet.Location
import restwars.business.planet.Planet
import restwars.business.planet.PlanetRepository
import restwars.business.planet.Resources
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

object InMemoryPlanetRepository : PlanetRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var planets: MutableList<Planet> = CopyOnWriteArrayList()

    override fun findAllInhabitated(): List<Planet> {
        return planets.filter { it.owner != null }
    }

    override fun addResources(planetId: UUID, resources: Resources) {
        val index = planets.indexOfFirst { it.id == planetId }

        val planet = planets[index]
        planets[index] = planet.copy(resources = planet.resources + resources)
    }

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

    override fun persist(path: Path) {
        Persister.saveData(path, planets)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(path: Path) {
        planets = Persister.loadData(path) as MutableList<Planet>
    }
}