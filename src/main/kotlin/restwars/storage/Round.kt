package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.clock.RoundRepository
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicLong

object InMemoryRoundRepository : RoundRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var currentRound = AtomicLong(0)

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

    override fun persist(persister: Persister, path: Path) {
        persister.saveData(path, currentRound.get())
    }

    override fun load(persister: Persister, path: Path) {
        val round = persister.loadData(path) as Long
        currentRound.set(round)
    }
}