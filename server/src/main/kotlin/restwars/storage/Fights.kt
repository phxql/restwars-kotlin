package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.fight.Fight
import restwars.business.fight.FightRepository
import restwars.business.fight.FightWithPlayersAndPlanet
import restwars.business.planet.PlanetRepository
import restwars.business.planet.Resources
import restwars.business.player.PlayerRepository
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

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

    override fun persist(persister: Persister, path: Path) {
        persister.saveData(path, fights)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(persister: Persister, path: Path) {
        this.fights = persister.loadData(path) as MutableList<Fight>
    }
}
