package restwars.rest.controller

import restwars.business.config.Config
import spark.Route

data class ConfigResponse(val roundTime: Int) {
    companion object {
        fun fromConfig(config: Config) = ConfigResponse(config.roundTime)
    }
}

class ConfigurationController(val config: Config) {
    fun get(): Route {
        return Route { req, res ->
            return@Route Json.toJson(res, ConfigResponse.fromConfig(config))
        }
    }
}