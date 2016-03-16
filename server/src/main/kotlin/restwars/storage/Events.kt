package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.event.Event
import restwars.business.event.EventRepository
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList

object InMemoryEventRepository : EventRepository, PersistentRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var events: MutableList<Event> = CopyOnWriteArrayList()

    override fun insert(event: Event) {
        logger.debug("Inserting {}", event)
        events.add(event)
    }

    override fun persist(persister: Persister, path: Path) {
        persister.saveData(path, events)
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(persister: Persister, path: Path) {
        events = persister.loadData(path) as MutableList<Event>
    }

}