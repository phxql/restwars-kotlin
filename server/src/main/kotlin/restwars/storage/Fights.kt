package restwars.storage

import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectWhereStep
import org.slf4j.LoggerFactory
import restwars.business.fight.Fight
import restwars.business.fight.FightRepository
import restwars.business.fight.FightWithPlayersAndPlanet
import restwars.business.planet.PlanetRepository
import restwars.business.planet.Resources
import restwars.business.player.PlayerRepository
import restwars.business.ship.Ship
import restwars.business.ship.ShipType
import restwars.business.ship.Ships
import restwars.storage.jooq.Tables.*
import restwars.storage.jooq.tables.records.FightShipsRecord
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

private enum class FightShipType {
    ATTACKER, DEFENDER, REMAINING_ATTACKER, REMAINING_DEFENDER
}

class JooqFightRepository(private val jooq: DSLContext) : FightRepository {
    override fun insert(fight: Fight) {
        jooq.
                insertInto(FIGHTS, FIGHTS.ID, FIGHTS.ATTACKER_ID, FIGHTS.DEFENDER_ID, FIGHTS.PLANET_ID, FIGHTS.ROUND, FIGHTS.LOOT_CRYSTAL, FIGHTS.LOOT_GAS)
                .values(fight.id, fight.attackerId, fight.defenderId, fight.planetId, fight.round, fight.loot.crystal, fight.loot.gas)
                .execute()

        insertShips(fight.id, fight.attackerShips, FightShipType.ATTACKER)
        insertShips(fight.id, fight.defenderShips, FightShipType.DEFENDER)
        insertShips(fight.id, fight.remainingAttackerShips, FightShipType.REMAINING_ATTACKER)
        insertShips(fight.id, fight.remainingDefenderShips, FightShipType.REMAINING_DEFENDER)
    }

    private fun insertShips(fightId: UUID, ships: Ships, type: FightShipType) {
        for (ship in ships.ships) {
            if (ship.amount > 0) {
                jooq
                        .insertInto(FIGHT_SHIPS, FIGHT_SHIPS.FIGHT_ID, FIGHT_SHIPS.TYPE, FIGHT_SHIPS.SHIP_TYPE, FIGHT_SHIPS.AMOUNT)
                        .values(fightId, type.name, ship.type.name, ship.amount)
                        .execute()
            }
        }
    }

    override fun updateLoot(fightId: UUID, loot: Resources) {
        jooq
                .update(FIGHTS)
                .set(FIGHTS.LOOT_CRYSTAL, loot.crystal)
                .set(FIGHTS.LOOT_GAS, loot.gas)
                .where(FIGHTS.ID.eq(fightId))
                .execute()
    }

    override fun findWithPlayer(playerId: UUID): List<FightWithPlayersAndPlanet> {
        val records = buildSelect()
                .where(FIGHTS.ATTACKER_ID.eq(playerId).or(FIGHTS.DEFENDER_ID.eq(playerId)))
                .fetchGroups(FIGHTS.ID)

        return records.values.map { JooqFightMapper.fromRecords(it) }
    }

    private fun buildSelect(): SelectWhereStep<Record> {
        val attacker = PLAYERS.`as`("attacker")
        val defender = PLAYERS.`as`("defender")
        return jooq
                .selectFrom(
                        FIGHTS.leftJoin(FIGHT_SHIPS).on(FIGHT_SHIPS.FIGHT_ID.eq(FIGHTS.ID))
                                .join(attacker).on(attacker.ID.eq(FIGHTS.ATTACKER_ID))
                                .join(defender).on(defender.ID.eq(FIGHTS.DEFENDER_ID))
                                .join(PLANETS).on(PLANETS.ID.eq(FIGHTS.PLANET_ID))
                )
    }

    override fun findWithPlayerSince(playerId: UUID, since: Long): List<FightWithPlayersAndPlanet> {
        val records = buildSelect()
                .where(FIGHTS.ATTACKER_ID.eq(playerId).or(FIGHTS.DEFENDER_ID.eq(playerId)))
                .and(FIGHTS.ROUND.ge(since))
                .fetchGroups(FIGHTS.ID)

        return records.values.map { JooqFightMapper.fromRecords(it) }
    }

    override fun findWithPlayerAndPlanet(playerId: UUID, planetId: UUID): List<FightWithPlayersAndPlanet> {
        val records = buildSelect()
                .where(FIGHTS.ATTACKER_ID.eq(playerId).or(FIGHTS.DEFENDER_ID.eq(playerId)))
                .and(FIGHTS.PLANET_ID.eq(planetId))
                .fetchGroups(FIGHTS.ID)

        return records.values.map { JooqFightMapper.fromRecords(it) }
    }

    override fun findWithPlayerAndPlanetSince(playerId: UUID, planetId: UUID, since: Long): List<FightWithPlayersAndPlanet> {
        val records = buildSelect()
                .where(FIGHTS.ATTACKER_ID.eq(playerId).or(FIGHTS.DEFENDER_ID.eq(playerId)))
                .and(FIGHTS.PLANET_ID.eq(planetId))
                .and(FIGHTS.ROUND.ge(since))
                .fetchGroups(FIGHTS.ID)

        return records.values.map { JooqFightMapper.fromRecords(it) }
    }
}

object JooqFightMapper {
    private data class FightShips(val attacker: Ships, val defender: Ships, val remainingAttacker: Ships, val remainingDefender: Ships) {
        companion object {
            fun none() = FightShips(Ships.none(), Ships.none(), Ships.none(), Ships.none())
        }
    }

    fun fromRecords(records: List<Record>): FightWithPlayersAndPlanet {
        assert(records.isNotEmpty())

        val fightRecords = records.map { it.into(FIGHTS) }
        val planetRecords = records.map { it.into(PLANETS) }
        val attackerRecords = records.map { it.into(PLAYERS.`as`("attacker")) }
        val defenderRecords = records.map { it.into(PLAYERS.`as`("defender")) }
        val fightShipsRecords = records.map { it.into(FIGHT_SHIPS) }

        val fightRecord = fightRecords[0]
        val planetRecord = planetRecords[0]
        val attackerRecord = attackerRecords[0]
        val defenderRecord = defenderRecords[0]


        val ships = if (fightShipsRecords[0].type == null) {
            // Happens if the LEFT JOIN has no rows in fight_ships
            FightShips.none()
        } else {
            FightShips(
                    Ships(toShips(fightShipsRecords, FightShipType.ATTACKER)),
                    Ships(toShips(fightShipsRecords, FightShipType.DEFENDER)),
                    Ships(toShips(fightShipsRecords, FightShipType.REMAINING_ATTACKER)),
                    Ships(toShips(fightShipsRecords, FightShipType.REMAINING_DEFENDER))
            )
        }

        val fight = Fight(
                fightRecord.id, fightRecord.attackerId, fightRecord.defenderId, fightRecord.planetId, ships.attacker, ships.defender,
                ships.remainingAttacker, ships.remainingDefender, fightRecord.round,
                Resources(fightRecord.lootCrystal, fightRecord.lootGas, 0)
        )
        val planet = JooqPlanetMapper.fromRecord(planetRecord)
        val attacker = JooqPlayerMapper.fromRecord(attackerRecord)
        val defender = JooqPlayerMapper.fromRecord(defenderRecord)

        return FightWithPlayersAndPlanet(fight, attacker, defender, planet)
    }

    private fun toShips(fightShipsRecords: List<FightShipsRecord>, type: FightShipType) = fightShipsRecords.filter { FightShipType.valueOf(it.type) == type }.map { Ship(ShipType.valueOf(it.shipType), it.amount) }
}

class InMemoryFightRepository(
        private val playerRepository: PlayerRepository,
        private val planetRepository: PlanetRepository
) : FightRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var fights: MutableList<Fight> = CopyOnWriteArrayList()

    override fun insert(fight: Fight) {
        logger.debug("Inserting fight {}", fight)
        fights.add(fight)
    }

    override fun updateLoot(fightId: UUID, loot: Resources) {
        val index = fights.indexOfFirst { it.id == fightId }

        val fight = fights[index]
        fights[index] = fight.copy(loot = loot)
    }

    override fun findWithPlayer(playerId: UUID): List<FightWithPlayersAndPlanet> {
        return fights
                .filter { it.attackerId == playerId || it.defenderId == playerId }
                .map {
                    FightWithPlayersAndPlanet(
                            it, playerRepository.findById(it.attackerId)!!, playerRepository.findById(it.defenderId)!!,
                            planetRepository.findById(it.planetId)!!
                    )
                }
    }

    override fun findWithPlayerSince(playerId: UUID, since: Long): List<FightWithPlayersAndPlanet> {
        return fights
                .filter { (it.attackerId == playerId || it.defenderId == playerId) && it.round >= since }
                .map {
                    FightWithPlayersAndPlanet(
                            it, playerRepository.findById(it.attackerId)!!, playerRepository.findById(it.defenderId)!!,
                            planetRepository.findById(it.planetId)!!
                    )
                }
    }

    override fun findWithPlayerAndPlanet(playerId: UUID, planetId: UUID): List<FightWithPlayersAndPlanet> {
        return fights
                .filter { it.attackerId == playerId || it.defenderId == playerId || it.planetId == planetId }
                .map {
                    FightWithPlayersAndPlanet(
                            it, playerRepository.findById(it.attackerId)!!, playerRepository.findById(it.defenderId)!!,
                            planetRepository.findById(it.planetId)!!
                    )
                }
    }

    override fun findWithPlayerAndPlanetSince(playerId: UUID, planetId: UUID, since: Long): List<FightWithPlayersAndPlanet> {
        return fights
                .filter { (it.attackerId == playerId || it.defenderId == playerId) && it.planetId == planetId && it.round >= since }
                .map {
                    FightWithPlayersAndPlanet(
                            it, playerRepository.findById(it.attackerId)!!, playerRepository.findById(it.defenderId)!!,
                            planetRepository.findById(it.planetId)!!
                    )
                }
    }
}
