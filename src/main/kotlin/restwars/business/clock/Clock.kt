package restwars.business.clock

import org.slf4j.LoggerFactory
import restwars.business.LockService
import restwars.business.building.Building
import restwars.business.building.BuildingService
import restwars.business.planet.Planet
import restwars.business.planet.PlanetService
import restwars.business.planet.Resources
import restwars.business.resource.ResourceService
import java.util.concurrent.atomic.AtomicLong

interface Clock {
    fun tick()
}

class ClockImpl(
        val planetService: PlanetService,
        val resourceService: ResourceService,
        val buildingService: BuildingService,
        val lockService: LockService,
        val roundService: RoundService
) : Clock {
    private val logger = LoggerFactory.getLogger(ClockImpl::class.java)

    override fun tick() {
        lockService.beforeClock()
        try {
            logger.debug("Tick")

            roundService.increaseRound()

            buildingService.finishConstructionSites()
            for (planet in planetService.findAllInhabitated()) {
                var updatedPlanet = planet
                val buildings = buildingService.findByPlanet(updatedPlanet)
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