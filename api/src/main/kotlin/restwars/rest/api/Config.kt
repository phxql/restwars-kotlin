package restwars.rest.api

data class UniverseSizeResponse(val minGalaxy: Int, val maxGalaxy: Int, val minSystem: Int, val maxSystem: Int, val minPlanet: Int, val maxPlanet: Int)

data class ConfigResponse(val roundTime: Int, val universeSize: UniverseSizeResponse) : Result {
    companion object {}
}