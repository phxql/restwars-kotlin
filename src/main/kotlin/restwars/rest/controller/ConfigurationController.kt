package restwars.rest.controller

import restwars.business.config.Config
import restwars.rest.api.ConfigResponse
import spark.Route

class ConfigurationController(val config: Config) {
    fun get(): Route {
        return Route { req, res ->
            return@Route Json.toJson(res, ConfigResponse.fromConfig(config))
        }
    }
}