package restwars.business

import restwars.business.building.BuildingType
import restwars.business.config.UniverseSize
import restwars.business.planet.Location
import restwars.business.planet.Resources
import restwars.business.ship.ShipType

interface ResourceFormulas {
    fun calculatePoints(resources: Resources): Long
}

interface BuildingFormulas {
    fun calculateBuildTime(type: BuildingType, level: Int): Int

    fun calculateBuildCost(type: BuildingType, level: Int): Resources

    fun calculateBuildSlots(commandCenterLevel: Int): Int

    fun calculateShipBuildSlots(shipyardLevel: Int): Int

    fun calculateScanRange(telescopeLevel: Int): Int

    fun calculateBuildingBuildTimeModifier(commandCenterLevel: Int): Double

    fun calculateShipBuildTimeModifier(shipyardLevel: Int): Double

    fun calculateFlightDetectionRange(telescopeLevel: Int): Int
}

interface ShipFormulas {
    fun calculateBuildTime(type: ShipType): Int

    fun calculateFlightSpeed(type: ShipType): Double

    fun calculateBuildCost(type: ShipType): Resources

    fun calculateFlightCostModifier(type: ShipType): Double

    fun calculateAttackPoints(type: ShipType): Int

    fun calculateDefendPoints(type: ShipType): Int

    fun calculateCargoSpace(type: ShipType): Int

    fun calculatePoints(type: ShipType): Long
}

interface LocationFormulas {
    fun calculateDistance(start: Location, destination: Location): Long
}

object ResourceFormulasImpl : ResourceFormulas {
    private val crystalMultiplier = 4L
    private val gasMultiplier = 8L

    override fun calculatePoints(resources: Resources): Long {
        return resources.crystal * crystalMultiplier + resources.gas * gasMultiplier + resources.energy
    }
}

object BuildingFormulasImpl : BuildingFormulas {
    override fun calculateScanRange(telescopeLevel: Int): Int {
        if (telescopeLevel == 0) return 0
        return telescopeLevel - 1
    }

    override fun calculateBuildTime(type: BuildingType, level: Int): Int {
        return when (type) {
            BuildingType.COMMAND_CENTER -> 50 + (level - 1) * 25
            BuildingType.CRYSTAL_MINE -> 30 + (level - 1) * 10
            BuildingType.GAS_REFINERY -> 30 + (level - 1) * 10
            BuildingType.SOLAR_PANELS -> 30 + (level - 1) * 10
            BuildingType.TELESCOPE -> 50 + (level - 1) * 10
            BuildingType.SHIPYARD -> 100 + (level - 1) * 50
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
            BuildingType.TELESCOPE -> Resources(
                    100 + (level - 1) * 50,
                    50 + (level - 1) * 25,
                    400 + (level - 1) * 200
            )
            BuildingType.SHIPYARD -> Resources(
                    300 + (level - 1) * 150,
                    150 + (level - 1) * 75,
                    1200 + (level - 1) * 600
            )
        }
    }

    override fun calculateBuildSlots(commandCenterLevel: Int): Int {
        return 1
    }

    override fun calculateShipBuildSlots(shipyardLevel: Int): Int {
        return 1
    }

    override fun calculateBuildingBuildTimeModifier(commandCenterLevel: Int): Double {
        if (commandCenterLevel == 0) return 1.0

        // TODO Gameplay: This eventually reaches 0, and further upgrade are useless - fix this
        return Math.max(0.0, 1.0 - ((commandCenterLevel - 1) * 0.05))
    }

    override fun calculateShipBuildTimeModifier(shipyardLevel: Int): Double {
        if (shipyardLevel == 0) return 1.0

        // TODO Gameplay: This eventually reaches 0, and further upgrade are useless - fix this
        return Math.max(0.0, 1.0 - ((shipyardLevel - 1) * 0.05))
    }

    override fun calculateFlightDetectionRange(telescopeLevel: Int): Int {
        if (telescopeLevel == 0) return 0

        return telescopeLevel + 1
    }
}

class ShipFormulasImpl(private val resourceFormulas: ResourceFormulas) : ShipFormulas {
    override fun calculateBuildTime(type: ShipType): Int {
        return when (type) {
            ShipType.MOSQUITO -> 10
            ShipType.COLONY -> 60
            ShipType.MULE -> 20
        }
    }

    override fun calculateFlightSpeed(type: ShipType): Double {
        return when (type) {
            ShipType.MOSQUITO -> 1.0
            ShipType.COLONY -> 0.5
            ShipType.MULE -> 1.0
        }
    }

    override fun calculateBuildCost(type: ShipType): Resources {
        return when (type) {
            ShipType.MOSQUITO -> Resources(100, 20, 270)
            ShipType.COLONY -> Resources(350, 150, 1750)
            ShipType.MULE -> Resources(200, 100, 1225)
        }
    }

    override fun calculateFlightCostModifier(type: ShipType): Double {
        return when (type) {
            ShipType.MOSQUITO -> 1.0
            ShipType.COLONY -> 2.0
            ShipType.MULE -> 1.5
        }
    }

    override fun calculateAttackPoints(type: ShipType): Int {
        return when (type) {
            ShipType.MOSQUITO -> 14
            ShipType.COLONY -> 0
            ShipType.MULE -> 0
        }
    }

    override fun calculateDefendPoints(type: ShipType): Int {
        return when (type) {
            ShipType.MOSQUITO -> 10
            ShipType.COLONY -> 75
            ShipType.MULE -> 20
        }
    }

    override fun calculateCargoSpace(type: ShipType): Int {
        return when (type) {
            ShipType.MOSQUITO -> 10
            ShipType.COLONY -> 500
            ShipType.MULE -> 750
        }
    }

    override fun calculatePoints(type: ShipType): Long {
        return resourceFormulas.calculatePoints(calculateBuildCost(type))
    }
}

class LocationFormulasImpl(private val universeSize: UniverseSize) : LocationFormulas {
    override fun calculateDistance(start: Location, destination: Location): Long {
        // TODO: Add factors: (planet1 - planet2) + (system2 - system2) * x + (galaxy - galaxy) * y

        val planets = Math.abs(start.planet - destination.planet).toLong()
        val systems = Math.abs(start.system - destination.system).toLong()
        val galaxies = Math.abs(start.galaxy - destination.galaxy).toLong()

        return galaxies * universeSize.maxSystems * universeSize.maxPlanets + systems * universeSize.maxPlanets + planets
    }

    private fun cube(value: Long): Long = value * value * value

    private fun square(value: Long): Long = value * value
}