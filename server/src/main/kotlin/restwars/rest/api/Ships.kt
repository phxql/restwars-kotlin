package restwars.rest.api

import restwars.business.ship.Ship
import restwars.business.ship.ShipInConstruction
import restwars.business.ship.Ships

fun ShipInConstructionResponse.Companion.fromShipInConstruction(shipInConstruction: ShipInConstruction) = ShipInConstructionResponse(shipInConstruction.id, shipInConstruction.type.name, shipInConstruction.done)

fun ShipsInConstructionResponse.Companion.fromShipsInConstruction(shipsInConstruction: List<ShipInConstruction>) = ShipsInConstructionResponse(shipsInConstruction.map { ShipInConstructionResponse.fromShipInConstruction(it) })

fun ShipResponse.Companion.fromShip(ship: Ship) = ShipResponse(ship.type.name, ship.amount)

fun ShipsResponse.Companion.fromShips(ships: Ships) = ShipsResponse(ships.ships.map { ShipResponse.fromShip(it) })
