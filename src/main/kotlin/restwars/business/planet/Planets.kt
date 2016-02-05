package restwars.business.planet

import restwars.business.RandomNumberGenerator
import restwars.business.UUIDFactory
import restwars.business.config.Config
import restwars.business.player.Player
import java.util.*

data class Location(val galaxy: Int, val system: Int, val planet: Int)

data class Planet(val id: UUID, val owner: UUID?, val location: Location)

interface PlanetService {
    fun createStarterPlanet(player: Player): Planet

    fun findByOwner(owner: Player?): List<Planet>
}

interface PlanetRepository {
    fun findAtLocation(location: Location): Planet?

    fun findByOwnerId(ownerId: UUID?): List<Planet>

    fun insert(planet: Planet)
}

class PlanetServiceImpl(
        private val uuidFactory: UUIDFactory,
        private val randomNumberGenerator: RandomNumberGenerator,
        private val planetRepository: PlanetRepository,
        private val config: Config
) : PlanetService {
    override fun findByOwner(owner: Player?): List<Planet> = planetRepository.findByOwnerId(owner?.id)

    override fun createStarterPlanet(player: Player): Planet {
        // Find location which isn't already occupied
        var location: Location
        do {
            val galaxy = randomNumberGenerator.nextInt(1, config.universeSize.maxGalaxies)
            val system = randomNumberGenerator.nextInt(1, config.universeSize.maxSystems)
            val planet = randomNumberGenerator.nextInt(1, config.universeSize.maxPlanets)
            location = Location(galaxy, system, planet)
        } while (planetRepository.findAtLocation(location) != null)

        val id = uuidFactory.create()
        val planet = Planet(id, player.id, location)
        planetRepository.insert(planet)

        return planet
    }
}
