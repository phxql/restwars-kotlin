package restwars.storage

import org.jooq.DSLContext
import restwars.business.building.*
import restwars.storage.jooq.Tables.BUILDINGS
import restwars.storage.jooq.Tables.CONSTRUCTION_SITES
import restwars.storage.jooq.tables.records.BuildingsRecord
import restwars.storage.jooq.tables.records.ConstructionSitesRecord
import java.util.*

class JooqBuildingRepository(val jooq: DSLContext) : BuildingRepository {
    override fun insert(building: Building) {
        jooq.insertInto(BUILDINGS, BUILDINGS.ID, BUILDINGS.PLANET_ID, BUILDINGS.BUILDING_TYPE, BUILDINGS.LEVEL)
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
                .map { JooqBuildingMapper.fromRecord(it) }
                .toList()
    }

    override fun findByPlanetIdAndType(planetId: UUID, type: BuildingType): Building? {
        val record = jooq.selectFrom(BUILDINGS)
                .where(BUILDINGS.PLANET_ID.eq(planetId).and(BUILDINGS.BUILDING_TYPE.eq(type.name)))
                .fetchOne() ?: return null
        return JooqBuildingMapper.fromRecord(record)
    }
}

object JooqBuildingMapper {
    fun fromRecord(record: BuildingsRecord): Building {
        return Building(
                record.id, record.planetId, BuildingType.valueOf(record.buildingType), record.level
        )
    }
}

class JooqConstructionSiteRepository(private val jooq: DSLContext) : ConstructionSiteRepository {
    override fun insert(constructionSite: ConstructionSite) {
        jooq.insertInto(CONSTRUCTION_SITES, CONSTRUCTION_SITES.ID, CONSTRUCTION_SITES.PLANET_ID, CONSTRUCTION_SITES.BUILDING_TYPE, CONSTRUCTION_SITES.LEVEL, CONSTRUCTION_SITES.DONE)
                .values(constructionSite.id, constructionSite.planetId, constructionSite.type.name, constructionSite.level, constructionSite.done)
                .execute()
    }

    override fun findByDone(done: Long): List<ConstructionSite> {
        return jooq.selectFrom(CONSTRUCTION_SITES)
                .where(CONSTRUCTION_SITES.DONE.eq(done))
                .fetch()
                .map { JooqConstructionSiteMapper.fromRecord(it) }
                .toList()
    }

    override fun findByPlanetId(planetId: UUID): List<ConstructionSite> {
        return jooq.selectFrom(CONSTRUCTION_SITES)
                .where(CONSTRUCTION_SITES.PLANET_ID.eq(planetId))
                .fetch()
                .map { JooqConstructionSiteMapper.fromRecord(it) }
                .toList()
    }

    override fun countByPlanetId(planetId: UUID): Int {
        return jooq.fetchCount(
                jooq.selectFrom(CONSTRUCTION_SITES)
                        .where(CONSTRUCTION_SITES.PLANET_ID.eq(planetId))
        )
    }

    override fun findByPlanetIdAndType(planetId: UUID, type: BuildingType): ConstructionSite? {
        val record = jooq.selectFrom(CONSTRUCTION_SITES)
                .where(CONSTRUCTION_SITES.PLANET_ID.eq(planetId).and(CONSTRUCTION_SITES.BUILDING_TYPE.eq(type.name)))
                .fetchOne() ?: return null
        return JooqConstructionSiteMapper.fromRecord(record)
    }

    override fun delete(id: UUID) {
        jooq.deleteFrom(CONSTRUCTION_SITES)
                .where(CONSTRUCTION_SITES.ID.eq(id))
                .execute()
    }
}

object JooqConstructionSiteMapper {
    fun fromRecord(record: ConstructionSitesRecord): ConstructionSite {
        return ConstructionSite(
                record.id, record.planetId, BuildingType.valueOf(record.buildingType), record.level, record.done
        )
    }
}