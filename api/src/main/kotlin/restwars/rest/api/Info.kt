package restwars.rest.api

data class ApplicationInformationResponse(val version: String, val hash: String) : Result {
    companion object {}
}

