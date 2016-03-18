package restwars.rest.controller

import restwars.rest.api.SuccessResponse
import restwars.rest.base.HttpMethod
import restwars.rest.base.RestMethod
import restwars.rest.base.SimpleRestMethod

object RootController {
    fun get(): RestMethod<SuccessResponse> {
        return SimpleRestMethod(HttpMethod.GET, "/", SuccessResponse::class.java, { res, req ->
            SuccessResponse("RESTwars is running")
        })
    }
}

