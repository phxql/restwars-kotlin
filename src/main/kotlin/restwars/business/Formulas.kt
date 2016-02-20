package restwars.business

import restwars.business.building.BuildingType
import restwars.business.planet.Location
import restwars.business.planet.Resources
import restwars.business.ship.ShipType

interface BuildingFormulas {
    fun calculateBuildTime(type: BuildingType, level: Int): Int

    fun calculateBuildCost(type: BuildingType, level: Int): Resources

    fun calculateBuildSlots(commandCenterLevel: Int): Int

    fun calculateShipBuildSlots(shipyardLevel: Int): Int
}

interface ShipFormulas {
    fun calculateBuildTime(type: ShipType): Int

    fun calculateFlightSpeed(type: ShipType): Double

    fun calculateBuildCost(type: ShipType): Resources

    fun calculateFlightCostModifier(type: ShipType): Double
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
        }
    }

    override fun calculateBuildCost(type: BuildingType, level: Int): Resources {
        return when (type) {
            BuildingType.COMMAND_CENTER -> Resources(
                    200 + (level - 1) * 100,
                    100 + (level - 1) * 50,
                    800 + (level - 1) * 400
            )
            BuildingType.CRYSTAL_MINE -> Resources(
                    100 + (level - 1) * 50,
                    50 + (level - 1) * 25,
                    400 + (level - 1) * 200
            )
            BuildingType.GAS_REFINERY -> Resources(
                    100 + (level - 1) * 50,
                    50 + (level - 1) * 25,
                    400 + (level - 1) * 200
            )
            BuildingType.SOLAR_PANELS -> Resources(
                    100 + (level - 1) * 50,
                    50 + (level - 1) * 25,
                    400 + (level - 1) * 200)
        }
    }

    override fun calculateBuildSlots(commandCenterLevel: Int): Int {
        return 1
    }

    override fun calculateShipBuildSlots(shipyardLevel: Int): Int {
        return 1
    }
}

object ShipFormulasImpl : ShipFormulas {
    override fun calculateBuildTime(type: ShipType): Int {
        return when (type) {
            ShipType.MOSQUITO -> 10
            ShipType.COLONY -> 60
        }
    }

    override fun calculateFlightSpeed(type: ShipType): Double {
        return when (type) {
            ShipType.MOSQUITO -> 1.0
            ShipType.COLONY -> 0.5
        }
    }

    override fun calculateBuildCost(type: ShipType): Resources {
        return when (type) {
            ShipType.MOSQUITO -> Resources(100, 20, 270)
            ShipType.COLONY -> Resources(350, 150, 1750)
        }
    }

    override fun calculateFlightCostModifier(type: ShipType): Double {
        return when (type) {
            ShipType.MOSQUITO -> 1.0
            ShipType.COLONY -> 2.0
        }
    }
}

object LocationFormulasImpl : LocationFormulas {
    override fun calculateDistance(start: Location, destination: Location): Long {
        return Math.abs(start.planet - destination.planet).toLong() + Math.abs(start.system - destination.system) + Math.abs(start.galaxy - destination.galaxy)
    }
}