package restwars.storage

import org.slf4j.LoggerFactory
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

interface PersistentRepository {
    fun persist(path: Path)

    fun load(path: Path)
}

object Persister {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val executor = Executors.newSingleThreadScheduledExecutor({ runnable -> Thread(runnable, "Persister") })
    private val persistInterval = 5L

    private val repositories = mapOf<PersistentRepository, Path>(
            InMemoryBuildingRepository to Paths.get("data/buildings.dat"),
            InMemoryConstructionSiteRepository to Paths.get("data/construction-sites.dat"),
            InMemoryPlanetRepository to Paths.get("data/planets.dat"),
            InMemoryPlayerRepository to Paths.get("data/players.dat"),
            InMemoryRoundRepository to Paths.get("data/round.dat"),
            InMemoryHangarRepository to Paths.get("data/hangars.dat"),
            InMemoryShipInConstructionRepository to Paths.get("data/ships-in-construction.dat")
    )

    fun start() {
        load()

        logger.debug("Scheduling persist task")
        executor.scheduleAtFixedRate({ persist() }, persistInterval, persistInterval, TimeUnit.SECONDS)
    }

    private fun load() {
        logger.debug("Loading repositories...")
        try {
            for ((repo, path) in repositories) {
                if (Files.exists(path)) {
                    repo.load(path)
                }
            }
            logger.debug("Done loading repositories...")
        } catch(e: Exception) {
            logger.error("Exception while loading repositories", e)
        }
    }

    fun persist() {
        logger.debug("Persisting repositories...")
        try {
            for ((repo, path) in repositories) {
                repo.persist(path)
            }
            logger.debug("Done persisting repositories")
        } catch(e: Exception) {
            logger.error("Exception while persisting repositories", e)
        }
    }

    fun saveData(path: Path, data: Any) {
        ObjectOutputStream(Files.newOutputStream(path)).use {
            it.writeObject(data)
        }
    }

    fun loadData(path: Path): Any {
        ObjectInputStream(Files.newInputStream(path)).use {
            return it.readObject()
        }
    }
}