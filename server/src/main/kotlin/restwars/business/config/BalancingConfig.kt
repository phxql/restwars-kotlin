package restwars.business.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import restwars.business.planet.Resources
import java.nio.file.Files
import java.nio.file.Path

data class BuildingBuildTime(val baseTime: Int, val gainPerLevel: Int)
data class BuildingBuildCost(val baseCost: Resources, val gainPerLevel: Resources)
data class BuildingProperties(val buildTime: BuildingBuildTime, val buildCost: BuildingBuildCost)
data class BuildingsProperties(val commandCenterProperties: BuildingProperties,
                               val crystalMineProperties: BuildingProperties,
                               val gasRefineryProperties: BuildingProperties,
                               val solarPanelsProperties: BuildingProperties,
                               val telescopeProperties: BuildingProperties,
                               val shipyardProperties: BuildingProperties,
                               val buildTimeAttenuation: Double)

data class ShipProperties(val buildTime: Int, val buildCost: Resources, val flightSpeed: Int, val flightCost: Int, val attackPoints: Int, val defendPoints: Int, val cargoSpace: Int)
data class ShipsProperties(val mosquitoProperties: ShipProperties, val colonyProperties: ShipProperties, val muleProperties: ShipProperties, val buildTimeAttenuation: Double)

data class ScoringProperties(val crystalMultiplier: Int, val gasMultiplier: Int)

data class BalancingConfig(val buildingsProperties: BuildingsProperties, val shipsProperties: ShipsProperties, val scoringProperties: ScoringProperties) {

    data class BuildingBuildTimeDto(val baseTime: Int, val gainPerLevel: Int) {
        fun toBuildingBuildTime(): BuildingBuildTime {
            return BuildingBuildTime(baseTime, gainPerLevel)
        }
    }

    data class BuildingBuildCostDto(val baseCost: GameConfig.ResourcesDto, val gainPerLevel: GameConfig.ResourcesDto) {
        fun toBuildingBuildCost(): BuildingBuildCost {
            return BuildingBuildCost(baseCost.toResources(), gainPerLevel.toResources())
        }
    }

    data class BuildingPropertiesDto(val buildTime: BuildingBuildTimeDto, val buildCost: BuildingBuildCostDto) {
        fun toBuildingProperties(): BuildingProperties {
            return BuildingProperties(buildTime.toBuildingBuildTime(), buildCost.toBuildingBuildCost())
        }
    }

    data class BuildingsPropertiesDto(val commandCenterProperties: BuildingPropertiesDto, val crystalMineProperties: BuildingPropertiesDto,
                                      val gasRefineryProperties: BuildingPropertiesDto, val solarPanelsProperties: BuildingPropertiesDto,
                                      val telescopeProperties: BuildingPropertiesDto, val shipyardProperties: BuildingPropertiesDto,
                                      val buildTimeAttenuation: Double) {
        fun toBuildingsProperties(): BuildingsProperties {
            return BuildingsProperties(commandCenterProperties.toBuildingProperties(), crystalMineProperties.toBuildingProperties(),
                    gasRefineryProperties.toBuildingProperties(), solarPanelsProperties.toBuildingProperties(),
                    telescopeProperties.toBuildingProperties(), shipyardProperties.toBuildingProperties(), buildTimeAttenuation)
        }
    }

    data class ShipPropertiesDto(val buildTime: Int, val buildCost: GameConfig.ResourcesDto, val flightSpeed: Int, val flightCost: Int, val attackPoints: Int, val defendPoints: Int, val cargoSpace: Int) {
        fun toShipProperties(): ShipProperties {
            return ShipProperties(buildTime, buildCost.toResources(), flightSpeed, flightCost, attackPoints, defendPoints, cargoSpace)
        }
    }

    data class ShipsPropertiesDto(val mosquitoProperties: ShipPropertiesDto, val colonyProperties: ShipPropertiesDto, val muleProperties: ShipPropertiesDto, val buildTimeAttenuation: Double) {
        fun toShipsPropertiesDto(): ShipsProperties {
            return ShipsProperties(mosquitoProperties.toShipProperties(), colonyProperties.toShipProperties(), muleProperties.toShipProperties(), buildTimeAttenuation)
        }
    }

    data class ScoringPropertiesDto(val crystalMultiplier: Int, val gasMultiplier: Int) {
        fun toScoringProperties(): ScoringProperties {
            return ScoringProperties(crystalMultiplier, gasMultiplier)
        }
    }

    data class BalancingConfigDto(val buildingsProperties: BuildingsPropertiesDto, val shipProperties: ShipsPropertiesDto, val scoringProperties: ScoringPropertiesDto) {
        fun toBalancingConfig(): BalancingConfig {
            return BalancingConfig(buildingsProperties.toBuildingsProperties(), shipProperties.toShipsPropertiesDto(), scoringProperties.toScoringProperties())
        }
    }

    companion object {
        fun loadFromFile(path: Path): BalancingConfig {
            val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
            mapper.registerModule(KotlinModule()) // Enable Kotlin support

            return Files.newBufferedReader(path).use {
                mapper.readValue(it, BalancingConfigDto::class.java)
            }.toBalancingConfig()
        }
    }
}


