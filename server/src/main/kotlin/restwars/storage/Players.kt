package restwars.storage

import org.jooq.DSLContext
import org.jooq.Record
import restwars.business.player.Player
import restwars.business.player.PlayerRepository
import restwars.storage.jooq.Tables.PLAYERS
import restwars.storage.jooq.tables.records.PlayersRecord
import java.util.*

class JooqPlayerRepository(private val jooq: DSLContext) : PlayerRepository {
    override fun insert(player: Player) {
        jooq.insertInto(PLAYERS, PLAYERS.ID, PLAYERS.USERNAME, PLAYERS.PASSWORD)
                .values(player.id, player.username, player.password)
                .execute()
    }

    override fun findByUsername(username: String): Player? {
        val record = jooq.selectFrom(PLAYERS)
                .where(PLAYERS.USERNAME.eq(username))
                .fetchOne() ?: return null

        return JooqPlayerMapper.fromRecord(record)
    }

    override fun findById(id: UUID): Player? {
        val record = jooq.selectFrom(PLAYERS)
                .where(PLAYERS.ID.eq(id))
                .fetchOne() ?: return null

        return JooqPlayerMapper.fromRecord(record)
    }

    override fun findAll(): List<Player> {
        return jooq.selectFrom(PLAYERS).fetch().map {
            JooqPlayerMapper.fromRecord(it)
        }.toList()
    }
}

object JooqPlayerMapper {
    fun fromRecord(record: Record): Player = fromRecord(record.into(PlayersRecord::class.java))

    fun fromRecord(record: PlayersRecord): Player {
        return Player(record.id, record.username, record.password)
    }
}