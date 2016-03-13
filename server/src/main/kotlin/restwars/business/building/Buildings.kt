package restwars.business.building

import org.slf4j.LoggerFactory
import restwars.business.BuildingFormulas
import restwars.business.UUIDFactory
import restwars.business.clock.RoundService
import restwars.business.planet.Planet
import restwars.business.planet.PlanetRepository
import restwars.business.resource.NotEnoughResourcesException
import restwars.util.ceil
import java.io.Serializable
import java.util.*

enum class BuildingType {
    COMMAND_CENTER,
    CRYSTAL_MINE,
    GAS_REFINERY,
    SOLAR_PANELS,
    TELESCOPE,
    SHIPYARD;

    companion object {
        fun parse(value: String): BuildingType {
            return valueOf(value)
        }
    }
}

data class Building(val id: UUID, val planetId: UUID, val type: BuildingType, val level: Int) : Serializable

data class ConstructionSite(val id: UUID, val planetId: UUID, val type: BuildingType, val level: Int, val done: Long) : Serializable

abstract class BuildBuildingException(message: String) : Exception(message)

class NotEnoughBuildSlotsException() : BuildBuildingException("Not enough build slots available")

class BuildingAlreadyInProgress(val type: BuildingType) : BuildBuildingException("Building $type is already in progress")

interface BuildingService {
    fun createStarterBuildings(planet: Planet): List<Building>

    fun createBuilding(planet: Planet, type: BuildingType, level: Int)

    fun findBuildingsByPlanet(planet: Planet): List<Building>

    fun findBuildingByPlanetAndType(planet: Planet, type: BuildingType): Building?

    fun findBuildingByPlanetIdAndType(planetId: UUID, type: BuildingType): Building?

    fun findConstructionSitesByPlanet(planet: Planet): List<ConstructionSite>

    /**
     * Creates a new building of type [type] on planet [planet].
     *
     * @throws NotEnoughBuildSlotsException If not enough build slots are available.
     * @throws BuildingAlreadyInProgress If a building of the same type is already in progress.
     * @throws NotEnoughResourcesException If not enough resources are available.
     */
    fun build(planet: Planet, type: BuildingType): BuildResult

    fun finishConstructionSites()
}

interface BuildingRepository {
    fun insert(building: Building)

    fun updateLevel(buildingId: UUID, newLevel: Int)

    fun findByPlanetId(planetId: UUID): List<Building>

    fun findByPlanetIdAndType(planetId: UUID, type: BuildingType): Building?
}

interface ConstructionSiteRepository {
    fun insert(constructionSite: ConstructionSite)

    fun findByDone(done: Long): List<ConstructionSite>

    fun findByPlanetId(planetId: UUID): List<ConstructionSite>

    fun countByPlanetId(planetId: UUID): Int

    fun findByPlanetIdAndType(planetId: UUID, type: BuildingType): ConstructionSite?

    fun delete(id: UUID)
}

data class BuildResult(val planet: Planet, val constructionSite: ConstructionSite)

class BuildingServiceImpl(
        private val uuidFactory: UUIDFactory,
        private val buildingRepository: BuildingRepository,
        private val constructionSiteRepository: ConstructionSiteRepository,
        private val buildingFormulas: BuildingFormulas,
        private val roundService: RoundService,
        private val planetRepository: PlanetRepository
) : BuildingService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun build(planet: Planet, type: BuildingType): BuildResult {
        // Check if enough slots are available
        val commandCenter = buildingRepository.findByPlanetIdAndType(planet.id, BuildingType.COMMAND_CENTER) ?: throw AssertionError("Planet $planet has no command center")
        val slots = buildingFormulas.calculateBuildSlots(commandCenter.level)
        val constructionSites = constructionSiteRepository.countByPlanetId(planet.id)
        if (constructionSites >= slots) {
            throw NotEnoughBuildSlotsException()
        }

        // Check if the building type is already in progress
        if (constructionSiteRepository.findByPlanetIdAndType(planet.id, type) != null) {
            throw BuildingAlreadyInProgress(type)
        }

        val building = buildingRepository.findByPlanetIdAndType(planet.id, type)
        val level = if (building == null) {
            1
        } else {
            building.level + 1
        }

        val cost = buildingFormulas.calculateBuildCost(type, level)
        if (!planet.resources.enough(cost)) {
            throw NotEnoughResourcesException(cost, planet.resources)
        }

        val id = uuidFactory.create()

        val buildTime = calculateBuildTime(commandCenter.level, level, type)
        val done = roundService.currentRound() + buildTime

        // Create construction site
        val constructionSite = ConstructionSite(id, planet.id, type, level, done)
        constructionSiteRepository.insert(constructionSite)

        // Decrease resources
        val updatedPlanet = planet.decreaseResources(cost)
        planetRepository.updateResources(updatedPlanet.id, updatedPlanet.resources)

        return BuildResult(updatedPlanet, constructionSite)
    }

    private fun calculateBuildTime(commandCenterLevel: Int, level: Int, type: BuildingType): Int {
        val buildTimeModifier = buildingFormulas.calculateBuildingBuildTimeModifier(commandCenterLevel)

        return Math.max(1, (buildingFormulas.calculateBuildTime(type, level) * buildTimeModifier).ceil())
    }

    override fun findBuildingsByPlanet(planet: Planet): List<Building> {
        return buildingRepository.findByPlanetId(planet.id)
    }

    override fun createStarterBuildings(planet: Planet): List<Building> {
        val buildings = listOf(
                Building(uuidFactory.create(), planet.id, BuildingType.COMMAND_CENTER, 1),
                Building(uuidFactory.create(), planet.id, BuildingType.CRYSTAL_MINE, 1),
                Building(uuidFactory.create(), planet.id, BuildingType.GAS_REFINERY, 1),
                Building(uuidFactory.create(), planet.id, BuildingType.SOLAR_PANELS, 1)
        )

        buildings.forEach { buildingRepository.insert(it) }
        return buildings
    }

    override fun finishConstructionSites() {
        val currentRound = roundService.currentRound()

        val sitesDone = constructionSiteRepository.findByDone(currentRound)
        for (siteDone in sitesDone) {
            logger.debug("Finishing construction site {}", siteDone)
            if (siteDone.level == 1) {
                buildingRepository.insert(Building(uuidFactory.create(), siteDone.planetId, siteDone.type, siteDone.level))
            } else {
                val building = buildingRepository.findByPlanetIdAndType(siteDone.planetId, siteDone.type) ?: throw IllegalStateException("Expected to find building with type ${siteDone.type} on planet ${siteDone.planetId}, but found none")
                buildingRepository.updateLevel(building.id, siteDone.level)
            }
            constructionSiteRepository.delete(siteDone.id)
        }
    }

    override fun findConstructionSitesByPlanet(planet: Planet): List<ConstructionSite> {
        return constructionSiteRepository.findByPlanetId(planet.id)
    }

    override fun createBuilding(planet: Planet, type: BuildingType, level: Int) {
        val building = Building(uuidFactory.create(), planet.id, type, level)
        buildingRepository.insert(building)
    }

    override fun findBuildingByPlanetAndType(planet: Planet, type: BuildingType): Building? = findBuildingByPlanetIdAndType(planet.id, type)

    override fun findBuildingByPlanetIdAndType(planetId: UUID, type: BuildingType): Building? {
        return buildingRepository.findByPlanetIdAndType(planetId, type)
    }
}