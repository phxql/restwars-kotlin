package restwars.rest.controller

import restwars.business.config.GameConfig
import restwars.rest.api.ConfigResponse
import restwars.rest.api.fromConfig
import restwars.rest.base.HttpMethod
import restwars.rest.base.RestMethod
import restwars.rest.base.SimpleRestMethod

class ConfigurationController(val gameConfig: GameConfig) {
    fun get(): RestMethod<ConfigResponse> {
        return SimpleRestMethod(HttpMethod.GET, "/v1/configuration", ConfigResponse::class.java, { req, res ->
            ConfigResponse.fromConfig(gameConfig)
        })
    }
}