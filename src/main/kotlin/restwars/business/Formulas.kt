package restwars.business

import restwars.business.building.BuildingType
import restwars.business.planet.Location
import restwars.business.ship.ShipType

interface BuildingFormulas {
    fun calculateBuildTime(type: BuildingType, level: Int): Int
}

interface ShipFormulas {
    fun calculateBuildTime(type: ShipType): Int

    fun calculateFlightSpeed(type: ShipType): Int
}

interface LocationFormulas {
    fun calculateDistance(start: Location, destination: Location): Long
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

    override fun calculateFlightSpeed(type: ShipType): Int {
        return when (type) {
            ShipType.MOSQUITO -> 1
        }
    }
}

object LocationFormulasImpl : LocationFormulas {
    override fun calculateDistance(start: Location, destination: Location): Long {
        return Math.abs(start.planet - destination.planet).toLong() + Math.abs(start.system - destination.system) + Math.abs(start.galaxy - destination.galaxy)
    }
}