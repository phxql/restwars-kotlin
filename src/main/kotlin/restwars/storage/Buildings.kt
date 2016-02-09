package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.building.*
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

    override fun findByPlanetIdAndType(planetId: UUID, type: BuildingType): Building? {
        return buildings.firstOrNull { it.planetId == planetId && it.type == type }
    }
}

object InMemoryConstructionSiteRepository : ConstructionSiteRepository {
    private val logger = LoggerFactory.getLogger(InMemoryConstructionSiteRepository::class.java)
    private val constructionSites: MutableList<ConstructionSite> = CopyOnWriteArrayList()

    override fun insert(constructionSite: ConstructionSite) {
        logger.info("Inserting construction site $constructionSite")

        constructionSites.add(constructionSite)
    }
}