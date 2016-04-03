package restwars.storage

import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import restwars.business.ship.*
import restwars.storage.jooq.Tables.SHIPS_IN_CONSTRUCTION
import restwars.storage.jooq.tables.records.ShipsInConstructionRecord
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class JooqShipInConstructionRepository(private val jooq: DSLContext) : ShipInConstructionRepository {
    override fun insert(shipInConstruction: ShipInConstruction) {
        jooq.insertInto(SHIPS_IN_CONSTRUCTION, SHIPS_IN_CONSTRUCTION.ID, SHIPS_IN_CONSTRUCTION.PLANET_ID, SHIPS_IN_CONSTRUCTION.TYPE, SHIPS_IN_CONSTRUCTION.DONE)
                .values(shipInConstruction.id, shipInConstruction.planetId, shipInConstruction.type.name, shipInConstruction.done)
                .execute()
    }

    override fun delete(id: UUID) {
        jooq.deleteFrom(SHIPS_IN_CONSTRUCTION)
                .where(SHIPS_IN_CONSTRUCTION.ID.eq(id))
                .execute()
    }

    override fun findByPlanetId(planetId: UUID): List<ShipInConstruction> {
        return jooq.selectFrom(SHIPS_IN_CONSTRUCTION)
                .where(SHIPS_IN_CONSTRUCTION.PLANET_ID.eq(planetId))
                .fetch()
                .map { JooqShipInConstructionMapper.fromRecord(it) }
                .toList()
    }

    override fun countByPlanetId(planetId: UUID): Int {
        return jooq.fetchCount(jooq.selectFrom(SHIPS_IN_CONSTRUCTION)
                .where(SHIPS_IN_CONSTRUCTION.PLANET_ID.eq(planetId))
        )
    }

    override fun findByDone(done: Long): List<ShipInConstruction> {
        return jooq.selectFrom(SHIPS_IN_CONSTRUCTION)
                .where(SHIPS_IN_CONSTRUCTION.DONE.eq(done))
                .fetch()
                .map { JooqShipInConstructionMapper.fromRecord(it) }
                .toList()
    }
}

object JooqShipInConstructionMapper {
    fun fromRecord(record: ShipsInConstructionRecord): ShipInConstruction {
        return ShipInConstruction(
                record.id, record.planetId, ShipType.valueOf(record.type), record.done
        )
    }
}

object InMemoryHangarRepository : HangarRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var hangars: MutableList<Hangar> = CopyOnWriteArrayList()

    override fun findByPlanetId(planetId: UUID): Hangar? {
        return hangars.find { it.planetId == planetId }
    }

    override fun insert(hangar: Hangar) {
        logger.debug("Inserting {}", hangar)
        hangars.add(hangar)
    }

    override fun updateShips(hangarId: UUID, type: ShipType, newAmount: Int) {
        logger.debug("Updating ships in hangar with id {}: type {}, new amount: {}", hangarId, type, newAmount)

        val index = hangars.indexOfFirst { it.id == hangarId }
        if (index == -1) throw IllegalStateException("No hangar with id $hangarId found")

        val hangar = hangars[index]
        hangars[index] = hangar.copy(ships = hangar.ships.with(type, newAmount))
    }

    override fun persist(persister: Persister, path: Path) {
        persister.saveData(path, hangars)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(persister: Persister, path: Path) {
        hangars = persister.loadData(path) as MutableList<Hangar>
    }
}