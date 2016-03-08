package restwars.business.tournament

import org.slf4j.LoggerFactory
import restwars.business.clock.RoundListener
import restwars.business.clock.RoundService
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch

class TournamentNotStartedException : Exception("Tournament has not started yet")

interface TournamentStartListener {
    fun onStart()
}

interface TournamentService {
    fun hasStarted(): Boolean

    fun start()

    fun blockUntilStart()

    fun addStartListener(listener: TournamentStartListener)

    fun removeStartListener(listener: TournamentStartListener)
}

object TournamentServiceImpl : TournamentService {
    val logger = LoggerFactory.getLogger(javaClass)
    val listeners = ConcurrentLinkedQueue<TournamentStartListener>()

    private val latch = CountDownLatch(1)

    override fun hasStarted(): Boolean = latch.count == 0L

    override fun start() {
        if (hasStarted()) return

        logger.info("Starting tournament")
        latch.countDown()
        notifyListeners()
    }

    override fun blockUntilStart() {
        latch.await()
    }

    private fun notifyListeners() {
        for (listener in listeners) {
            listener.onStart()
        }
    }

    override fun addStartListener(listener: TournamentStartListener) {
        listeners.add(listener)
    }

    override fun removeStartListener(listener: TournamentStartListener) {
        listeners.remove(listener)
    }
}

object NoopTournamentService : TournamentService {
    override fun hasStarted(): Boolean = true
    override fun start() {
    }

    override fun blockUntilStart() {
    }

    override fun addStartListener(listener: TournamentStartListener) {
    }

    override fun removeStartListener(listener: TournamentStartListener) {
    }
}

class TournamentRoundListener(private val roundService: RoundService, private val tournamentService: TournamentService, val tournamentStartRound: Long) : RoundListener {
    override fun onNewRound(newRound: Long) {
        if (newRound >= tournamentStartRound) {
            // Statt the tournament and remove this listener from the round service
            tournamentService.start()
            roundService.removeRoundListener(this)
        }
    }
}