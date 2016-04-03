package restwars.business.clock

import org.slf4j.LoggerFactory
import restwars.business.LockService
import restwars.business.building.Building
import restwars.business.building.BuildingService
import restwars.business.config.Config
import restwars.business.flight.FlightService
import restwars.business.planet.Planet
import restwars.business.planet.PlanetService
import restwars.business.planet.Resources
import restwars.business.point.PointsService
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
        private val flightService: FlightService,
        private val pointsService: PointsService,
        private val config: Config
) : Clock {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun tick() {
        try {
            _tick()
        } catch(e: Exception) {
            logger.error("Clock crashed!", e)
        }
    }

    private fun _tick() {
        lockService.beforeClock()
        try {
            logger.debug("Tick")

            val newRound = roundService.increaseRound()
            buildingService.finishConstructionSites()
            shipService.finishShipsInConstruction()
            flightService.detectFlights()
            flightService.finishFlights()

            for (planet in planetService.findAllInhabited()) {
                var updatedPlanet = planet
                val buildings = buildingService.findBuildingsByPlanet(updatedPlanet)
                gatherResources(buildings, updatedPlanet)
            }

            if (newRound % config.calculatePointsEvery == 0L) {
                pointsService.calculatePoints()
            }

            logger.info("Now in round $newRound")
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