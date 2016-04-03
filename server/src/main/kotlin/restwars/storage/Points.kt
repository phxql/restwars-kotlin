package restwars.storage

import org.jooq.DSLContext
import org.jooq.Record
import restwars.business.point.Points
import restwars.business.point.PointsRepository
import restwars.business.point.PointsWithPlayer
import restwars.storage.jooq.Tables.PLAYERS
import restwars.storage.jooq.Tables.POINTS
import restwars.storage.jooq.tables.records.PointsRecord

class JooqPointsRepository(private val jooq: DSLContext) : PointsRepository {
    override fun insert(points: Points) {
        jooq.insertInto(POINTS, POINTS.ID, POINTS.PLAYER_ID, POINTS.ROUND, POINTS.POINTS_)
                .values(points.id, points.playerId, points.round, points.points)
                .execute()
    }

    override fun listMostRecentPoints(): List<PointsWithPlayer> {
        return jooq
                .selectFrom(POINTS.join(PLAYERS).on(PLAYERS.ID.eq(POINTS.PLAYER_ID)))
                .where(POINTS.ROUND.eq(
                        jooq.select(POINTS.ROUND.max()).from(POINTS)
                ))
                .fetch()
                .map { PointsWithPlayer(JooqPlayerMapper.fromRecord(it), JooqPointsMapper.fromRecord(it)) }
                .toList()
    }
}

object JooqPointsMapper {
    fun fromRecord(record: Record): Points = fromRecord(record.into(POINTS))

    fun fromRecord(record: PointsRecord): Points {
        return Points(record.id, record.playerId, record.points, record.round)
    }
}