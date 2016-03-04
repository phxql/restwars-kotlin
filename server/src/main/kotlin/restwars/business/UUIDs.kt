package restwars.business

import java.util.*

interface UUIDFactory {
    fun create(): UUID
}

object UUIDFactoryImpl : UUIDFactory {
    override fun create(): UUID = UUID.randomUUID()
}