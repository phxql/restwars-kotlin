package restwars.business.player

import restwars.business.UUIDFactory
import java.io.Serializable
import java.util.*

data class Player(
        val id: UUID,
        val username: String,
        val password: String
) : Serializable

abstract class CreatePlayerException(message: String) : Exception(message)

class UsernameNotUniqueException(username: String) : CreatePlayerException("User with username '$username' already exists")

interface PlayerService {
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
}