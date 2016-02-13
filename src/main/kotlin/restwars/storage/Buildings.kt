package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.building.*
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

object InMemoryBuildingRepository : BuildingRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var buildings: MutableList<Building> = CopyOnWriteArrayList()

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

    override fun updateLevel(buildingId: UUID, newLevel: Int) {
        val index = buildings.indexOfFirst { it.id == buildingId }

        buildings[index] = buildings[index].copy(level = newLevel)
    }

    override fun persist(path: Path) {
        Persister.saveData(path, buildings)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(path: Path) {
        this.buildings = Persister.loadData(path) as MutableList<Building>
    }
}

object InMemoryConstructionSiteRepository : ConstructionSiteRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var constructionSites: MutableList<ConstructionSite> = CopyOnWriteArrayList()

    override fun insert(constructionSite: ConstructionSite) {
        logger.info("Inserting construction site $constructionSite")

        constructionSites.add(constructionSite)
    }

    override fun findByDone(done: Long): List<ConstructionSite> {
        return constructionSites.filter { it.done == done }
    }

    override fun delete(id: UUID) {
        constructionSites.removeAll { it.id == id }
    }

    override fun findByPlanetId(planetId: UUID): List<ConstructionSite> {
        return constructionSites.filter { it.planetId == planetId }
    }

    override fun persist(path: Path) {
        Persister.saveData(path, constructionSites)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(path: Path) {
        this.constructionSites = Persister.loadData(path) as MutableList<ConstructionSite>
    }
}