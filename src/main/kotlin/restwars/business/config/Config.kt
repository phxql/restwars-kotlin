package restwars.business.config

data class UniverseSize(val maxGalaxies: Int, val maxSystems: Int, val maxPlanets: Int)

data class Config(val universeSize: UniverseSize)