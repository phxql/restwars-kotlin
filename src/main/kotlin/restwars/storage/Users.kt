package restwars.storage

import org.slf4j.LoggerFactory
import restwars.business.user.User
import restwars.business.user.UserRepository

object InMemoryUserRepository : UserRepository {
    val logger = LoggerFactory.getLogger(InMemoryUserRepository::class.java)

    override fun insert(user: User) {
        logger.info("Inserting user {}", user)
    }
}