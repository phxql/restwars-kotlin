package restwars.business.flight

import org.slf4j.LoggerFactory
import restwars.business.LocationFormulas
import restwars.business.ShipFormulas
import restwars.business.UUIDFactory
import restwars.business.building.BuildingService
import restwars.business.building.BuildingType
import restwars.business.clock.RoundService
import restwars.business.config.Config
import restwars.business.planet.*
import restwars.business.player.Player
import restwars.business.resource.NotEnoughResourcesException
import restwars.business.ship.ShipService
import restwars.business.ship.ShipType
import restwars.business.ship.Ships
import restwars.util.ceil
import java.io.Serializable
import java.util.*

enum class FlightDirection {
    OUTWARD, RETURN
}

enum class FlightType {
    COLONIZE, ATTACK;

    companion object {
        fun parse(value: String): FlightType {
            return FlightType.valueOf(value)
        }
    }
}

data class Flight(
        val id: UUID, val playerId: UUID, val start: Location, val destination: Location,
        val startedInRound: Long, val arrivalInRound: Long, val ships: Ships, val direction: FlightDirection,
        val type: FlightType
) : Serializable

data class SendResult(val planet: Planet, val flight: Flight)

interface FlightService {
    fun sendShipsToPlanet(player: Player, start: Planet, destination: Location, ships: Ships, type: FlightType): SendResult

    fun finishFlights()

    fun createReturnFlight(flight: Flight, ships: Ships)
}

interface FlightRepository {
    fun insert(flight: Flight)

    fun update(id: UUID, ships: Ships, arrivalInRound: Long, direction: FlightDirection)

    fun delete(id: UUID)

    fun findByArrivalInRound(arrivalInRound: Long): List<Flight>
}

interface FlightTypeHandler {
    fun handleFlight(flight: Flight, flightService: FlightService)
}

abstract class FlightException(message: String?) : Exception(message) {
    constructor() : this(null)
}

class SameStartAndDestinationException : FlightException()

class NoShipsException : FlightException()

class NotEnoughShipsException(val type: ShipType, val needed: Int, val available: Int) : FlightException("Not enough ships of type $type available. Needed: $needed, available: $available")

class ColonyShipRequiredException() : FlightException("A colony ship on this flight is required")

class FlightServiceImpl(
        private val config: Config,
        private val roundService: RoundService,
        private val uuidFactory: UUIDFactory,
        private val flightRepository: FlightRepository,
        private val shipFormulas: ShipFormulas,
        private val locationFormulas: LocationFormulas,
        private val shipService: ShipService,
        private val colonizeFlightHandler: FlightTypeHandler,
        private val attackFlightHandler: FlightTypeHandler,
        private val planetRepository: PlanetRepository
) : FlightService {
    val logger = LoggerFactory.getLogger(javaClass)

    override fun sendShipsToPlanet(player: Player, start: Planet, destination: Location, ships: Ships, type: FlightType): SendResult {
        // Flights which where start = location are forbidden
        if (start.location == destination) throw SameStartAndDestinationException()
        // Check that the location is contained in the universe
        if (!destination.isValid(config.universeSize)) throw InvalidLocationException(destination)
        // Empty flights are forbidden
        if (ships.isEmpty()) throw NoShipsException()
        // Colony flights must have a colony ship
        if (type == FlightType.COLONIZE && ships[ShipType.COLONY] < 1) throw ColonyShipRequiredException()

        val distance = locationFormulas.calculateDistance(start.location, destination)

        val cost = calculateFlightCost(distance, ships)

        if (!start.resources.enough(cost)) {
            throw NotEnoughResourcesException(cost, start.resources)
        }

        val shipsAvailable = shipService.findShipsByPlanet(start)

        // Check that enough ships are available
        for (ship in ships.ships) {
            val available = shipsAvailable[ship.type]
            if (ship.amount > available) throw NotEnoughShipsException(ship.type, ship.amount, available)
        }

        // Find the slowest ship
        val slowestSpeed = calculateTravelSpeed(ships)
        logger.debug("Slowest ship has speed {}", slowestSpeed)

        val currentRound = roundService.currentRound()
        val arrival = calculateArrivalRound(currentRound, distance, slowestSpeed)

        // Decrease resources
        val updatedPlanet = start.decreaseResources(cost)
        planetRepository.updateResources(updatedPlanet.id, updatedPlanet.resources)

        // Decrease ships
        shipService.removeShips(start, ships)

        val flight = Flight(uuidFactory.create(), player.id, start.location, destination, currentRound, arrival, ships, FlightDirection.OUTWARD, type)
        flightRepository.insert(flight)

        return SendResult(updatedPlanet, flight)
    }

    private fun calculateFlightCost(distance: Long, ships: Ships): Resources {
        // Energy is calculated for outward and return flight, hence times 2
        return Resources.energy(ships.ships.map { shipFormulas.calculateFlightCostModifier(it.type) * it.amount * distance * 2 }.sum().ceil())
    }

    private fun calculateArrivalRound(currentRound: Long, distance: Long, slowestSpeed: Double): Long {
        return currentRound + (distance / slowestSpeed).ceil()
    }

    private fun calculateTravelSpeed(ships: Ships): Double {
        return ships.ships.map { shipFormulas.calculateFlightSpeed(it.type) }.min() ?: throw AssertionError("Ships can't be empty")
    }

    override fun finishFlights() {
        val currentRound = roundService.currentRound()

        val flights = flightRepository.findByArrivalInRound(currentRound)
        for (flight in flights) {
            when (flight.direction) {
                FlightDirection.OUTWARD -> finishOutwardFlight(flight)
                FlightDirection.RETURN -> finishReturnFlight(flight)
            }
        }
    }

    private fun finishReturnFlight(flight: Flight) {
        logger.debug("Finishing return flight {}", flight)

        val planet = planetRepository.findAtLocation(flight.start)
        if (planet == null) {
            // TODO: What happens if the planet is no more?
            return;
        }
        // TODO: What happens if the planet changed the owner?

        // Land ships in hangar
        shipService.addShips(planet, flight.ships)
        flightRepository.delete(flight.id)
    }

    private fun finishOutwardFlight(flight: Flight) {
        when (flight.type) {
            FlightType.COLONIZE -> colonizeFlightHandler.handleFlight(flight, this)
            FlightType.ATTACK -> attackFlightHandler.handleFlight(flight, this)
            else -> throw AssertionError("Unhandled flight type: ${flight.type}")
        }
    }

    override fun createReturnFlight(flight: Flight, ships: Ships) {
        val distance = locationFormulas.calculateDistance(flight.start, flight.destination)
        val speed = calculateTravelSpeed(ships)

        val currentRound = roundService.currentRound()
        val arrival = calculateArrivalRound(currentRound, distance, speed)
        flightRepository.update(flight.id, ships, arrival, FlightDirection.RETURN)
    }
}

class AttackFlightHandler : FlightTypeHandler {
    val logger = LoggerFactory.getLogger(javaClass)

    override fun handleFlight(flight: Flight, flightService: FlightService) {
        logger.debug("Handling attack flight {}", flight)
        // TODO: Attack planet
        flightService.createReturnFlight(flight, flight.ships)
    }
}

class ColonizeFlightHandler(
        private val planetService: PlanetService,
        private val buildingService: BuildingService,
        private val shipService: ShipService
) : FlightTypeHandler {
    val logger = LoggerFactory.getLogger(javaClass)

    override fun handleFlight(flight: Flight, flightService: FlightService) {
        logger.debug("Handling colonize flight {}", flight)

        val planet = planetService.findByLocation(flight.destination)
        if (planet != null) {
            logger.debug("Planet at {} is already colonized", flight.destination)
            flightService.createReturnFlight(flight, flight.ships)
            return
        }

        logger.debug("Player {} colonized planet at {}", flight.playerId, flight.destination)
        val newPlanet = planetService.createPlanet(flight.playerId, flight.destination)
        buildingService.createBuilding(newPlanet, BuildingType.COMMAND_CENTER, 1)

        // Colony ship gets converted into a command center, land the remaining ships
        val shipsToLand = flight.ships - Ships.of(ShipType.COLONY, 1)
        shipService.addShips(newPlanet, shipsToLand)
    }
}