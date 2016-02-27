package restwars.business.planet

import restwars.business.RandomNumberGenerator
import restwars.business.UUIDFactory
import restwars.business.config.Config
import restwars.business.config.UniverseSize
import restwars.business.player.Player
import java.io.Serializable
import java.util.*

class InvalidLocationException(val location: Location) : Exception("Location $location is invalid")

data class Location(val galaxy: Int, val system: Int, val planet: Int) : Serializable {
    override fun toString(): String = "$galaxy.$system.$planet"

    fun isValid(universeSize: UniverseSize): Boolean {
        return galaxy in 1..universeSize.maxGalaxies &&
                system in 1..universeSize.maxSystems &&
                planet in 1..universeSize.maxPlanets
    }

    companion object {
        fun parse(input: String): Location {
            val parts = input.split(delimiters = '.')
            if (parts.size != 3) throw IllegalArgumentException("Unable to parse location from '$input'")

            return Location(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }
    }
}

data class Resources(val crystal: Int, val gas: Int, val energy: Int) : Serializable {
    companion object {
        fun crystal(crystal: Int) = Resources(crystal, 0, 0)

        fun gas(gas: Int) = Resources(0, gas, 0)

        fun energy(energy: Int) = Resources(0, 0, energy)

        fun none() = Resources(0, 0, 0)
    }

    operator fun plus(other: Resources) = Resources(crystal + other.crystal, gas + other.gas, energy + other.energy)

    operator fun minus(other: Resources) = Resources(crystal - other.crystal, gas - other.gas, energy - other.energy)

    fun isEmpty(): Boolean = crystal == 0 && gas == 0 && energy == 0

    fun enough(cost: Resources): Boolean {
        return crystal >= cost.crystal && gas >= cost.gas && energy >= cost.energy
    }
}

data class Planet(val id: UUID, val owner: UUID?, val location: Location, val resources: Resources) : Serializable {
    fun decreaseResources(loss: Resources): Planet = copy(resources = this.resources - loss)

    fun increaseResources(gain: Resources): Planet = copy(resources = this.resources + gain)
}

interface PlanetService {
    fun createStarterPlanet(player: Player): Planet

    fun createPlanet(playerId: UUID, location: Location): Planet

    fun findByOwner(owner: Player?): List<Planet>

    fun findByLocation(location: Location): Planet?

    fun findAllInhabited(): List<Planet>

    fun addResources(planet: Planet, resources: Resources): Planet
}

interface PlanetRepository {
    fun findAtLocation(location: Location): Planet?

    fun findByOwnerId(ownerId: UUID?): List<Planet>

    fun insert(planet: Planet)

    fun findByLocation(location: Location): Planet?

    fun findAllInhabited(): List<Planet>

    fun updateResources(planetId: UUID, resources: Resources)
}

class PlanetAlreadyExistsException(val location: Location) : Exception("Planet at location $location already exists")

class PlanetServiceImpl(
        private val uuidFactory: UUIDFactory,
        private val randomNumberGenerator: RandomNumberGenerator,
        private val planetRepository: PlanetRepository,
        private val config: Config
) : PlanetService {
    override fun addResources(planet: Planet, resources: Resources): Planet {
        val updatedPlanet = planet.increaseResources(resources)
        planetRepository.updateResources(planet.id, updatedPlanet.resources)
        return updatedPlanet
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

    override fun findAllInhabited(): List<Planet> {
        return planetRepository.findAllInhabited()
    }

    override fun createPlanet(playerId: UUID, location: Location): Planet {
        if (findByLocation(location) != null) throw PlanetAlreadyExistsException(location)

        val planet = Planet(uuidFactory.create(), playerId, location, config.newPlanet.resources)
        planetRepository.insert(planet)
        return planet
    }
}
