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
    fun persist(persister: Persister, path: Path)

    fun load(persister: Persister, path: Path)
}

class Persister(
        roundRepository: PersistentRepository,
        hangarRepository: PersistentRepository,
        shipInConstructionRepository: PersistentRepository,
        flightRepository: PersistentRepository,
        fightRepository: PersistentRepository,
        pointsRepository: PersistentRepository,
        detectedFlightRepository: PersistentRepository,
        eventRepository: PersistentRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val executor = Executors.newSingleThreadScheduledExecutor({ runnable -> Thread(runnable, "Persister") })
    private val persistInterval = 5L

    private val repositories = mapOf<PersistentRepository, Path>(
            roundRepository to Paths.get("data/round.dat"),
            hangarRepository to Paths.get("data/hangars.dat"),
            shipInConstructionRepository to Paths.get("data/ships-in-construction.dat"),
            flightRepository to Paths.get("data/flights.dat"),
            fightRepository to Paths.get("data/fights.dat"),
            pointsRepository to Paths.get("data/points.dat"),
            detectedFlightRepository to Paths.get("data/detected-flights.dat"),
            eventRepository to Paths.get("data/events.dat")
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
                    repo.load(this, path)
                }
            }
            logger.debug("Done loading repositories")
        } catch(e: Exception) {
            logger.error("Exception while loading repositories", e)
        }
    }

    fun persist() {
        logger.debug("Persisting repositories...")
        try {
            for ((repo, path) in repositories) {
                if (!Files.exists(path.parent)) {
                    Files.createDirectories(path.parent)
                }

                repo.persist(this, path)
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