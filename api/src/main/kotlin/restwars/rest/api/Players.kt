package restwars.rest.api

data class CreatePlayerRequest(
        @get:org.hibernate.validator.constraints.NotBlank
        val username: String,
        @get:org.hibernate.validator.constraints.NotBlank
        val password: String
)

data class PlayerResponse(
        val username: String
) : Result {
    companion object {}
}