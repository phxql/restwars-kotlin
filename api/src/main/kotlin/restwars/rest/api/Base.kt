package restwars.rest.api

/**
 * Tagging interface for controller method results.
 */
interface Result {

}

data class ErrorResponse(val reason: String) : Result

data class SuccessResponse(val message: String) : Result