package restwars.business.admin

import restwars.business.config.Config

data class Admin(val username: String, val password: String)

interface AdminService {
    fun login(username: String, password: String): Boolean
}

class AdminServiceImpl(private val config: Config) : AdminService {
    override fun login(username: String, password: String): Boolean {
        // TODO: Mitigate timing attacks
        return username == config.admin.username && password == config.admin.password
    }
}