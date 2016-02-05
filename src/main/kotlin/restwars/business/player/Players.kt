package restwars.business.player

import restwars.business.UUIDFactory
import java.util.*

data class Player(
        val id: UUID,
        val username: String,
        val password: String
)

interface PlayerService {
    fun create(username: String, password: String): Player

    fun login(username: String, password: String): Player?
}

interface PlayerRepository {
    fun insert(player: Player)

    fun findByUsername(username: String): Player?
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

        val player = Player(id, username, password) // TODO: Hash password
        playerRepository.insert(player)
        return player
    }
}