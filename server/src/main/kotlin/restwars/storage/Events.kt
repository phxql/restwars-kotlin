package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.event.Event
import restwars.business.event.EventRepository
import restwars.business.event.EventWithPlanet
import restwars.business.planet.PlanetRepository
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class InMemoryEventRepository(
        private val planetRepository: PlanetRepository
) : EventRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var events: MutableList<Event> = CopyOnWriteArrayList()

    override fun insert(event: Event) {
        logger.debug("Inserting {}", event)
        events.add(event)
    }

    override fun persist(persister: Persister, path: Path) {
        persister.saveData(path, events)
    }

    override fun findWithPlayer(playerId: UUID): List<EventWithPlanet> {
        return events.filter { it.playerId == playerId }.map {
            val planet = planetRepository.findById(it.planetId) ?: throw AssertionError("Planet with id ${it.planetId} not found")
            EventWithPlanet(it, planet)
        }
    }

    override fun findWithPlayerSince(playerId: UUID, since: Long): List<EventWithPlanet> {
        return events.filter { it.playerId == playerId && it.round >= since }.map {
            val planet = planetRepository.findById(it.planetId) ?: throw AssertionError("Planet with id ${it.planetId} not found")
            EventWithPlanet(it, planet)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(persister: Persister, path: Path) {
        events = persister.loadData(path) as MutableList<Event>
    }

}