package restwars.business.ship

import org.slf4j.LoggerFactory
import restwars.business.BuildingFormulas
import restwars.business.ShipFormulas
import restwars.business.UUIDFactory
import restwars.business.building.BuildingService
import restwars.business.building.BuildingType
import restwars.business.clock.RoundService
import restwars.business.event.EventService
import restwars.business.planet.Planet
import restwars.business.planet.PlanetRepository
import restwars.business.resource.NotEnoughResourcesException
import restwars.business.sumByLong
import restwars.util.ceil
import java.io.Serializable
import java.util.*

enum class ShipType {
    MOSQUITO, COLONY, MULE;

    companion object {
        fun parse(value: String): ShipType {
            return ShipType.valueOf(value)
        }
    }

}

data class Ship(val type: ShipType, val amount: Int) : Serializable {
    init {
        if (amount < 0) throw IllegalArgumentException("amount must be >= 0")
    }
}

data class ShipInConstruction(val id: UUID, val planetId: UUID, val type: ShipType, val done: Long) : Serializable

data class Ships(val ships: List<Ship>) : Serializable {
    operator fun get(type: ShipType): Int {
        return ships.find { it.type == type }?.amount ?: 0
    }

    operator fun minus(other: Ships): Ships {
        return Ships(ships.map {
            val otherAmount = other[it.type]
            it.copy(amount = Math.max(0, it.amount - otherAmount))
        })
    }

    operator fun plus(other: Ships): Ships {
        val shipsNotInThis = other.ships.filter { this[it.type] == 0 }

        return Ships(
                ships.map {
                    it.copy(amount = it.amount + other[it.type])
                } + shipsNotInThis
        )
    }

    fun with(type: ShipType, amount: Int): Ships {
        if (amount < 0) throw IllegalArgumentException("amount must be >= 0")

        val containsShip = ships.any { it.type == type }
        if (!containsShip) {
            return copy(ships = ships + Ship(type, amount))
        } else {
            return copy(ships = ships.map {
                if (it.type == type) {
                    Ship(type, amount)
                } else {
                    it
                }
            })
        }
    }

    fun isEmpty(): Boolean {
        val amount = ships.map { it.amount }.sum()
        return amount <= 0
    }

    fun compact(): Ships {
        return Ships(ships.filter { it.amount > 0 })
    }

    fun amount(): Long {
        return ships.sumByLong { it.amount.toLong() }
    }

    companion object {
        fun none(): Ships = Ships(listOf())

        fun of(type: ShipType, amount: Int): Ships {
            if (amount < 0) throw IllegalArgumentException("amount must be >= 0")
            return Ships(listOf(Ship(type, amount)))
        }
    }
}

data class Hangar(val id: UUID, val planetId: UUID, val ships: Ships) : Serializable {
}

abstract class BuildShipException(message: String) : Exception(message)
class NotEnoughBuildSlotsException() : BuildShipException("Not enough build slots available")
class NoShipyardException() : BuildShipException("No shipyard on planet")

interface ShipService {
    /**
     * Builds a ship on the [planet].
     *
     * @throws NotEnoughBuildSlotsException If not enough build slots available.
     * @throws NotEnoughResourcesException If not enough resources are available.
     * @throws NoShipyardException If the planet has not shipyard.
     */
    fun buildShip(planet: Planet, type: ShipType): BuildResult

    fun findShipsInConstructionByPlanet(planet: Planet): List<ShipInConstruction>

    fun findShipsByPlanet(planet: Planet): Ships

    fun finishShipsInConstruction()

    fun removeShips(planet: Planet, ships: Ships)

    fun addShips(planet: Planet, ships: Ships)

    fun setShips(planet: Planet, ships: Ships)

    fun calculateCargoSpace(ships: Ships): Int
}

interface ShipInConstructionRepository {
    fun insert(shipInConstruction: ShipInConstruction)

    fun delete(id: UUID)

    fun findByPlanetId(planetId: UUID): List<ShipInConstruction>

    fun countByPlanetId(planetId: UUID): Int

    fun findByDone(done: Long): List<ShipInConstruction>
}

interface HangarRepository {
    fun findByPlanetId(planetId: UUID): Hangar?

    fun insert(hangar: Hangar)

    fun updateShips(hangarId: UUID, type: ShipType, newAmount: Int)
}

data class BuildResult(val planet: Planet, val shipInConstruction: ShipInConstruction)

class ShipServiceImpl(
        private val uuidFactory: UUIDFactory,
        private val roundService: RoundService,
        private val hangarRepository: HangarRepository,
        private val shipInConstructionRepository: ShipInConstructionRepository,
        private val shipFormulas: ShipFormulas,
        private val buildingFormulas: BuildingFormulas,
        private val planetRepository: PlanetRepository,
        private val buildingService: BuildingService,
        private val eventService: EventService
) : ShipService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun buildShip(planet: Planet, type: ShipType): BuildResult {
        logger.debug("Building new ship of type {} on planet {}", type, planet.location)

        val shipyardLevel = buildingService.findBuildingByPlanetAndType(planet, BuildingType.SHIPYARD)?.level ?: 0
        if (shipyardLevel < 1) throw NoShipyardException()

        // Check build slots
        val slots = buildingFormulas.calculateShipBuildSlots(shipyardLevel)
        val shipsInConstruction = shipInConstructionRepository.countByPlanetId(planet.id)
        if (shipsInConstruction >= slots) {
            throw NotEnoughBuildSlotsException()
        }

        val cost = shipFormulas.calculateBuildCost(type)
        if (!planet.resources.enough(cost)) {
            throw NotEnoughResourcesException(cost, planet.resources)
        }

        val currentRound = roundService.currentRound()
        val buildTime = calculateBuildTime(shipyardLevel, type)
        val done = currentRound + buildTime

        // Decrease resources
        val updatedPlanet = planet.decreaseResources(cost)
        planetRepository.updateResources(updatedPlanet.id, updatedPlanet.resources)

        // Create ship in construction
        val shipInConstruction = ShipInConstruction(uuidFactory.create(), planet.id, type, done)
        shipInConstructionRepository.insert(shipInConstruction)

        return BuildResult(updatedPlanet, shipInConstruction)
    }

    private fun calculateBuildTime(shipyardLevel: Int, type: ShipType): Int {
        val buildTimeModifier = buildingFormulas.calculateShipBuildTimeModifier(shipyardLevel)

        return Math.max(1, (shipFormulas.calculateBuildTime(type) * buildTimeModifier).ceil())
    }

    override fun finishShipsInConstruction() {
        val currentRound = roundService.currentRound()

        val shipsDone = shipInConstructionRepository.findByDone(currentRound)
        for (shipDone in shipsDone) {
            logger.debug("Finishing ship {}", shipDone)

            val planet = planetRepository.findById(shipDone.planetId) ?: throw AssertionError("Planet with id ${shipDone.planetId} not found")
            val hangar = hangarRepository.findByPlanetId(planet.id)

            eventService.createShipCompleteEvent(planet.owner, planet.id)
            if (hangar == null) {
                val newHangar = Hangar(uuidFactory.create(), shipDone.planetId, Ships(listOf(Ship(shipDone.type, 1))))
                hangarRepository.insert(newHangar)
            } else {
                val newAmount = hangar.ships[shipDone.type] + 1
                hangarRepository.updateShips(hangar.id, shipDone.type, newAmount)
            }
            shipInConstructionRepository.delete(shipDone.id)
        }
    }

    override fun findShipsInConstructionByPlanet(planet: Planet): List<ShipInConstruction> {
        return shipInConstructionRepository.findByPlanetId(planet.id)
    }

    override fun findShipsByPlanet(planet: Planet): Ships {
        val hangar = hangarRepository.findByPlanetId(planet.id)

        return if (hangar == null) Ships.none() else hangar.ships
    }

    override fun removeShips(planet: Planet, ships: Ships) {
        val hangar = hangarRepository.findByPlanetId(planet.id) ?: throw IllegalArgumentException("No hangar for planet $planet found")

        for (ship in ships.ships) {
            val newAmount = hangar.ships[ship.type] - ship.amount
            if (newAmount < 0) throw IllegalStateException("Amount of ships with type ${ship.type} have to be >= 0, would be $newAmount")

            hangarRepository.updateShips(hangar.id, ship.type, newAmount)
        }
    }

    override fun addShips(planet: Planet, ships: Ships) {
        val hangar = hangarRepository.findByPlanetId(planet.id)
        if (hangar == null) {
            val newHangar = Hangar(uuidFactory.create(), planet.id, ships)
            hangarRepository.insert(newHangar)
        } else {
            for (ship in ships.ships) {
                val newAmount = hangar.ships[ship.type] + ship.amount
                hangarRepository.updateShips(hangar.id, ship.type, newAmount)
            }
        }
    }

    override fun setShips(planet: Planet, ships: Ships) {
        val hangar = hangarRepository.findByPlanetId(planet.id)
        if (hangar == null) {
            val newHangar = Hangar(uuidFactory.create(), planet.id, ships)
            hangarRepository.insert(newHangar)
        } else {
            for (ship in ships.ships) {
                hangarRepository.updateShips(hangar.id, ship.type, ship.amount)
            }
        }
    }

    override fun calculateCargoSpace(ships: Ships): Int {
        return ships.ships.sumBy { shipFormulas.calculateCargoSpace(it.type) * it.amount }
    }
}