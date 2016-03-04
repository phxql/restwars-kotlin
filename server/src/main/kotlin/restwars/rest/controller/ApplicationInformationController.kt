package restwars.rest.controller

import restwars.business.ApplicationInformationService
import restwars.rest.api.ApplicationInformationResponse
import restwars.rest.api.Result
import restwars.rest.api.fromApplicationInformation
import restwars.rest.base.ControllerHelper
import restwars.rest.base.Method
import spark.Request
import spark.Response

class ApplicationInformationController(val applicationInformationService: ApplicationInformationService) : ControllerHelper {
    fun get(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val information = applicationInformationService.getInformation()

                return ApplicationInformationResponse.fromApplicationInformation(information)
            }
        }
    }
}