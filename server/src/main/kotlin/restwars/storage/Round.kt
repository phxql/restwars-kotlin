package restwars.storage

import org.jooq.DSLContext
import restwars.business.clock.RoundRepository
import restwars.storage.jooq.Tables.ROUND

class JooqRoundRepository(private val jooq: DSLContext) : RoundRepository {
    override fun rowCount(): Int {
        return jooq.fetchCount(ROUND)
    }

    override fun insert(round: Long) {
        jooq.insertInto(ROUND, ROUND.ROUND_)
                .values(round)
                .execute()
    }

    override fun update(round: Long) {
        jooq.update(ROUND)
                .set(ROUND.ROUND_, round)
                .execute()
    }

    override fun readRound(): Long {
        val record = jooq.selectFrom(ROUND).fetchOne()
        return record.round
    }
}