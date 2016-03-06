package restwars.rest.controller

import restwars.rest.api.Result
import restwars.rest.api.SuccessResponse
import restwars.rest.base.Method
import spark.Request
import spark.Response

object RootController {
    fun get(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                return SuccessResponse("RESTwars is running")
            }
        }
    }
}

