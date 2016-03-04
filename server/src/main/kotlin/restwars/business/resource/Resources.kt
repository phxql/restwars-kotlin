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
            BuildingType.CRYSTAL_MINE -> Resources.crystal(10 + (level - 1) * 5)
            BuildingType.GAS_REFINERY -> Resources.gas((5 + (level - 1) * 2.5).ceil());
            BuildingType.SOLAR_PANELS -> Resources.energy(40 + (level - 1) * 20);
            else -> Resources.none()
        }
    }
}