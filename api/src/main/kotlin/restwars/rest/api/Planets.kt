package restwars.rest.api

data class LocationResponse(val galaxy: Int, val system: Int, val planet: Int) : Result {
    companion object {}
}

data class ResourcesResponse(val crystal: Int, val gas: Int, val energy: Int) : Result {
    companion object {}
}

data class PlanetResponse(val location: LocationResponse, val resources: ResourcesResponse) : Result {
    companion object {}
}

data class PlanetsResponse(val planets: List<PlanetResponse>) : Result {
    companion object {}
}

data class ScannedPlanetResponse(val location: LocationResponse, val owner: String) : Result

data class ScanResponse(val planets: List<ScannedPlanetResponse>) : Result {
    companion object {}
}
