package restwars.business.building

import org.slf4j.LoggerFactory
import restwars.business.BuildingFormulas
import restwars.business.UUIDFactory
import restwars.business.clock.RoundService
import restwars.business.planet.Planet
import java.io.Serializable
import java.util.*

enum class BuildingType {
    COMMAND_CENTER,
    CRYSTAL_MINE,
    GAS_REFINERY,
    SOLAR_PANELS;

    companion object {
        fun parse(value: String): BuildingType {
            return valueOf(value)
        }
    }
}

data class Building(val id: UUID, val planetId: UUID, val type: BuildingType, val level: Int) : Serializable

data class ConstructionSite(val id: UUID, val planetId: UUID, val type: BuildingType, val level: Int, val done: Long) : Serializable

interface BuildingService {
    fun createStarterBuildings(planet: Planet): List<Building>

    fun findBuildingsByPlanet(planet: Planet): List<Building>

    fun findConstructionSitesByPlanet(planet: Planet): List<ConstructionSite>

    fun build(planet: Planet, type: BuildingType): ConstructionSite

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

    fun delete(id: UUID)
}

class BuildingServiceImpl(
        val uuidFactory: UUIDFactory,
        val buildingRepository: BuildingRepository,
        val constructionSiteRepository: ConstructionSiteRepository,
        val buildingFormulas: BuildingFormulas,
        val roundService: RoundService
) : BuildingService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun build(planet: Planet, type: BuildingType): ConstructionSite {
        val building = buildingRepository.findByPlanetIdAndType(planet.id, type)
        // TODO: Check resources
        // TODO: Check build queues
        // TODO: Check if building type already in queue
        val level = if (building == null) {
            1
        } else {
            building.level + 1
        }

        val id = uuidFactory.create()
        val buildTime = buildingFormulas.calculateBuildTime(type, level)
        val done = roundService.currentRound() + buildTime

        val constructionSite = ConstructionSite(id, planet.id, type, level, done)
        constructionSiteRepository.insert(constructionSite)
        return constructionSite
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
}