package restwars.rest.api

import restwars.business.flight.Flight
import restwars.business.planet.Resources
import restwars.business.ship.Ship
import restwars.business.ship.ShipType
import restwars.business.ship.Ships
import restwars.rest.base.Result

data class ShipsRequest(
        @get:org.hibernate.validator.constraints.NotEmpty
        val ships: Map<String, Int>
) {
    fun toShips(): Ships {
        val ships = ships.entries.map { Ship(ShipType.parse(it.key), it.value) }
        return Ships(ships)
    }
}

data class CargoRequest(
        @get:javax.validation.constraints.Min(0)
        val crystal: Int,
        @get:javax.validation.constraints.Min(0)
        val gas: Int
) {
    fun toResources(): Resources = Resources(crystal, gas, 0)
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
    companion object {
        fun from(flights: List<Flight>) = FlightsResponse(flights.map { FlightResponse.fromFlight(it) })
    }
}

data class FlightResponse(
        val start: LocationResponse,
        val destination: LocationResponse,
        val arrivalInRound: Long,
        val ships: ShipsResponse
) : Result {
    companion object {
        fun fromFlight(flight: Flight) = FlightResponse(
                LocationResponse.fromLocation(flight.start), LocationResponse.fromLocation(flight.destination),
                flight.arrivalInRound, ShipsResponse.fromShips(flight.ships)
        )
    }
}