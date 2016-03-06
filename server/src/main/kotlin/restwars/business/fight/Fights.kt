package restwars.business.fight

import org.slf4j.LoggerFactory
import restwars.business.RandomNumberGenerator
import restwars.business.ShipFormulas
import restwars.business.UUIDFactory
import restwars.business.clock.RoundService
import restwars.business.planet.Planet
import restwars.business.planet.Resources
import restwars.business.player.Player
import restwars.business.ship.ShipType
import restwars.business.ship.Ships
import java.io.Serializable
import java.util.*

data class Fight(val id: UUID, val attackerId: UUID, val defenderId: UUID, val planetId: UUID,
                 val attackerShips: Ships, val defenderShips: Ships, val remainingAttackerShips: Ships,
                 val remainingDefenderShips: Ships, val round: Long, val loot: Resources
) : Serializable

interface FightCalculator {
    fun attack(attackerId: UUID, defenderId: UUID, planetId: UUID, attackerShips: Ships, defenderShips: Ships, round: Long): Fight
}

data class FightWithPlayersAndPlanet(val fight: Fight, val attacker: Player, val defender: Player, val planet: Planet)

interface FightService {
    fun attack(attackerId: UUID, defenderId: UUID, planetId: UUID, attackerShips: Ships, defenderShips: Ships): Fight

    fun updateLoot(fight: Fight, loot: Resources): Fight

    fun findWithPlayer(player: Player, since: Long?): List<FightWithPlayersAndPlanet>

    fun findWithPlayerAndPlanet(player: Player, planet: Planet, since: Long?): List<FightWithPlayersAndPlanet>
}

interface FightRepository {
    fun insert(fight: Fight)

    fun updateLoot(fightId: UUID, loot: Resources)

    fun findWithPlayer(playerId: UUID): List<FightWithPlayersAndPlanet>

    fun findWithPlayerSince(playerId: UUID, since: Long): List<FightWithPlayersAndPlanet>

    fun findWithPlayerAndPlanet(playerId: UUID, planetId: UUID): List<FightWithPlayersAndPlanet>

    fun findWithPlayerAndPlanetSince(playerId: UUID, planetId: UUID, since: Long): List<FightWithPlayersAndPlanet>
}

class FightServiceImpl(
        private val fightCalculator: FightCalculator,
        private val roundService: RoundService,
        private val fightRepository: FightRepository
) : FightService {
    override fun attack(attackerId: UUID, defenderId: UUID, planetId: UUID, attackerShips: Ships, defenderShips: Ships): Fight {
        val round = roundService.currentRound()
        val fight = fightCalculator.attack(attackerId, defenderId, planetId, attackerShips, defenderShips, round)

        fightRepository.insert(fight)

        return fight
    }

    override fun updateLoot(fight: Fight, loot: Resources): Fight {
        fightRepository.updateLoot(fight.id, loot)

        return fight.copy(loot = loot)
    }

    override fun findWithPlayer(player: Player, since: Long?): List<FightWithPlayersAndPlanet> {
        return if (since == null) {
            fightRepository.findWithPlayer(player.id)
        } else {
            val round = if (since < 0) roundService.currentRound() + since else since
            fightRepository.findWithPlayerSince(player.id, round)
        }
    }

    override fun findWithPlayerAndPlanet(player: Player, planet: Planet, since: Long?): List<FightWithPlayersAndPlanet> {
        return if (since == null) {
            fightRepository.findWithPlayerAndPlanet(player.id, planet.id)
        } else {
            val round = if (since < 0) roundService.currentRound() + since else since
            fightRepository.findWithPlayerAndPlanetSince(player.id, planet.id, round)
        }
    }
}

class FightCalculatorImpl(
        private val uuidFactory: UUIDFactory,
        private val shipFormulas: ShipFormulas,
        private val randomNumberGenerator: RandomNumberGenerator
) : FightCalculator {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun attack(attackerId: UUID, defenderId: UUID, planetId: UUID, attackerShips: Ships, defenderShips: Ships, round: Long): Fight {
        logger.debug("Fight between {} and {}", attackerShips, defenderShips)

        val remainingDefenderShips = fight(attackerShips, defenderShips)
        val remainingAttackerShips = fight(defenderShips, attackerShips)

        return Fight(uuidFactory.create(), attackerId, defenderId, planetId, attackerShips, defenderShips, remainingAttackerShips, remainingDefenderShips, round, Resources.none())
    }

    private fun fight(attackerShips: Ships, defenderShips: Ships): Ships {
        var attackerAttackPoints = attackerShips.ships.sumBy { shipFormulas.calculateAttackPoints(it.type) * it.amount }
        logger.debug("Attacker attack points: $attackerAttackPoints")

        var remainingDefendingShips = defenderShips
        while (attackerAttackPoints > 0) {
            val destroyableShips = findDestroyableShips(remainingDefendingShips, attackerAttackPoints);
            if (destroyableShips.isEmpty()) {
                break;
            }

            val shipToDestroy = randomNumberGenerator.nextElement(destroyableShips)
            logger.trace("Destroying $shipToDestroy")
            remainingDefendingShips -= Ships.of(shipToDestroy, 1)

            attackerAttackPoints -= shipFormulas.calculateDefendPoints(shipToDestroy)
            logger.trace("Attacker attack points: $attackerAttackPoints")
        }

        logger.debug("Remaining defending ships: $remainingDefendingShips")
        return remainingDefendingShips
    }

    /**
     * Returns a list of ship types which can be destroyed with the given [attackPoints].
     */
    private fun findDestroyableShips(ships: Ships, attackPoints: Int): List<ShipType> {
        return ships.ships.filter { it.amount > 0 && shipFormulas.calculateDefendPoints(it.type) <= attackPoints }.map { it.type }
    }
}