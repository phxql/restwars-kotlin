package restwars.rest.api

data class ShipsRequest(
        @get:org.hibernate.validator.constraints.NotEmpty
        val ships: Map<String, Int>
)

data class CargoRequest(
        @get:javax.validation.constraints.Min(0)
        val crystal: Int,
        @get:javax.validation.constraints.Min(0)
        val gas: Int
) {
    companion object {
        fun none() = CargoRequest(0, 0)
    }
}

data class CreateFlightRequest(
        @get:org.hibernate.validator.constraints.NotBlank
        val destination: String,
        @get:javax.validation.constraints.NotNull
        val ships: ShipsRequest,
        @get:org.hibernate.validator.constraints.NotBlank
        val type: String,
        val cargo: CargoRequest?
)

data class FlightsResponse(val flights: List<FlightResponse>) : Result {
    companion object {}
}

data class FlightResponse(
        val start: LocationResponse,
        val destination: LocationResponse,
        val arrivalInRound: Long,
        val ships: ShipsResponse
) : Result {
    companion object {}
}