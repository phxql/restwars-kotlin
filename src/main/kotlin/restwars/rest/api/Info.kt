package restwars.rest.api

import restwars.business.ApplicationInformation

data class ApplicationInformationResponse(val version: String, val hash: String) {
    companion object {
        fun fromApplicationInformation(applicationInformation: ApplicationInformation) = ApplicationInformationResponse(applicationInformation.version, applicationInformation.hash)
    }
}

