package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.fight.Fight
import restwars.business.fight.FightRepository
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList

object InMemoryFightRepository : FightRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var fights: MutableList<Fight> = CopyOnWriteArrayList()

    override fun insert(fight: Fight) {
        logger.debug("Inserting fight {}", fight)
        fights.add(fight)
    }

    override fun persist(path: Path) {
        Persister.saveData(path, fights)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(path: Path) {
        this.fights = Persister.loadData(path) as MutableList<Fight>
    }
}
