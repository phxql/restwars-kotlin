package restwars.rest.api

import restwars.business.planet.Location
import restwars.business.planet.Planet
import restwars.business.planet.PlanetWithPlayer
import restwars.business.planet.Resources

fun LocationResponse.Companion.fromLocation(location: Location) = LocationResponse(location.galaxy, location.system, location.planet)

fun ResourcesResponse.Companion.fromResources(resources: Resources) = ResourcesResponse(resources.crystal, resources.gas, resources.energy)

fun PlanetResponse.Companion.fromPlanet(planet: Planet) = PlanetResponse(
        LocationResponse.fromLocation(planet.location),
        ResourcesResponse.fromResources(planet.resources)
)

fun PlanetsResponse.Companion.fromPlanets(planets: List<Planet>) = PlanetsResponse(planets.map { PlanetResponse.fromPlanet(it) })

fun ScanResponse.Companion.from(planets: List<PlanetWithPlayer>): ScanResponse {
    return ScanResponse(planets.map { ScannedPlanetResponse(LocationResponse.fromLocation(it.planet.location), it.player.username) })
}
