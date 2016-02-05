package restwars.business

import java.util.concurrent.ThreadLocalRandom

interface RandomNumberGenerator {
    fun nextInt(from: Int, to: Int): Int
}

object RandomNumberGeneratorImpl : RandomNumberGenerator {
    override fun nextInt(from: Int, to: Int): Int = ThreadLocalRandom.current().nextInt(from, to + 1)
}