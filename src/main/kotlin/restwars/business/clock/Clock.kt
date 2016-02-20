package restwars.business.clock

import org.slf4j.LoggerFactory
import restwars.business.LockService
import restwars.business.building.Building
import restwars.business.building.BuildingService
import restwars.business.flight.FlightService
import restwars.business.planet.Planet
import restwars.business.planet.PlanetService
import restwars.business.planet.Resources
import restwars.business.resource.ResourceService
import restwars.business.ship.ShipService

interface Clock {
    fun tick()
}

class ClockImpl(
        private val planetService: PlanetService,
        private val resourceService: ResourceService,
        private val buildingService: BuildingService,
        private val lockService: LockService,
        private val roundService: RoundService,
        private val shipService: ShipService,
        private val flightService: FlightService
) : Clock {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun tick() {
        lockService.beforeClock()
        try {
            logger.debug("Tick")

            roundService.increaseRound()
            buildingService.finishConstructionSites()
            shipService.finishShipsInConstruction()
            flightService.finishFlights()

            for (planet in planetService.findAllInhabitated()) {
                var updatedPlanet = planet
                val buildings = buildingService.findBuildingsByPlanet(updatedPlanet)
                updatedPlanet = gatherResources(buildings, updatedPlanet)
            }
        } finally {
            lockService.afterClock()
        }
    }

    private fun gatherResources(buildings: List<Building>, planet: Planet): Planet {
        logger.debug("Gathering resources on {}", planet.location)

        val gatheredResources = calculateGatheredResources(buildings)
        return planetService.addResources(planet, gatheredResources)
    }

    private fun calculateGatheredResources(buildings: List<Building>): Resources {
        var gatheredTotal = Resources.none()

        for (building in buildings) {
            val gatheredFromBuilding = resourceService.calculateGatheredResources(building.type, building.level)
            gatheredTotal += gatheredFromBuilding
        }

        return gatheredTotal
    }
}