package restwars.rest.api

import restwars.business.ApplicationInformation
import restwars.rest.base.Result

data class ApplicationInformationResponse(val version: String, val hash: String) : Result {
    companion object {
        fun fromApplicationInformation(applicationInformation: ApplicationInformation) = ApplicationInformationResponse(applicationInformation.version, applicationInformation.hash)
    }
}

