package restwars.rest.controller

import restwars.business.ApplicationInformationService
import restwars.rest.api.ApplicationInformationResponse
import restwars.rest.api.fromApplicationInformation
import restwars.rest.base.ControllerHelper
import restwars.rest.base.HttpMethod
import restwars.rest.base.RestMethod
import restwars.rest.base.SimpleRestMethod

class ApplicationInformationController(val applicationInformationService: ApplicationInformationService) : ControllerHelper {
    fun get(): RestMethod<ApplicationInformationResponse> {
        return SimpleRestMethod(HttpMethod.GET, "/v1/restwars", ApplicationInformationResponse::class.java, { req, res ->
            val information = applicationInformationService.getInformation()

            ApplicationInformationResponse.fromApplicationInformation(information)
        })
    }
}