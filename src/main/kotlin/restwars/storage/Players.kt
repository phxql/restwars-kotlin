package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.player.Player
import restwars.business.player.PlayerRepository
import java.util.concurrent.CopyOnWriteArrayList

object InMemoryPlayerRepository : PlayerRepository {
    private val logger = LoggerFactory.getLogger(InMemoryPlayerRepository::class.java)
    private val players: MutableList<Player> = CopyOnWriteArrayList()

    override fun insert(player: Player) {
        logger.info("Inserting player {}", player)
        players.add(player)
    }
}