package restwars.business

import java.util.concurrent.ThreadLocalRandom

interface RandomNumberGenerator {
    /**
     * Returns an [Int] in the range of ([from] .. [to]), both inclusive.
     */
    fun nextInt(from: Int, to: Int): Int

    fun <T> nextElement(list: List<T>): T
}

object RandomNumberGeneratorImpl : RandomNumberGenerator {
    override fun nextInt(from: Int, to: Int): Int = ThreadLocalRandom.current().nextInt(from, to + 1)

    override fun <T> nextElement(list: List<T>): T = list[nextInt(0, list.size - 1)]
}