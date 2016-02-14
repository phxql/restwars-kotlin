package restwars.business.flight

import org.slf4j.LoggerFactory
import restwars.business.LocationFormulas
import restwars.business.ShipFormulas
import restwars.business.UUIDFactory
import restwars.business.clock.RoundService
import restwars.business.config.Config
import restwars.business.planet.InvalidLocationException
import restwars.business.planet.Location
import restwars.business.planet.Planet
import restwars.business.player.Player
import restwars.business.ship.ShipService
import restwars.business.ship.ShipType
import restwars.business.ship.Ships
import restwars.util.ceil
import java.io.Serializable
import java.util.*

enum class FlightDirection {
    OUTWARD, RETURN
}

data class Flight(
        val id: UUID, val playerId: UUID, val start: Location, val destination: Location,
        val startedInRound: Long, val arrivalInRound: Long, val ships: Ships, val direction: FlightDirection
) : Serializable

interface FlightService {
    fun sendShipsToPlanet(player: Player, start: Planet, destination: Location, ships: Ships): Flight
}

interface FlightRepository {
    fun insert(flight: Flight)
}

abstract class FlightException(message: String?) : Exception(message) {
    constructor() : this(null)
}

class SameStartAndDestinationException : FlightException()

class NoShipsException : FlightException()

class NotEnoughShipsException(val type: ShipType, val needed: Int, val available: Int) : FlightException("Not enough ships of type $type available. Needed: $needed, available: $available")

class FlightServiceImpl(
        private val config: Config,
        private val roundService: RoundService,
        private val uuidFactory: UUIDFactory,
        private val flightRepository: FlightRepository,
        private val shipFormulas: ShipFormulas,
        private val locationFormulas: LocationFormulas,
        private val shipService: ShipService
) : FlightService {
    val logger = LoggerFactory.getLogger(javaClass)

    override fun sendShipsToPlanet(player: Player, start: Planet, destination: Location, ships: Ships): Flight {
        // Flights which where start = location are forbissen
        if (start.location == destination) throw SameStartAndDestinationException()
        // Check that the location is contained in the universe
        if (!destination.isValid(config.universeSize)) throw InvalidLocationException(destination)
        // Empty flights are forbidden
        if (ships.isEmpty()) throw NoShipsException()

        val distance = locationFormulas.calculateDistance(start.location, destination)
        val shipsAvailable = shipService.findShipsByPlanet(start)

        // Check that enough ships are available
        for (ship in ships.ships) {
            val available = shipsAvailable[ship.type]
            if (ship.amount > available) throw NotEnoughShipsException(ship.type, ship.amount, available)
        }

        // Find the slowest ship
        val slowestSpeed = ships.ships.map { shipFormulas.calculateFlightSpeed(it.type) }.min() ?: throw AssertionError("Ships can't be empty")
        logger.debug("Slowest ship has speed {}", slowestSpeed)

        val currentRound = roundService.currentRound()
        val arrival = currentRound + (distance.toDouble() / slowestSpeed.toDouble()).ceil()

        // TODO: Check & consume resources
        // TODO: Decrease ships
        val flight = Flight(uuidFactory.create(), player.id, start.location, destination, currentRound, arrival, ships, FlightDirection.OUTWARD)
        flightRepository.insert(flight)

        return flight
    }
}