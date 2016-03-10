package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.player.Player
import restwars.business.player.PlayerRepository
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

object InMemoryPlayerRepository : PlayerRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var players: MutableList<Player> = CopyOnWriteArrayList()

    override fun insert(player: Player) {
        logger.info("Inserting player {}", player)
        players.add(player)
    }

    override fun findById(id: UUID): Player? {
        return players.firstOrNull { it.id == id }
    }

    override fun findByUsername(username: String): Player? {
        return players.firstOrNull { it.username == username }
    }

    override fun findAll(): List<Player> {
        return players
    }

    override fun persist(persister: Persister, path: Path) {
        persister.saveData(path, players)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(persister: Persister, path: Path) {
        players = persister.loadData(path) as MutableList<Player>
    }
}