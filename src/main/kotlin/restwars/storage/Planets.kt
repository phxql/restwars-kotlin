package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.planet.*
import restwars.business.player.PlayerRepository
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class InMemoryPlanetRepository(
        private val playerRepository: PlayerRepository
) : PlanetRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var planets: MutableList<Planet> = CopyOnWriteArrayList()

    override fun findAllInhabited(): List<Planet> {
        return planets
    }

    override fun updateResources(planetId: UUID, resources: Resources) {
        logger.debug("Updating resources of planet {} to {}", planetId, resources)

        val index = planets.indexOfFirst { it.id == planetId }

        val planet = planets[index]
        planets[index] = planet.copy(resources = resources)
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

    override fun findInRangeWithOwner(galaxyMin: Int, galaxyMax: Int, systemMin: Int, systemMax: Int, planetMin: Int, planetMax: Int): List<PlanetWithPlayer> {
        val planets = planets.filter {
            it.location.galaxy in galaxyMin..galaxyMax &&
                    it.location.system in systemMin..systemMax &&
                    it.location.planet in planetMin..planetMax
        }

        return planets.map { PlanetWithPlayer(it, playerRepository.findById(it.owner)!!) }
    }

    override fun persist(persister: Persister, path: Path) {
        persister.saveData(path, planets)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(persister: Persister, path: Path) {
        planets = persister.loadData(path) as MutableList<Planet>
    }
}