package restwars.business

import restwars.business.building.BuildingType
import restwars.business.ship.ShipType

interface BuildingFormulas {
    fun calculateBuildTime(type: BuildingType, level: Int): Int
}

interface ShipFormulas {
    fun calculateBuildTime(type: ShipType): Int
}

object BuildingFormulasImpl : BuildingFormulas {
    override fun calculateBuildTime(type: BuildingType, level: Int): Int {
        return when (type) {
            BuildingType.COMMAND_CENTER -> 50 + (level - 1) * 25
            BuildingType.CRYSTAL_MINE -> 30 + (level - 1) * 10
            BuildingType.GAS_REFINERY -> 30 + (level - 1) * 10
            BuildingType.SOLAR_PANELS -> 30 + (level - 1) * 10
        //            else -> throw IllegalArgumentException("Unknown building type: $type")
        }
    }
}

object ShipFormulasImpl : ShipFormulas {
    override fun calculateBuildTime(type: ShipType): Int {
        return when (type) {
            ShipType.MOSQUITO -> 10
        }
    }
}