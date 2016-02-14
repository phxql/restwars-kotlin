package restwars.rest.api

import restwars.business.flight.Flight
import restwars.business.ship.Ship
import restwars.business.ship.ShipType
import restwars.business.ship.Ships

data class ShipsRequest(
        @get:org.hibernate.validator.constraints.NotEmpty
        val ships: Map<String, Int>
) {
    fun toShips(): Ships {
        val ships = ships.entries.map { Ship(ShipType.parse(it.key), it.value) }
        return Ships(ships)
    }
}

data class CreateFlightRequest(
        @get:org.hibernate.validator.constraints.NotBlank
        val destination: String,
        @get:javax.validation.constraints.NotNull
        val ships: ShipsRequest
)

data class FlightResponse(
        val start: LocationResponse,
        val destination: LocationResponse,
        val arrivalInRound: Long,
        val ships: ShipsResponse
) {
    companion object {
        fun fromFlight(flight: Flight) = FlightResponse(
                LocationResponse.fromLocation(flight.start), LocationResponse.fromLocation(flight.destination),
                flight.arrivalInRound, ShipsResponse.fromShips(flight.ships)
        )
    }
}