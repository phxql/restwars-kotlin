package restwars.business.clock

import java.util.concurrent.ConcurrentLinkedQueue

interface RoundListener {
    fun onNewRound(newRound: Long)
}

interface RoundService {
    fun currentRound(): Long

    fun increaseRound(): Long

    fun addRoundListener(listener: RoundListener)

    fun removeRoundListener(listener: RoundListener)
}

interface RoundRepository {
    fun insert(round: Long)

    fun update(round: Long)

    fun readRound(): Long
}

class RoundServiceImpl(val roundRepository: RoundRepository) : RoundService {
    val listeners = ConcurrentLinkedQueue<RoundListener>()

    override fun increaseRound(): Long {
        val newRound = roundRepository.readRound() + 1
        roundRepository.update(newRound)
        notifyListeners(newRound)

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
}
