package restwars.storage

import org.jooq.DSLContext
import org.jooq.Record
import restwars.business.planet.*
import restwars.storage.jooq.Tables.PLANETS
import restwars.storage.jooq.Tables.PLAYERS
import restwars.storage.jooq.tables.records.PlanetsRecord
import java.util.*

class JooqPlanetRepository(private val jooq: DSLContext) : PlanetRepository {
    override fun findByOwnerId(ownerId: UUID): List<Planet> {
        return jooq.selectFrom(PLANETS)
                .where(PLANETS.OWNER_ID.eq(ownerId))
                .fetch().map { JooqPlanetMapper.toPlanet(it) }.toList()
    }

    override fun findById(id: UUID): Planet? {
        val record = jooq.selectFrom(PLANETS)
                .where(PLANETS.ID.eq(id))
                .fetchOne() ?: return null
        return JooqPlanetMapper.toPlanet(record)
    }

    override fun insert(planet: Planet) {
        jooq
                .insertInto(PLANETS, PLANETS.ID, PLANETS.OWNER_ID, PLANETS.GALAXY, PLANETS.SYSTEM, PLANETS.PLANET, PLANETS.CRYSTAL, PLANETS.GAS, PLANETS.ENERGY)
                .values(planet.id, planet.owner, planet.location.galaxy, planet.location.system, planet.location.planet, planet.resources.crystal, planet.resources.gas, planet.resources.energy)
                .execute()
    }

    override fun findByLocation(location: Location): Planet? {
        val record = jooq.selectFrom(PLANETS)
                .where(PLANETS.GALAXY.eq(location.galaxy), PLANETS.SYSTEM.eq(location.system), PLANETS.PLANET.eq(location.planet))
                .fetchOne() ?: return null
        return JooqPlanetMapper.toPlanet(record)
    }

    override fun findAllInhabited(): List<Planet> {
        return jooq.selectFrom(PLANETS)
                .fetch().map { JooqPlanetMapper.toPlanet(it) }.toList()
    }

    override fun updateResources(planetId: UUID, resources: Resources) {
        jooq.update(PLANETS)
                .set(PLANETS.CRYSTAL, resources.crystal)
                .set(PLANETS.GAS, resources.gas)
                .set(PLANETS.ENERGY, resources.energy)
                .where(PLANETS.ID.eq(planetId))
                .execute()
    }

    override fun findInRangeWithOwner(galaxyMin: Int, galaxyMax: Int, systemMin: Int, systemMax: Int, planetMin: Int, planetMax: Int): List<PlanetWithPlayer> {
        return jooq
                .selectFrom(PLANETS.join(PLAYERS).on(PLAYERS.ID.eq(PLANETS.OWNER_ID)))
                .where(PLANETS.GALAXY.between(galaxyMin, galaxyMax)
                        .and(PLANETS.SYSTEM.between(systemMin, systemMax))
                        .and(PLANETS.PLANET.between(planetMin, planetMax))
                ).map { PlanetWithPlayer(JooqPlanetMapper.toPlanet(it), JooqPlayerMapper.fromRecord(it)) }
                .toList()
    }
}

object JooqPlanetMapper {
    fun toPlanet(record: Record): Planet = toPlanet(record.into(PlanetsRecord::class.java))

    fun toPlanet(record: PlanetsRecord): Planet {
        return Planet(
                record.id, record.ownerId, Location(record.galaxy, record.system, record.planet),
                Resources(record.crystal, record.gas, record.energy)
        )
    }
}