package restwars.business

import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantReadWriteLock

interface LockService {
    fun beforeRequest()

    fun afterRequest()

    fun beforeClock()

    fun afterClock()
}

object LockServiceImpl : LockService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val lock = ReentrantReadWriteLock()

    override fun beforeRequest() {
        logger.trace("Locking read lock...")
        lock.readLock().lock()
        logger.trace("Locked read lock")
    }

    override fun afterRequest() {
        logger.trace("Unlocking read lock")
        lock.readLock().unlock()
    }

    override fun beforeClock() {
        logger.trace("Locking write lock...")
        lock.writeLock().lock()
        logger.trace("Locked write lock")
    }

    override fun afterClock() {
        logger.trace("Unlocking write lock")
        lock.writeLock().unlock()
    }
}