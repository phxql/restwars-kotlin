package restwars.rest.api

import restwars.business.config.Config

fun ConfigResponse.Companion.fromConfig(config: Config) = ConfigResponse(
        config.roundTime, UniverseSizeResponse(
        1, config.universeSize.maxGalaxies,
        1, config.universeSize.maxSystems,
        1, config.universeSize.maxPlanets
)
)
