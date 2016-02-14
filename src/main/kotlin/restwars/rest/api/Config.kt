package restwars.rest.api

import restwars.business.config.Config

data class ConfigResponse(val roundTime: Int) {
    companion object {
        fun fromConfig(config: Config) = ConfigResponse(config.roundTime)
    }
}
