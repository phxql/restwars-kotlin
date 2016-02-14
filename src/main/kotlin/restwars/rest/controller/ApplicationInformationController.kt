package restwars.rest.controller

import restwars.business.ApplicationInformationService
import restwars.rest.api.ApplicationInformationResponse
import spark.Route

class ApplicationInformationController(val applicationInformationService: ApplicationInformationService) : ControllerHelper {
    fun get(): Route {
        return Route { req, res ->
            val information = applicationInformationService.getInformation()

            return@Route Json.toJson(res, ApplicationInformationResponse.fromApplicationInformation(information))
        }
    }
}