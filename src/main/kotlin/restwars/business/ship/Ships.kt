package restwars.business.ship

import org.slf4j.LoggerFactory
import restwars.business.ShipFormulas
import restwars.business.UUIDFactory
import restwars.business.clock.RoundService
import restwars.business.planet.Planet
import java.util.*

enum class ShipType {
    MOSQUITO;

    companion object {
        fun parse(value: String): ShipType {
            return ShipType.valueOf(value)
        }
    }

}

data class Ship(val type: ShipType, val amount: Int)

data class ShipInConstruction(val id: UUID, val planetId: UUID, val type: ShipType, val done: Long)

data class Ships(val ships: List<Ship>) {
    operator fun get(type: ShipType): Int {
        return ships.find { it.type == type }?.amount ?: 0
    }

    fun with(type: ShipType, amount: Int): Ships {
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

    companion object {
        fun none(): Ships = Ships(listOf())
    }
}

data class Hangar(val id: UUID, val planetId: UUID, val ships: Ships) {
}

interface ShipService {
    fun buildShip(planet: Planet, type: ShipType): ShipInConstruction

    fun findShipsByPlanet(planet: Planet): Ships

    fun finishShipsInConstruction()
}

interface ShipInConstructionRepository {
    fun insert(shipInConstruction: ShipInConstruction)

    fun delete(id: UUID)

    fun findByDone(done: Long): List<ShipInConstruction>
}

interface HangarRepository {
    fun findByPlanetId(planetId: UUID): Hangar?

    fun insert(hangar: Hangar)

    fun updateShips(hangarId: UUID, type: ShipType, newAmount: Int)
}

class ShipServiceImpl(
        private val uuidFactory: UUIDFactory,
        private val roundService: RoundService,
        private val hangarRepository: HangarRepository,
        private val shipInConstructionRepository: ShipInConstructionRepository,
        private val shipFormulas: ShipFormulas
) : ShipService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun buildShip(planet: Planet, type: ShipType): ShipInConstruction {
        logger.debug("Building new ship of type {} on planet {}", type, planet.location)

        val currentRound = roundService.currentRound()
        val buildTime = shipFormulas.calculateBuildTime(type)
        val done = currentRound + buildTime

        val shipInConstruction = ShipInConstruction(uuidFactory.create(), planet.id, type, done)
        shipInConstructionRepository.insert(shipInConstruction)
        return shipInConstruction
    }

    override fun finishShipsInConstruction() {
        val currentRound = roundService.currentRound()

        val shipsDone = shipInConstructionRepository.findByDone(currentRound)
        for (shipDone in shipsDone) {
            logger.debug("Finishing ship {}", shipDone)

            val hangar = hangarRepository.findByPlanetId(shipDone.planetId)
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

    override fun findShipsByPlanet(planet: Planet): Ships {
        val hangar = hangarRepository.findByPlanetId(planet.id)

        return if (hangar == null) Ships.none() else hangar.ships
    }
}