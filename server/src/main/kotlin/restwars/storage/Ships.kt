package restwars.storage

import org.jooq.DSLContext
import org.jooq.Record
import restwars.business.ship.*
import restwars.storage.jooq.Tables.*
import restwars.storage.jooq.tables.records.ShipsInConstructionRecord
import java.util.*

class JooqShipInConstructionRepository(private val jooq: DSLContext) : ShipInConstructionRepository {
    override fun insert(shipInConstruction: ShipInConstruction) {
        jooq.insertInto(SHIPS_IN_CONSTRUCTION, SHIPS_IN_CONSTRUCTION.ID, SHIPS_IN_CONSTRUCTION.PLANET_ID, SHIPS_IN_CONSTRUCTION.SHIP_TYPE, SHIPS_IN_CONSTRUCTION.DONE)
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
                record.id, record.planetId, ShipType.valueOf(record.shipType), record.done
        )
    }
}

class JooqHangarRepository(private val jooq: DSLContext) : HangarRepository {
    override fun findByPlanetId(planetId: UUID): Hangar {
        val records = jooq.selectFrom(HANGAR.leftJoin(HANGAR_SHIPS).on(HANGAR_SHIPS.HANGAR_ID.eq(HANGAR.ID)))
                .where(HANGAR.PLANET_ID.eq(planetId))
                .fetch().toList()

        if (records.isEmpty()) throw AssertionError("Planet $planetId has no hangar")

        return JooqHangarMapper.toRecord(records)
    }

    override fun insert(hangar: Hangar) {
        jooq.insertInto(HANGAR, HANGAR.ID, HANGAR.PLANET_ID)
                .values(hangar.id, hangar.planetId)
                .execute()

        for (ship in hangar.ships.ships) {
            if (ship.amount > 0) {
                jooq.insertInto(HANGAR_SHIPS, HANGAR_SHIPS.HANGAR_ID, HANGAR_SHIPS.SHIP_TYPE, HANGAR_SHIPS.AMOUNT)
                        .values(hangar.id, ship.type.name, ship.amount)
                        .execute()
            }
        }
    }

    override fun updateShips(hangarId: UUID, type: ShipType, oldAmount: Int, newAmount: Int) {
        if (oldAmount == newAmount) return

        if (newAmount == 0) {
            // Delete row
            jooq.deleteFrom(HANGAR_SHIPS)
                    .where(HANGAR_SHIPS.HANGAR_ID.eq(hangarId).and(HANGAR_SHIPS.SHIP_TYPE.eq(type.name)))
                    .execute()
        } else if (oldAmount == 0) {
            // Insert new row
            jooq.insertInto(HANGAR_SHIPS, HANGAR_SHIPS.HANGAR_ID, HANGAR_SHIPS.SHIP_TYPE, HANGAR_SHIPS.AMOUNT)
                    .values(hangarId, type.name, newAmount)
                    .execute()
        } else {
            // Update row
            jooq.update(HANGAR_SHIPS)
                    .set(HANGAR_SHIPS.AMOUNT, newAmount)
                    .where(HANGAR_SHIPS.HANGAR_ID.eq(hangarId).and(HANGAR_SHIPS.SHIP_TYPE.eq(type.name)))
                    .execute()
        }
    }
}

object JooqHangarMapper {
    fun toRecord(records: List<Record>): Hangar {
        val hangarRecords = records.map { it.into(HANGAR) }
        val hangarShipRecords = records.map { it.into(HANGAR_SHIPS) }
        val id = hangarRecords[0].id
        val planetId = hangarRecords[0].planetId

        val ships = if (hangarShipRecords[0].shipType == null) {
            // Happens if the LEFT JOIN has no rows in hangar_ships
            Ships.none()
        } else {
            Ships(hangarShipRecords.map { Ship(ShipType.valueOf(it.shipType), it.amount) })
        }

        return Hangar(id, planetId, ships)
    }
}