package restwars.business.planet

import restwars.business.RandomNumberGenerator
import restwars.business.UUIDFactory
import restwars.business.config.Config
import restwars.business.player.Player
import java.util.*

data class Location(val galaxy: Int, val system: Int, val planet: Int) {
    override fun toString(): String = "$galaxy.$system.$planet"

    companion object {
        fun parse(input: String): Location {
            val parts = input.split(delimiters = '.')
            if (parts.size != 3) throw IllegalArgumentException("Unable to parse location from '$input'")

            return Location(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }
    }
}

data class Resources(val crystal: Int, val gas: Int, val energy: Int) {
    companion object {
        fun crystal(crystal: Int) = Resources(crystal, 0, 0)

        fun gas(gas: Int) = Resources(0, gas, 0)

        fun energy(energy: Int) = Resources(0, 0, energy)

        fun none() = Resources(0, 0, 0)
    }

    operator fun plus(other: Resources) = Resources(crystal + other.crystal, gas + other.gas, energy + other.energy)
}

data class Planet(val id: UUID, val owner: UUID?, val location: Location, val resources: Resources)

interface PlanetService {
    fun createStarterPlanet(player: Player): Planet

    fun findByOwner(owner: Player?): List<Planet>

    fun findByLocation(location: Location): Planet?

    fun findAllInhabitated(): List<Planet>

    fun addResources(planet: Planet, resources: Resources): Planet
}

interface PlanetRepository {
    fun findAtLocation(location: Location): Planet?

    fun findByOwnerId(ownerId: UUID?): List<Planet>

    fun insert(planet: Planet)

    fun findByLocation(location: Location): Planet?

    fun findAllInhabitated(): List<Planet>

    fun addResources(planetId: UUID, resources: Resources)
}

class PlanetServiceImpl(
        private val uuidFactory: UUIDFactory,
        private val randomNumberGenerator: RandomNumberGenerator,
        private val planetRepository: PlanetRepository,
        private val config: Config
) : PlanetService {
    override fun addResources(planet: Planet, resources: Resources): Planet {
        planetRepository.addResources(planet.id, resources)

        return planet.copy(resources = planet.resources + resources)
    }

    override fun findByLocation(location: Location): Planet? {
        return planetRepository.findByLocation(location)
    }

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
        val planet = Planet(id, player.id, location, config.starterPlanet.resources)
        planetRepository.insert(planet)

        return planet
    }

    override fun findAllInhabitated(): List<Planet> {
        return planetRepository.findAllInhabitated()
    }
}