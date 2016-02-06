package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.building.Building
import restwars.business.building.BuildingRepository
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

object InMemoryBuildingRepository : BuildingRepository {
    private val logger = LoggerFactory.getLogger(InMemoryBuildingRepository::class.java)
    private val buildings: MutableList<Building> = CopyOnWriteArrayList()

    override fun findByPlanetId(planetId: UUID): List<Building> {
        return buildings.filter { it.planetId == planetId }
    }

    override fun insert(building: Building) {
        logger.info("Inserting building {}", building)
        buildings.add(building)
    }
}