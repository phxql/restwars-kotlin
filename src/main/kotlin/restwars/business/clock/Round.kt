package restwars.business.clock

import java.util.concurrent.atomic.AtomicLong

interface RoundService {
    fun currentRound(): Long

    fun increaseRound(): Long
}

interface RoundRepository {
    fun insert(round: Long)

    fun update(round: Long)

    fun readRound(): Long
}

class RoundServiceImpl(val roundRepository: RoundRepository) : RoundService {
    private val currentRound = AtomicLong(0)

    init {
        currentRound.set(roundRepository.readRound())
    }

    override fun increaseRound(): Long {
        val newRound = currentRound.incrementAndGet()
        roundRepository.update(newRound)
        return newRound
    }

    override fun currentRound(): Long {
        return currentRound.get()
    }
}
