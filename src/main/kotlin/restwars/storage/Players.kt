package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.player.Player
import restwars.business.player.PlayerRepository
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList

object InMemoryPlayerRepository : PlayerRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var players: MutableList<Player> = CopyOnWriteArrayList()

    override fun insert(player: Player) {
        logger.info("Inserting player {}", player)
        players.add(player)
    }

    override fun findByUsername(username: String): Player? {
        return players.firstOrNull { it.username == username }
    }

    override fun persist(path: Path) {
        Persister.saveData(path, players)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(path: Path) {
        players = Persister.loadData(path) as MutableList<Player>
    }
}