package restwars.business.planet

import restwars.business.BuildingFormulas
import restwars.business.RandomNumberGenerator
import restwars.business.UUIDFactory
import restwars.business.config.GameConfig
import restwars.business.config.UniverseSize
import restwars.business.player.Player
import restwars.business.ship.Hangar
import restwars.business.ship.HangarRepository
import restwars.business.ship.Ships
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
            val parts = input.split('.')
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

data class Planet(val id: UUID, val owner: UUID, val location: Location, val resources: Resources) : Serializable {
    fun decreaseResources(loss: Resources): Planet = copy(resources = this.resources - loss)

    fun increaseResources(gain: Resources): Planet = copy(resources = this.resources + gain)
}

data class PlanetWithPlayer(val planet: Planet, val player: Player)

class NoTelescopeException : Exception("No telescope on planet")

interface PlanetService {
    fun createStarterPlanet(player: Player): Planet

    fun createPlanet(playerId: UUID, location: Location): Planet

    fun findByOwner(owner: Player): List<Planet>

    fun findByLocation(location: Location): Planet?

    fun findAllInhabited(): List<Planet>

    fun addResources(planet: Planet, resources: Resources): Planet

    fun removeResources(planet: Planet, resources: Resources): Planet

    /**
     * @throws NoTelescopeException If the planet has no telescope.
     */
    fun findInVicinity(planet: Planet, telescopeLevel: Int): List<PlanetWithPlayer>
}

interface PlanetRepository {
    fun findById(id: UUID): Planet?

    fun findByOwnerId(ownerId: UUID): List<Planet>

    fun insert(planet: Planet)

    fun findByLocation(location: Location): Planet?

    fun findAllInhabited(): List<Planet>

    fun updateResources(planetId: UUID, resources: Resources)

    fun findInRangeWithOwner(galaxyMin: Int, galaxyMax: Int, systemMin: Int, systemMax: Int, planetMin: Int, planetMax: Int): List<PlanetWithPlayer>

    fun countInhabited(): Int
}

class PlanetAlreadyExistsException(val location: Location) : Exception("Planet at location $location already exists")

class UniverseFullException() : Exception("Universe is full")

class PlanetServiceImpl(
        private val uuidFactory: UUIDFactory,
        private val randomNumberGenerator: RandomNumberGenerator,
        private val planetRepository: PlanetRepository,
        private val gameConfig: GameConfig,
        private val buildingFormulas: BuildingFormulas,
        private val hangarRepository: HangarRepository
) : PlanetService {
    override fun addResources(planet: Planet, resources: Resources): Planet {
        if (resources.isEmpty()) return planet

        val updatedPlanet = planet.increaseResources(resources)
        planetRepository.updateResources(planet.id, updatedPlanet.resources)
        return updatedPlanet
    }

    override fun removeResources(planet: Planet, resources: Resources): Planet {
        if (resources.isEmpty()) return planet

        val updatedPlanet = planet.decreaseResources(resources)
        planetRepository.updateResources(planet.id, updatedPlanet.resources)
        return updatedPlanet
    }

    override fun findByLocation(location: Location): Planet? {
        return planetRepository.findByLocation(location)
    }

    override fun findByOwner(owner: Player): List<Planet> = planetRepository.findByOwnerId(owner.id)

    override fun createStarterPlanet(player: Player): Planet {
        val totalPlanets = gameConfig.universeSize.maxPlanets * gameConfig.universeSize.maxSystems * gameConfig.universeSize.maxGalaxies

        if (planetRepository.countInhabited() == totalPlanets) {
            throw UniverseFullException()
        }

        // Find location which isn't already occupied
        var location: Location
        do {
            val galaxy = randomNumberGenerator.nextInt(1, gameConfig.universeSize.maxGalaxies)
            val system = randomNumberGenerator.nextInt(1, gameConfig.universeSize.maxSystems)
            val planet = randomNumberGenerator.nextInt(1, gameConfig.universeSize.maxPlanets)
            location = Location(galaxy, system, planet)
        } while (planetRepository.findByLocation(location) != null)

        val id = uuidFactory.create()
        val planet = Planet(id, player.id, location, gameConfig.starterPlanet.resources)
        planetRepository.insert(planet)
        hangarRepository.insert(Hangar(uuidFactory.create(), planet.id, Ships.none()))

        return planet
    }

    override fun findAllInhabited(): List<Planet> {
        return planetRepository.findAllInhabited()
    }

    override fun createPlanet(playerId: UUID, location: Location): Planet {
        if (findByLocation(location) != null) throw PlanetAlreadyExistsException(location)

        val planet = Planet(uuidFactory.create(), playerId, location, gameConfig.newPlanet.resources)
        planetRepository.insert(planet)
        hangarRepository.insert(Hangar(uuidFactory.create(), planet.id, Ships.none()))

        return planet
    }

    override fun findInVicinity(planet: Planet, telescopeLevel: Int): List<PlanetWithPlayer> {
        if (telescopeLevel < 1) throw NoTelescopeException()

        val range = buildingFormulas.calculateScanRange(telescopeLevel)

        val location = planet.location
        val galaxyMin = location.galaxy
        val galaxyMax = location.galaxy
        val systemMin = Math.max(location.system - range, 1)
        val systemMax = Math.min(location.system + range, gameConfig.universeSize.maxSystems)
        val planetMin = 1
        val planetMax = gameConfig.universeSize.maxPlanets

        return planetRepository.findInRangeWithOwner(galaxyMin, galaxyMax, systemMin, systemMax, planetMin, planetMax)
    }
}
