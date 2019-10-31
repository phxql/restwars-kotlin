package restwars.business

import restwars.business.building.BuildingType
import restwars.business.config.BalancingConfig
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

class ResourceFormulasImpl(private val balancingConfig: BalancingConfig) : ResourceFormulas {
    override fun calculatePoints(resources: Resources): Long {
        return resources.crystal * balancingConfig.scoringProperties.crystalMultiplier + resources.gas * balancingConfig.scoringProperties.gasMultiplier + resources.energy
    }
}

class BuildingFormulasImpl(balancingConfig: BalancingConfig) : BuildingFormulas {
    private val buildingsProperties = balancingConfig.buildingsProperties

    override fun calculateScanRange(telescopeLevel: Int): Int {
        if (telescopeLevel == 0) return 0
        return telescopeLevel - 1
    }

    override fun calculateBuildTime(type: BuildingType, level: Int): Int {
        return when (type) {
            BuildingType.COMMAND_CENTER -> buildingsProperties.commandCenterProperties.buildTime.baseTime + (level - 1) * buildingsProperties.commandCenterProperties.buildTime.gainPerLevel
            BuildingType.CRYSTAL_MINE -> buildingsProperties.crystalMineProperties.buildTime.baseTime + (level - 1) * buildingsProperties.crystalMineProperties.buildTime.gainPerLevel
            BuildingType.GAS_REFINERY -> buildingsProperties.gasRefineryProperties.buildTime.baseTime + (level - 1) * buildingsProperties.gasRefineryProperties.buildTime.gainPerLevel
            BuildingType.SOLAR_PANELS -> buildingsProperties.solarPanelsProperties.buildTime.baseTime + (level - 1) * buildingsProperties.solarPanelsProperties.buildTime.gainPerLevel
            BuildingType.TELESCOPE -> buildingsProperties.telescopeProperties.buildTime.baseTime + (level - 1) * buildingsProperties.telescopeProperties.buildTime.gainPerLevel
            BuildingType.SHIPYARD -> buildingsProperties.shipyardProperties.buildTime.baseTime + (level - 1) * buildingsProperties.shipyardProperties.buildTime.gainPerLevel
        }
    }

    override fun calculateBuildCost(type: BuildingType, level: Int): Resources {
        return when (type) {
            BuildingType.COMMAND_CENTER -> calculateBuildCostFromConfigValues(buildingsProperties.commandCenterProperties.buildCost.baseCost, buildingsProperties.commandCenterProperties.buildCost.gainPerLevel, level)
            BuildingType.CRYSTAL_MINE -> calculateBuildCostFromConfigValues(buildingsProperties.crystalMineProperties.buildCost.baseCost, buildingsProperties.crystalMineProperties.buildCost.gainPerLevel, level)
            BuildingType.GAS_REFINERY -> calculateBuildCostFromConfigValues(buildingsProperties.gasRefineryProperties.buildCost.baseCost, buildingsProperties.gasRefineryProperties.buildCost.gainPerLevel, level)
            BuildingType.SOLAR_PANELS -> calculateBuildCostFromConfigValues(buildingsProperties.solarPanelsProperties.buildCost.baseCost, buildingsProperties.solarPanelsProperties.buildCost.gainPerLevel, level)
            BuildingType.TELESCOPE -> calculateBuildCostFromConfigValues(buildingsProperties.telescopeProperties.buildCost.baseCost, buildingsProperties.telescopeProperties.buildCost.gainPerLevel, level)
            BuildingType.SHIPYARD -> calculateBuildCostFromConfigValues(buildingsProperties.shipyardProperties.buildCost.baseCost, buildingsProperties.shipyardProperties.buildCost.gainPerLevel, level)
        }
    }

    private fun calculateBuildCostFromConfigValues(baseCost: Resources, gainPerLevel: Resources, level: Int): Resources {
        return Resources(
                baseCost.crystal + (level - 1) * gainPerLevel.crystal,
                baseCost.gas + (level - 1) * gainPerLevel.gas,
                baseCost.energy + (level - 1) * gainPerLevel.energy
        )

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
        return Math.max(0.0, 1.0 - ((commandCenterLevel - 1) * buildingsProperties.buildTimeAttenuation))
    }

    override fun calculateShipBuildTimeModifier(shipyardLevel: Int): Double {
        if (shipyardLevel == 0) return 1.0

        // TODO Gameplay: This eventually reaches 0, and further upgrade are useless - fix this
        return Math.max(0.0, 1.0 - ((shipyardLevel - 1) * buildingsProperties.buildTimeAttenuation))
    }

    override fun calculateFlightDetectionRange(telescopeLevel: Int): Int {
        if (telescopeLevel == 0) return 0

        return telescopeLevel + 1
    }
}

class ShipFormulasImpl(private val resourceFormulas: ResourceFormulas, balancingConfig: BalancingConfig) : ShipFormulas {
    private val shipsProperties = balancingConfig.shipsProperties

    override fun calculateBuildTime(type: ShipType): Int {
        return when (type) {
            ShipType.MOSQUITO -> shipsProperties.mosquitoProperties.buildTime
            ShipType.COLONY -> shipsProperties.colonyProperties.buildTime
            ShipType.MULE -> shipsProperties.muleProperties.buildTime
        }
    }

    override fun calculateFlightSpeed(type: ShipType): Double {
        return when (type) {
            ShipType.MOSQUITO -> shipsProperties.mosquitoProperties.flightSpeed
            ShipType.COLONY -> shipsProperties.colonyProperties.flightSpeed
            ShipType.MULE -> shipsProperties.muleProperties.flightSpeed
        }
    }

    override fun calculateBuildCost(type: ShipType): Resources {
        return when (type) {
            ShipType.MOSQUITO -> shipsProperties.mosquitoProperties.buildCost
            ShipType.COLONY -> shipsProperties.colonyProperties.buildCost
            ShipType.MULE -> shipsProperties.muleProperties.buildCost
        }
    }

    override fun calculateFlightCostModifier(type: ShipType): Double {
        return when (type) {
            ShipType.MOSQUITO -> shipsProperties.mosquitoProperties.flightCost
            ShipType.COLONY -> shipsProperties.colonyProperties.flightCost
            ShipType.MULE -> shipsProperties.muleProperties.flightCost
        }
    }

    override fun calculateAttackPoints(type: ShipType): Int {
        return when (type) {
            ShipType.MOSQUITO -> shipsProperties.mosquitoProperties.attackPoints
            ShipType.COLONY -> shipsProperties.colonyProperties.attackPoints
            ShipType.MULE -> shipsProperties.muleProperties.attackPoints
        }
    }

    override fun calculateDefendPoints(type: ShipType): Int {
        return when (type) {
            ShipType.MOSQUITO -> shipsProperties.mosquitoProperties.defendPoints
            ShipType.COLONY -> shipsProperties.colonyProperties.defendPoints
            ShipType.MULE -> shipsProperties.muleProperties.defendPoints
        }
    }

    override fun calculateCargoSpace(type: ShipType): Int {
        return when (type) {
            ShipType.MOSQUITO -> shipsProperties.mosquitoProperties.cargoSpace
            ShipType.COLONY -> shipsProperties.colonyProperties.cargoSpace
            ShipType.MULE -> shipsProperties.colonyProperties.cargoSpace
        }
    }

    override fun calculatePoints(type: ShipType): Long {
        return resourceFormulas.calculatePoints(calculateBuildCost(type))
    }
}

class LocationFormulasImpl(private val universeSize: UniverseSize) : LocationFormulas {
    override fun calculateDistance(start: Location, destination: Location): Long {
        val planets = Math.abs(start.planet - destination.planet).toLong()
        val systems = Math.abs(start.system - destination.system).toLong()
        val galaxies = Math.abs(start.galaxy - destination.galaxy).toLong()

        return galaxies * universeSize.maxSystems * universeSize.maxPlanets + systems * universeSize.maxPlanets + planets
    }

    private fun cube(value: Long): Long = value * value * value

    private fun square(value: Long): Long = value * value
}