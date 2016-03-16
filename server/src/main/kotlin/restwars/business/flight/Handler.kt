package restwars.business.flight

import org.slf4j.LoggerFactory
import restwars.business.building.BuildingService
import restwars.business.building.BuildingType
import restwars.business.event.EventService
import restwars.business.fight.FightService
import restwars.business.planet.Planet
import restwars.business.planet.PlanetService
import restwars.business.planet.Resources
import restwars.business.ship.ShipService
import restwars.business.ship.ShipType
import restwars.business.ship.Ships

class AttackFlightHandler(
        private val planetService: PlanetService,
        private val fightService: FightService,
        private val shipService: ShipService
) : FlightTypeHandler {
    val logger = LoggerFactory.getLogger(javaClass)

    override fun handleFlight(flight: Flight, flightService: FlightService) {
        logger.debug("Handling attack flight {}", flight)

        val planet = planetService.findByLocation(flight.destination)
        if (planet == null) {
            logger.debug("Planet ${flight.destination} is not colonized")
            flightService.createReturnFlight(flight, flight.ships, flight.cargo)
            return
        }

        if (planet.owner == flight.playerId) {
            logger.debug("Planet {} is friendly, creating return flight", flight.destination)
            flightService.createReturnFlight(flight, flight.ships, flight.cargo)
            return
        }

        val defenderShips = shipService.findShipsByPlanet(planet)

        val fight = fightService.attack(flight.playerId, planet.owner, planet.id, flight.ships, defenderShips)
        shipService.setShips(planet, fight.remainingDefenderShips)

        if (fight.remainingAttackerShips.isEmpty()) {
            logger.debug("Attacker lost all ships")
            flightService.delete(flight)
        } else {
            logger.debug("Looting planet")
            val loot = calculateLoot(planet, fight.remainingAttackerShips)
            logger.debug("Looting {}", loot)

            // Store in fight
            fightService.updateLoot(fight, loot)

            // Loot planet
            planetService.removeResources(planet, loot)

            flightService.createReturnFlight(flight, fight.remainingAttackerShips, loot)
        }
    }

    private fun calculateLoot(planet: Planet, ships: Ships): Resources {
        val cargoSpace = shipService.calculateCargoSpace(ships)
        val lootCrystals = Math.min(planet.resources.crystal, cargoSpace / 2)
        val lootGas = Math.min(planet.resources.gas, cargoSpace - lootCrystals)

        return Resources(lootCrystals, lootGas, 0)
    }
}

class ColonizeFlightHandler(
        private val planetService: PlanetService,
        private val buildingService: BuildingService,
        private val shipService: ShipService,
        private val eventService: EventService
) : FlightTypeHandler {
    val logger = LoggerFactory.getLogger(javaClass)

    override fun handleFlight(flight: Flight, flightService: FlightService) {
        logger.debug("Handling colonize flight {}", flight)

        val planet = planetService.findByLocation(flight.destination)
        if (planet != null) {
            logger.debug("Planet at {} is already colonized", flight.destination)
            flightService.createReturnFlight(flight, flight.ships, flight.cargo)
            return
        }

        logger.debug("Player {} colonized planet at {}", flight.playerId, flight.destination)
        val newPlanet = planetService.createPlanet(flight.playerId, flight.destination)
        buildingService.createBuilding(newPlanet, BuildingType.COMMAND_CENTER, 1)

        // Colony ship gets converted into a command center, land the remaining ships
        val shipsToLand = flight.ships - Ships.of(ShipType.COLONY, 1)
        shipService.addShips(newPlanet, shipsToLand)

        // Unload cargo
        planetService.addResources(newPlanet, flight.cargo)

        eventService.createPlanetColonizedEvent(newPlanet.owner, newPlanet.id)

        flightService.delete(flight)
    }
}

class TransferFlightHandler(
        private val planetService: PlanetService,
        private val shipService: ShipService,
        private val eventService: EventService
) : FlightTypeHandler {
    val logger = LoggerFactory.getLogger(javaClass)

    override fun handleFlight(flight: Flight, flightService: FlightService) {
        logger.debug("Handling transfer flight {}", flight)
        val planet = planetService.findByLocation(flight.destination)
        if (planet == null) {
            logger.debug("Planet at {} it not colonized", flight.destination)
            flightService.createReturnFlight(flight, flight.ships, flight.cargo)
            return
        }

        if (planet.owner != flight.playerId) {
            logger.debug("Planet at {} is hostile", flight.destination)
            flightService.createReturnFlight(flight, flight.ships, flight.cargo)
            return
        }

        // Land ships on planet
        shipService.addShips(planet, flight.ships)
        // Unload cargo
        planetService.addResources(planet, flight.cargo)

        flightService.delete(flight)
    }
}

class TransportFlightHandler(
        private val planetService: PlanetService,
        private val eventService: EventService
) : FlightTypeHandler {
    val logger = LoggerFactory.getLogger(javaClass)

    override fun handleFlight(flight: Flight, flightService: FlightService) {
        logger.debug("Handling transport flight {}", flight)
        val planet = planetService.findByLocation(flight.destination)
        if (planet == null) {
            logger.debug("Planet at {} is not colonized", flight.destination)
            flightService.createReturnFlight(flight, flight.ships, flight.cargo)
            return
        }

        if (planet.owner != flight.playerId) {
            logger.debug("Planet at {} is hostile", flight.destination)
            flightService.createReturnFlight(flight, flight.ships, flight.cargo)
            return
        }

        // Unload cargo
        planetService.addResources(planet, flight.cargo)

        // Send ships back
        flightService.createReturnFlight(flight, flight.ships, Resources.none())
    }
}