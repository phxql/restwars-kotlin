package restwars.business.tournament

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

interface TournamentService {
    fun hasStarted(): Boolean

    fun start()

    fun blockUntilStart()
}

object TournamentServiceImpl : TournamentService {
    private val started = AtomicBoolean(false)
    private val startedLock = ReentrantLock()

    init {
        startedLock.lock()
    }

    override fun hasStarted(): Boolean = started.get()

    override fun start() {
        started.set(true)
        startedLock.unlock()
    }

    override fun blockUntilStart() {
        startedLock.lock()
        startedLock.unlock()
    }
}

object NoopTournamentService : TournamentService {
    override fun hasStarted(): Boolean = true
    override fun start() {
    }

    override fun blockUntilStart() {
    }
}