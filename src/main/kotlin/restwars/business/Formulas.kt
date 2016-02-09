package restwars.business

import restwars.business.building.BuildingType

interface BuildingFormula {
    fun calculateBuildTime(type: BuildingType, level: Int): Int
}

object BuildingFormulaImpl : BuildingFormula {
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