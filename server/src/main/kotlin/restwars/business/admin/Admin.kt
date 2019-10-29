package restwars.business.admin

import restwars.business.config.GameConfig

data class Admin(val username: String, val password: String)

interface AdminService {
    fun login(username: String, password: String): Boolean
}

class AdminServiceImpl(private val gameConfig: GameConfig) : AdminService {
    override fun login(username: String, password: String): Boolean {
        // TODO: Mitigate timing attacks
        return username == gameConfig.admin.username && password == gameConfig.admin.password
    }
}