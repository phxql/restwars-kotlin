package restwars.rest.api

import restwars.business.config.GameConfig

fun ConfigResponse.Companion.fromConfig(gameConfig: GameConfig) = ConfigResponse(
        gameConfig.roundTime, UniverseSizeResponse(
        1, gameConfig.universeSize.maxGalaxies,
        1, gameConfig.universeSize.maxSystems,
        1, gameConfig.universeSize.maxPlanets
)
)
