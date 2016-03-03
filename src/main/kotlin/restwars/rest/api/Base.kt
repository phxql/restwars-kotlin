package restwars.rest.api

import restwars.rest.base.Result

data class ErrorResponse(val reason: String) : Result

data class SuccessResponse(val message: String) : Result