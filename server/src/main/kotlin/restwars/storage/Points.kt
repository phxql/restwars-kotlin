package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.player.PlayerRepository
import restwars.business.point.Points
import restwars.business.point.PointsRepository
import restwars.business.point.PointsWithPlayer
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList

class InMemoryPointsRepository(
        private val playerRepository: PlayerRepository
) : PointsRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(InMemoryPointsRepository::class.java)
    private var points: MutableList<Points> = CopyOnWriteArrayList()

    override fun insert(points: Points) {
        logger.debug("Inserting {}", points)
        this.points.add(points)
    }

    override fun listMostRecentPoints(): List<PointsWithPlayer> {
        return points.groupBy { it.playerId }.map {
            val player = playerRepository.findById(it.key)
            val points = it.value.sortedByDescending { it.round }.firstOrNull()

            if (points == null || player == null) null else PointsWithPlayer(player, points)
        }.filterNotNull()
    }

    override fun persist(persister: Persister, path: Path) {
        persister.saveData(path, points)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(persister: Persister, path: Path) {
        points = persister.loadData(path) as MutableList<Points>
    }
}