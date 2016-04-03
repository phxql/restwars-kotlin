package restwars.storage

import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import restwars.business.building.*
import restwars.storage.jooq.Tables.BUILDINGS
import restwars.storage.jooq.tables.records.BuildingsRecord
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class JooqBuildingRepository(val jooq: DSLContext) : BuildingRepository {
    override fun insert(building: Building) {
        jooq.insertInto(BUILDINGS, BUILDINGS.ID, BUILDINGS.PLANET_ID, BUILDINGS.TYPE, BUILDINGS.LEVEL)
                .values(building.id, building.planetId, building.type.name, building.level)
                .execute()
    }

    override fun updateLevel(buildingId: UUID, newLevel: Int) {
        jooq.update(BUILDINGS)
                .set(BUILDINGS.LEVEL, newLevel)
                .where(BUILDINGS.ID.eq(buildingId))
                .execute()
    }

    override fun findByPlanetId(planetId: UUID): List<Building> {
        return jooq.selectFrom(BUILDINGS)
                .where(BUILDINGS.PLANET_ID.eq(planetId))
                .fetch()
                .map { JooqBuildingMapper.toBuilding(it) }
                .toList()
    }

    override fun findByPlanetIdAndType(planetId: UUID, type: BuildingType): Building? {
        val record = jooq.selectFrom(BUILDINGS)
                .where(BUILDINGS.PLANET_ID.eq(planetId).and(BUILDINGS.TYPE.eq(type.name)))
                .fetchOne() ?: return null
        return JooqBuildingMapper.toBuilding(record)
    }
}

object JooqBuildingMapper {
    fun toBuilding(record: BuildingsRecord): Building {
        return Building(
                record.id, record.planetId, BuildingType.valueOf(record.type), record.level
        )
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

    override fun persist(persister: Persister, path: Path) {
        persister.saveData(path, constructionSites)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(persister: Persister, path: Path) {
        this.constructionSites = persister.loadData(path) as MutableList<ConstructionSite>
    }

    override fun countByPlanetId(planetId: UUID): Int {
        return constructionSites.count { it.planetId == planetId }
    }

    override fun findByPlanetIdAndType(planetId: UUID, type: BuildingType): ConstructionSite? {
        return constructionSites.find { it.planetId == planetId && it.type == type }
    }
}