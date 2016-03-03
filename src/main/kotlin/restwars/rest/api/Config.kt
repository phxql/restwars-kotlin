package restwars.rest.api

import restwars.business.config.Config
import restwars.rest.base.Result

data class ConfigResponse(val roundTime: Int) : Result {
    companion object {
        fun fromConfig(config: Config) = ConfigResponse(config.roundTime)
    }
}
