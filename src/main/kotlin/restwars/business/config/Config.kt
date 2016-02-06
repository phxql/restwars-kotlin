package restwars.business.config

import restwars.business.planet.Resources

data class UniverseSize(val maxGalaxies: Int, val maxSystems: Int, val maxPlanets: Int)

data class StarterPlanet(val resources: Resources)

data class Config(val universeSize: UniverseSize, val starterPlanet: StarterPlanet, val roundTime: Int)