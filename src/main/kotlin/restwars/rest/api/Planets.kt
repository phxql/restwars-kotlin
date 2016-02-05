package restwars.rest.api

import restwars.business.planet.Location
import restwars.business.planet.Planet

data class LocationResponse(val galaxy: Int, val system: Int, val planet: Int) {
    companion object {
        fun fromLocation(location: Location) = LocationResponse(location.galaxy, location.system, location.planet)
    }
}

data class PlanetResponse(val location: LocationResponse) {
    companion object {
        fun fromPlanet(planet: Planet) = PlanetResponse(LocationResponse.fromLocation(planet.location))
    }
}

data class PlanetsResponse(val planets: List<PlanetResponse>) {
    companion object {
        fun fromPlanets(planets: List<Planet>) = PlanetsResponse(planets.map { PlanetResponse.fromPlanet(it) })
    }
}
