package restwars.business.config

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import restwars.business.planet.Resources
import java.nio.file.Files
import java.nio.file.Path

data class UniverseSize(val maxGalaxies: Int, val maxSystems: Int, val maxPlanets: Int)

data class StarterPlanet(val resources: Resources)

data class Config(val universeSize: UniverseSize, val starterPlanet: StarterPlanet, val roundTime: Int) {
    data class UniverseSizeDto(var maxGalaxies: Int = 0, var maxSystems: Int = 0, var maxPlanets: Int = 0) {
        fun toUniverseSize(): UniverseSize {
            return UniverseSize(maxGalaxies, maxSystems, maxPlanets)
        }
    }

    data class ResourcesDto(var crystal: Int = 0, var gas: Int = 0, var energy: Int = 0) {
        fun toResources(): Resources {
            return Resources(crystal, gas, energy)
        }
    }

    data class StarterPlanetDto(var resources: ResourcesDto = ResourcesDto()) {
        fun toStarterPlanet(): StarterPlanet {
            return StarterPlanet(resources.toResources())
        }
    }

    data class ConfigDto(var universeSize: UniverseSizeDto = UniverseSizeDto(), var starterPlanet: StarterPlanetDto = StarterPlanetDto(), var roundTime: Int = 0) {
        fun toConfig(): Config {
            return Config(universeSize.toUniverseSize(), starterPlanet.toStarterPlanet(), roundTime)
        }
    }

    companion object {
        fun loadFromFile(path: Path): Config {
            val yaml = Yaml(Constructor(ConfigDto::class.java))
            val configDto = Files.newBufferedReader(path).use {
                yaml.load(it) as ConfigDto
            }
            return configDto.toConfig()
        }
    }
}