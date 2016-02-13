package restwars.business.clock

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
    override fun increaseRound(): Long {
        val newRound = roundRepository.readRound() + 1
        roundRepository.update(newRound)
        return newRound
    }

    override fun currentRound(): Long {
        return roundRepository.readRound()
    }
}
