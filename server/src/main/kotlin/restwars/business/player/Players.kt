package restwars.business.player

import restwars.business.UUIDFactory
import java.io.Serializable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

data class Player(
        val id: UUID,
        val username: String,
        val password: String
) : Serializable

abstract class CreatePlayerException(message: String) : Exception(message)

class UsernameNotUniqueException(username: String) : CreatePlayerException("User with username '$username' already exists")

interface PlayerService {
    fun beforeReadRequest(player: Player)

    fun afterReadRequest(player: Player)

    fun beforeWriteRequest(player: Player)

    fun afterWriteRequest(player: Player)

    /**
     * Creates a new player.
     *
     * @throws UsernameNotUniqueException If the username is already taken.
     */
    fun create(username: String, password: String): Player

    fun login(username: String, password: String): Player?

    fun findAll(): List<Player>
}

interface PlayerRepository {
    fun insert(player: Player)

    fun findByUsername(username: String): Player?

    fun findById(id: UUID): Player?

    fun findAll(): List<Player>
}

class PlayerServiceImpl(
        private val uuidFactory: UUIDFactory,
        private val playerRepository: PlayerRepository
) : PlayerService {
    private val locks = ConcurrentHashMap<UUID, ReadWriteLock>()

    override fun login(username: String, password: String): Player? {
        val player = playerRepository.findByUsername(username) ?: return null

        return if (player.password == password) player else null // TODO: Verify hash & timing attack mitigation
    }

    override fun create(username: String, password: String): Player {
        val id = uuidFactory.create()

        if (playerRepository.findByUsername(username) != null) throw UsernameNotUniqueException(username)

        val player = Player(id, username, password) // TODO: Hash password
        playerRepository.insert(player)
        return player
    }

    override fun findAll(): List<Player> {
        return playerRepository.findAll()
    }

    override fun beforeReadRequest(player: Player) {
        val lock = getOrCreateLock(player)
        lock.readLock().lock()
    }

    override fun afterReadRequest(player: Player) {
        locks[player.id]?.readLock()?.unlock()
    }

    override fun beforeWriteRequest(player: Player) {
        val lock = getOrCreateLock(player)
        lock.writeLock().lock()
    }

    override fun afterWriteRequest(player: Player) {
        locks[player.id]?.writeLock()?.unlock()
    }

    private fun getOrCreateLock(player: Player): ReadWriteLock {
        return locks.computeIfAbsent(player.id) { ReentrantReadWriteLock() }
    }
}