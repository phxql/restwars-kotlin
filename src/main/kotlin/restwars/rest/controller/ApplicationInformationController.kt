package restwars.rest.controller

import restwars.business.ApplicationInformation
import restwars.business.ApplicationInformationService
import spark.Route

data class ApplicationInformationResponse(val version: String, val hash: String) {
    companion object {
        fun fromApplicationInformation(applicationInformation: ApplicationInformation) = ApplicationInformation(applicationInformation.version, applicationInformation.hash)
    }
}

class ApplicationInformationController(val applicationInformationService: ApplicationInformationService) : ControllerHelper {
    fun get(): Route {
        return Route { req, res ->
            val information = applicationInformationService.getInformation()

            return@Route Json.toJson(res, ApplicationInformationResponse.fromApplicationInformation(information))
        }
    }
}