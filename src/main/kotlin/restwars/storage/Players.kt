package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.player.Player
import restwars.business.player.PlayerRepository

object InMemoryPlayerRepository : PlayerRepository {
    val logger = LoggerFactory.getLogger(InMemoryPlayerRepository::class.java)

    override fun insert(player: Player) {
        logger.info("Inserting player {}", player)
    }
}