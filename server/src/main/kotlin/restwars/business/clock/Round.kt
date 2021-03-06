package restwars.business.clock

import java.util.concurrent.ConcurrentLinkedQueue

interface RoundListener {
    fun onNewRound(newRound: Long)
}

interface RoundService {
    fun initialize()

    fun currentRound(): Long

    fun increaseRound(): Long

    fun addRoundListener(listener: RoundListener)

    fun removeRoundListener(listener: RoundListener)

    fun blockUntilNextRound(): Long
}

interface RoundRepository {
    fun rowCount(): Int

    fun insert(round: Long)

    fun update(round: Long)

    fun readRound(): Long
}

class RoundServiceImpl(private val roundRepository: RoundRepository) : RoundService {
    private val listeners = ConcurrentLinkedQueue<RoundListener>()
    private val nextRoundMonitor = Object()

    override fun initialize() {
        if (roundRepository.rowCount() == 0) {
            roundRepository.insert(0)
        }
    }

    override fun increaseRound(): Long {
        val newRound = roundRepository.readRound() + 1
        roundRepository.update(newRound)
        notifyListeners(newRound)
        // Notify monitor for the blockUntilNextRound() method
        synchronized(nextRoundMonitor) {
            nextRoundMonitor.notifyAll()
        }

        return newRound
    }

    private fun notifyListeners(newRound: Long) {
        for (listener in listeners) {
            listener.onNewRound(newRound)
        }
    }

    override fun currentRound(): Long {
        return roundRepository.readRound()
    }

    override fun addRoundListener(listener: RoundListener) {
        listeners.add(listener)
    }

    override fun removeRoundListener(listener: RoundListener) {
        listeners.remove(listener)
    }

    override fun blockUntilNextRound(): Long {
        synchronized(nextRoundMonitor) {
            nextRoundMonitor.wait()
        }
        return currentRound()
    }
}
