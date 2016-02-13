package restwars.rest.api

import restwars.business.ship.ShipInConstruction
import java.util.*

data class ShipInConstructionResponse(val id: UUID, val type: String, val done: Long) {
    companion object {
        fun fromShipInConstruction(shipInConstruction: ShipInConstruction) = ShipInConstructionResponse(shipInConstruction.id, shipInConstruction.type.name, shipInConstruction.done)
    }
}


data class BuildShipRequest(
        @get:org.hibernate.validator.constraints.NotBlank
        val type: String
)