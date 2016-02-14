package restwars.business

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

data class ApplicationInformation(val version: String, val hash: String)

interface ApplicationInformationService {
    fun getInformation(): ApplicationInformation
}

object ApplicationInformationServiceImpl : ApplicationInformationService {
    data class ApplicationInformationDto(var version: String = "", var hash: String = "") {
        fun toApplicationInformation() = ApplicationInformation(version, hash)
    }

    override fun getInformation(): ApplicationInformation {
        val yaml = Yaml(Constructor(ApplicationInformationDto::class.java))
        val dto = javaClass.getResourceAsStream("/info.yaml").use {
            yaml.load(it) as ApplicationInformationDto
        }
        return dto.toApplicationInformation()
    }
}