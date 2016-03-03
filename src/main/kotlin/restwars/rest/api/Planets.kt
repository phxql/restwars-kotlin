package restwars.rest.api

import restwars.business.planet.Location
import restwars.business.planet.Planet
import restwars.business.planet.PlanetWithPlayer
import restwars.business.planet.Resources
import restwars.rest.base.Result

data class LocationResponse(val galaxy: Int, val system: Int, val planet: Int) : Result {
    companion object {
        fun fromLocation(location: Location) = LocationResponse(location.galaxy, location.system, location.planet)
    }
}

data class ResourcesResponse(val crystal: Int, val gas: Int, val energy: Int) : Result {
    companion object {
        fun fromResources(resources: Resources) = ResourcesResponse(resources.crystal, resources.gas, resources.energy)
    }
}

data class PlanetResponse(val location: LocationResponse, val resources: ResourcesResponse) : Result {
    companion object {
        fun fromPlanet(planet: Planet) = PlanetResponse(
                LocationResponse.fromLocation(planet.location),
                ResourcesResponse.fromResources(planet.resources)
        )
    }
}

data class PlanetsResponse(val planets: List<PlanetResponse>) : Result {
    companion object {
        fun fromPlanets(planets: List<Planet>) = PlanetsResponse(planets.map { PlanetResponse.fromPlanet(it) })
    }
}

data class ScannedPlanetResponse(val location: LocationResponse, val owner: String) : Result

data class ScanResponse(val planets: List<ScannedPlanetResponse>) : Result {
    companion object {
        fun from(planets: List<PlanetWithPlayer>): ScanResponse {
            return ScanResponse(planets.map { ScannedPlanetResponse(LocationResponse.fromLocation(it.planet.location), it.player.username) })
        }
    }
}
