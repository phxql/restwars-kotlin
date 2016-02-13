package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.clock.RoundRepository
import java.util.concurrent.atomic.AtomicLong

object InMemoryRoundRepository : RoundRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val currentRound = AtomicLong(0)

    override fun update(round: Long) {
        logger.info("Updating round to {}", round)
        currentRound.set(round)
    }

    override fun readRound(): Long {
        return currentRound.get()
    }

    override fun insert(round: Long) {
        currentRound.set(round)
    }
}