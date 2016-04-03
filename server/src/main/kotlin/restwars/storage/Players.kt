package restwars.storage

import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import restwars.business.player.Player
import restwars.business.player.PlayerRepository
import restwars.storage.jooq.Tables.PLAYERS
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class JooqPlayerRepository(val jooq: DSLContext) : PlayerRepository {
    override fun insert(player: Player) {
        jooq.insertInto(PLAYERS, PLAYERS.ID, PLAYERS.USERNAME, PLAYERS.PASSWORD)
                .values(player.id, player.username, player.password)
                .execute()
    }

    override fun findByUsername(username: String): Player? {
        val record = jooq.selectFrom(PLAYERS)
                .where(PLAYERS.USERNAME.eq(username))
                .fetchOne() ?: return null

        return Player(record.id, record.username, record.password)
    }

    override fun findById(id: UUID): Player? {
        val record = jooq.selectFrom(PLAYERS)
                .where(PLAYERS.ID.eq(id))
                .fetchOne() ?: return null

        return Player(record.id, record.username, record.password)
    }

    override fun findAll(): List<Player> {
        return jooq.selectFrom(PLAYERS).fetch().map {
            Player(it.id, it.username, it.password)
        }.toList()
    }
}

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