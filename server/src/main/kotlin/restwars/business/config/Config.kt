package restwars.business.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import restwars.business.planet.Resources
import java.nio.file.Files
import java.nio.file.Path

data class UniverseSize(val maxGalaxies: Int, val maxSystems: Int, val maxPlanets: Int)

data class StarterPlanet(val resources: Resources)

data class NewPlanet(val resources: Resources)

data class Config(val universeSize: UniverseSize, val starterPlanet: StarterPlanet, val newPlanet: NewPlanet, val roundTime: Int) {
    data class UniverseSizeDto(val maxGalaxies: Int, val maxSystems: Int, val maxPlanets: Int) {
        fun toUniverseSize(): UniverseSize {
            return UniverseSize(maxGalaxies, maxSystems, maxPlanets)
        }
    }

    data class ResourcesDto(val crystal: Int, val gas: Int, val energy: Int) {
        fun toResources(): Resources {
            return Resources(crystal, gas, energy)
        }
    }

    data class StarterPlanetDto(val resources: ResourcesDto) {
        fun toStarterPlanet(): StarterPlanet {
            return StarterPlanet(resources.toResources())
        }
    }

    data class NewPlanetDto(val resources: ResourcesDto) {
        fun toNewPlanet(): NewPlanet {
            return NewPlanet(resources.toResources())
        }
    }

    data class ConfigDto(
            val universeSize: UniverseSizeDto,
            val starterPlanet: StarterPlanetDto,
            val newPlanet: NewPlanetDto,
            val roundTime: Int) {
        fun toConfig(): Config {
            return Config(universeSize.toUniverseSize(), starterPlanet.toStarterPlanet(), newPlanet.toNewPlanet(), roundTime)
        }
    }

    companion object {
        fun loadFromFile(path: Path): Config {
            val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
            mapper.registerModule(KotlinModule()) // Enable Kotlin support

            return Files.newBufferedReader(path).use {
                mapper.readValue(it, ConfigDto::class.java)
            }.toConfig()
        }
    }
}