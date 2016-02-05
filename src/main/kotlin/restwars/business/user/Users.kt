package restwars.business.user

import restwars.business.UUIDFactory
import java.util.*

data class User(
        val id: UUID,
        val username: String,
        val password: String
)

interface UserService {
    fun create(username: String, password: String): User
}

interface UserRepository {
    fun insert(user: User)
}

class UserServiceImpl(val uuidFactory: UUIDFactory, val userRepository: UserRepository) : UserService {
    override fun create(username: String, password: String): User {
        val id = uuidFactory.create()

        val user = User(id, username, password) // TODO: Hash password
        userRepository.insert(user)
        return user
    }
}