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
}

interface PlayerRepository {
    fun insert(player: Player)
}

class PlayerServiceImpl(val uuidFactory: UUIDFactory, val playerRepository: PlayerRepository) : PlayerService {
    override fun create(username: String, password: String): Player {
        val id = uuidFactory.create()

        val player = Player(id, username, password) // TODO: Hash password
        playerRepository.insert(player)
        return player
    }
}