package restwars.rest.api

import restwars.business.ship.Ship
import restwars.business.ship.ShipInConstruction
import restwars.business.ship.Ships
import java.util.*

data class ShipInConstructionResponse(val id: UUID, val type: String, val done: Long) {
    companion object {
        fun fromShipInConstruction(shipInConstruction: ShipInConstruction) = ShipInConstructionResponse(shipInConstruction.id, shipInConstruction.type.name, shipInConstruction.done)
    }
}

data class ShipResponse(val type: String, val amount: Int) {
    companion object {
        fun fromShip(ship: Ship) = ShipResponse(ship.type.name, ship.amount)
    }
}

data class ShipsResponse(val ships: List<ShipResponse>) {
    companion object {
        fun fromShips(ships: Ships) = ShipsResponse(ships.ships.map { ShipResponse.fromShip(it) })
    }
}

data class BuildShipRequest(
        @get:org.hibernate.validator.constraints.NotBlank
        val type: String
)