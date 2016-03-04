package restwars.rest.api

import restwars.business.config.Config

fun ConfigResponse.Companion.fromConfig(config: Config) = ConfigResponse(config.roundTime)
