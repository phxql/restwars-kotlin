package restwars.business.flight

import org.slf4j.LoggerFactory
import restwars.business.BuildingFormulas
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
    COLONIZE, ATTACK, TRANSFER, TRANSPORT;

    companion object {
        fun parse(value: String): FlightType {
            return FlightType.valueOf(value)
        }
    }
}

data class Flight(
        val id: UUID, val playerId: UUID, val start: Location, val destination: Location,
        val startedInRound: Long, val arrivalInRound: Long, val ships: Ships, val direction: FlightDirection,
        val type: FlightType, val cargo: Resources, val detected: Boolean, val speed: Double
) : Serializable

data class DetectedFlight(val id: UUID, val flightId: UUID, val playerId: UUID, val approximatedFleetSize: Long) : Serializable

data class SendResult(val planet: Planet, val flight: Flight)

abstract class FlightException(message: String?) : Exception(message) {
    constructor() : this(null)
}

class SameStartAndDestinationException : FlightException("Start and destination are the same")

class NoShipsException : FlightException("No ships on the flight")

class NotEnoughShipsException(val type: ShipType, val needed: Int, val available: Int) : FlightException("Not enough ships of type $type available. Needed: $needed, available: $available")

class ColonyShipRequiredException : FlightException("A colony ship on this flight is required")

class CargoNotAllowedException : FlightException("Cargo is not allowed")

class EnergyInCargoException : FlightException("Energy can't be put in cargo")

class NotEnoughCargoSpaceException(val needed: Int, val available: Int) : FlightException("Not enough cargo available: Needed: $needed, available: $available")

interface FlightService {
    /**
     * Sends ships on the flight to another planet.
     *
     * @throws SameStartAndDestinationException If [start] and [destination] are the same.
     * @throws InvalidLocationException If the location is invalid (e.g. not in the universe).
     * @throws NoShipsException If no ships are on the flight.
     * @throws ColonyShipRequiredException If the flight is a colonize flight and no colony ship is on the flight.
     * @throws CargoNotAllowedException If the flight is an attack flight and resources are in the cargo.
     * @throws EnergyInCargoException If energy is in the cargo.
     * @throws NotEnoughCargoSpaceException If not enough cargo space is available.
     * @throws NotEnoughResourcesException If not enough resources are available to start the flight.
     * @throws NotEnoughShipsException If not enough ships are available.
     */
    fun sendShipsToPlanet(player: Player, start: Planet, destination: Location, ships: Ships, type: FlightType, cargo: Resources): SendResult

    fun finishFlights()

    fun createReturnFlight(flight: Flight, ships: Ships, cargo: Resources)

    fun delete(flight: Flight)

    fun findWithPlayerAndDestination(player: Player, destination: Location): List<Flight>

    fun findWithPlayerAndStart(player: Player, start: Location): List<Flight>

    fun findWithPlayer(player: Player): List<Flight>

    fun detectFlights()
}

interface FlightRepository {
    fun insert(flight: Flight)

    fun update(id: UUID, ships: Ships, arrivalInRound: Long, direction: FlightDirection, cargo: Resources)

    fun delete(id: UUID)

    fun findByArrivalInRound(arrivalInRound: Long): List<Flight>

    fun findWithPlayerAndDestination(playerId: UUID, destination: Location): List<Flight>

    fun findWithPlayerAndStart(playerId: UUID, start: Location): List<Flight>

    fun findWithPlayer(playerId: UUID): List<Flight>

    fun findUndetectedFlights(): List<Flight>

    fun updateDetected(flightId: UUID, detected: Boolean)
}

interface DetectedFlightRepository {
    fun insert(detectedFlight: DetectedFlight)
}

interface FlightTypeHandler {
    fun handleFlight(flight: Flight, flightService: FlightService)
}

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
        private val transferFlightHandler: FlightTypeHandler,
        private val transportFlightHandler: FlightTypeHandler,
        private val planetService: PlanetService,
        private val buildingService: BuildingService,
        private val buildingFormulas: BuildingFormulas,
        private val detectedFlightRepository: DetectedFlightRepository
) : FlightService {
    val logger = LoggerFactory.getLogger(javaClass)

    override fun sendShipsToPlanet(player: Player, start: Planet, destination: Location, ships: Ships, type: FlightType, cargo: Resources): SendResult {
        // Flights which where start = location are forbidden
        if (start.location == destination) throw SameStartAndDestinationException()
        // Check that the location is contained in the universe
        if (!destination.isValid(config.universeSize)) throw InvalidLocationException(destination)
        // Empty flights are forbidden
        if (ships.isEmpty()) throw NoShipsException()
        // Colony flights must have a colony ship
        if (type == FlightType.COLONIZE && ships[ShipType.COLONY] < 1) throw ColonyShipRequiredException()
        // Cargo is only allowed on transport, transfer and colonize flights
        if (!cargo.isEmpty() && !(type == FlightType.TRANSFER || type == FlightType.COLONIZE || type == FlightType.TRANSPORT)) throw CargoNotAllowedException()
        // Energy can't be put in cargo
        if (cargo.energy > 0) throw EnergyInCargoException()

        // Check cargo size
        val cargoNeeded = cargo.crystal + cargo.gas
        val cargoAvailable = shipService.calculateCargoSpace(ships)
        if (cargoNeeded > cargoAvailable) throw NotEnoughCargoSpaceException(cargoNeeded, cargoAvailable)

        val distance = locationFormulas.calculateDistance(start.location, destination)

        val resourcesNeeded = calculateFlightCost(distance, ships) + cargo

        if (!start.resources.enough(resourcesNeeded)) {
            throw NotEnoughResourcesException(resourcesNeeded, start.resources)
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
        val updatedPlanet = planetService.removeResources(start, resourcesNeeded)

        // Decrease ships
        shipService.removeShips(start, ships)

        val flight = Flight(uuidFactory.create(), player.id, start.location, destination, currentRound, arrival, ships, FlightDirection.OUTWARD, type, cargo, false, slowestSpeed)
        flightRepository.insert(flight)

        return SendResult(updatedPlanet, flight)
    }

    override fun findWithPlayerAndDestination(player: Player, destination: Location): List<Flight> {
        return flightRepository.findWithPlayerAndDestination(player.id, destination)
    }

    override fun findWithPlayerAndStart(player: Player, start: Location): List<Flight> {
        return flightRepository.findWithPlayerAndStart(player.id, start)
    }

    override fun findWithPlayer(player: Player): List<Flight> {
        return flightRepository.findWithPlayer(player.id)
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

        val planet = planetService.findByLocation(flight.start)
        if (planet == null) {
            // TODO: What happens if the planet is no more?
            return;
        }
        // TODO: What happens if the planet changed the owner?

        // Unload cargo
        planetService.addResources(planet, flight.cargo)

        // Land ships in hangar
        shipService.addShips(planet, flight.ships)
        flightRepository.delete(flight.id)
    }

    private fun finishOutwardFlight(flight: Flight) {
        when (flight.type) {
            FlightType.COLONIZE -> colonizeFlightHandler.handleFlight(flight, this)
            FlightType.ATTACK -> attackFlightHandler.handleFlight(flight, this)
            FlightType.TRANSFER -> transferFlightHandler.handleFlight(flight, this)
            FlightType.TRANSPORT -> transportFlightHandler.handleFlight(flight, this)
            else -> throw AssertionError("Unhandled flight type: ${flight.type}")
        }
    }

    override fun createReturnFlight(flight: Flight, ships: Ships, cargo: Resources) {
        val distance = locationFormulas.calculateDistance(flight.start, flight.destination)
        val speed = calculateTravelSpeed(ships)

        val currentRound = roundService.currentRound()
        val arrival = calculateArrivalRound(currentRound, distance, speed)
        flightRepository.update(flight.id, ships, arrival, FlightDirection.RETURN, cargo)
    }

    override fun delete(flight: Flight) {
        flightRepository.delete(flight.id)
    }

    override fun detectFlights() {
        logger.debug("Detecting flights")

        val flights = flightRepository.findUndetectedFlights()
        val currentRound = roundService.currentRound()

        for (flight in flights) {
            val planet = planetService.findByLocation(flight.destination)
            // Ignore flights to uninhabited planets or own flights
            if (planet == null || planet.owner == flight.playerId) continue

            val telescope = buildingService.findBuildingByPlanetAndType(planet, BuildingType.TELESCOPE)
            if (telescope != null) {
                val range = buildingFormulas.calculateFlightDetectionRange(telescope.level) * (1.0 / flight.speed).ceil()
                if (currentRound + range >= flight.arrivalInRound) {
                    detectFlight(flight)
                }
            }
        }
    }

    private fun detectFlight(flight: Flight) {
        logger.debug("Detected flight {}", flight)

        val fleetSize = flight.ships.amount()
        val detectedFlight = DetectedFlight(uuidFactory.create(), flight.id, flight.playerId, fleetSize)
        detectedFlightRepository.insert(detectedFlight)

        flightRepository.updateDetected(flight.id, true)
    }
}