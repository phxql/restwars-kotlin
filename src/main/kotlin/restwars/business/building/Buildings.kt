package restwars.business.building

import restwars.business.UUIDFactory
import restwars.business.planet.Planet
import java.util.*

enum class BuildingType {
    COMMAND_CENTER,
    CRYSTAL_MINE,
    GAS_REFINERY,
    SOLAR_PANELS
}

data class Building(val id: UUID, val planetId: UUID, val type: BuildingType, val level: Int)

interface BuildingService {
    fun createStarterBuildings(planet: Planet): List<Building>

    fun findByPlanet(planet: Planet): List<Building>
}

interface BuildingRepository {
    fun insert(building: Building)

    fun findByPlanetId(planetId: UUID): List<Building>
}

class BuildingServiceImpl(
        val uuidFactory: UUIDFactory,
        val buildingRepository: BuildingRepository
) : BuildingService {
    override fun findByPlanet(planet: Planet): List<Building> {
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
}