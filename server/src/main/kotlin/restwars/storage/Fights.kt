package restwars.storage

import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import restwars.business.fight.Fight
import restwars.business.fight.FightRepository
import restwars.business.fight.FightWithPlayersAndPlanet
import restwars.business.planet.PlanetRepository
import restwars.business.planet.Resources
import restwars.business.player.PlayerRepository
import restwars.business.ship.Ships
import restwars.storage.jooq.Tables.FIGHTS
import restwars.storage.jooq.Tables.FIGHT_SHIPS
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class JooqFightRepository(private val jooq: DSLContext) : FightRepository {
    private enum class FightShipType {
        ATTACKER, DEFENDER, REMAINING_ATTACKER, REMAINING_DEFENDER
    }

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
        throw UnsupportedOperationException()
    }

    override fun findWithPlayerSince(playerId: UUID, since: Long): List<FightWithPlayersAndPlanet> {
        throw UnsupportedOperationException()
    }

    override fun findWithPlayerAndPlanet(playerId: UUID, planetId: UUID): List<FightWithPlayersAndPlanet> {
        throw UnsupportedOperationException()
    }

    override fun findWithPlayerAndPlanetSince(playerId: UUID, planetId: UUID, since: Long): List<FightWithPlayersAndPlanet> {
        throw UnsupportedOperationException()
    }
}

class InMemoryFightRepository(
        private val playerRepository: PlayerRepository,
        private val planetRepository: PlanetRepository
) : FightRepository, PersistentRepository {
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

    override fun persist(persister: Persister, path: Path) {
        persister.saveData(path, fights)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(persister: Persister, path: Path) {
        this.fights = persister.loadData(path) as MutableList<Fight>
    }
}
