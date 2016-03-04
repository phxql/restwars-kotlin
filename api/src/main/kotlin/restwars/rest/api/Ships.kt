package restwars.rest.api

import java.util.*

data class ShipInConstructionResponse(val id: UUID, val type: String, val done: Long) : Result {
    companion object {}
}

data class ShipsInConstructionResponse(val shipsInConstruction: List<ShipInConstructionResponse>) : Result {
    companion object {}
}

data class ShipResponse(val type: String, val amount: Int) : Result {
    companion object {}
}

data class ShipsResponse(val ships: List<ShipResponse>) : Result {
    companion object {}
}

data class BuildShipRequest(
        @get:org.hibernate.validator.constraints.NotBlank
        val type: String
)