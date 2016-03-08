package restwars.business.tournament

import org.slf4j.LoggerFactory
import restwars.business.clock.RoundListener
import restwars.business.clock.RoundService
import java.util.concurrent.CountDownLatch

class TournamentNotStartedException : Exception("Tournament has not started yet")

interface TournamentService {
    fun hasStarted(): Boolean

    fun start()

    fun blockUntilStart()
}

object TournamentServiceImpl : TournamentService {
    val logger = LoggerFactory.getLogger(javaClass)

    private val latch = CountDownLatch(1)

    override fun hasStarted(): Boolean = latch.count == 0L

    override fun start() {
        if (hasStarted()) return

        logger.info("Starting tournament")
        latch.countDown()
    }

    override fun blockUntilStart() {
        latch.await()
    }
}

object NoopTournamentService : TournamentService {
    override fun hasStarted(): Boolean = true
    override fun start() {
    }

    override fun blockUntilStart() {
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