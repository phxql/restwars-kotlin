package restwars.business.resource

import restwars.business.building.BuildingType
import restwars.business.planet.Resources
import restwars.util.ceil

class NotEnoughResourcesException(val needed: Resources, val available: Resources) : Exception("Not enough resources. Needed: $needed, available: $available")

interface ResourceService {
    fun calculateGatheredResources(type: BuildingType, level: Int): Resources
}

object ResourceServiceImpl : ResourceService {
    override fun calculateGatheredResources(type: BuildingType, level: Int): Resources {
        return when (type) {
            BuildingType.COMMAND_CENTER -> Resources.energy(2)
            BuildingType.CRYSTAL_MINE -> Resources.crystal((5 + (level - 1) * 2.5).ceil())
            BuildingType.GAS_REFINERY -> Resources.gas((2.5 + (level - 1) * 1.25).ceil())
            BuildingType.SOLAR_PANELS -> Resources.energy(20 + (level - 1) * 10)
            else -> Resources.none()
        }
    }
}