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

// TODO: ShipsResponse should be a list!
data class ShipsResponse(val ships: List<ShipResponse>) : Result {
    companion object {}
}

data class BuildShipRequest(
        @get:org.hibernate.validator.constraints.NotBlank
        val type: String
)

data class ShipsMetadataResponse(val data: List<ShipMetadataResponse>) : Result {
    companion object {}
}

data class ShipMetadataResponse(
        val type: String, val buildTime: Int, val flightSpeed: Double, val buildCost: ResourcesResponse,
        val flightCostModifier: Double, val attackPoints: Int, val defensePoints: Int, val cargoSpace: Int
) : Result {
    companion object {}
}