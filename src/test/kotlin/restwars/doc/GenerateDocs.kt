package restwars.doc

import restwars.business.building.BuildingType
import restwars.business.flight.FlightType
import restwars.business.ship.ShipType
import restwars.rest.api.*
import restwars.rest.controller.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

object GenerateDocs {
    private enum class Type {
        RESPONSE, REQUEST;

        fun getPath(name: String): Path {
            val base = Paths.get("doc")

            return when (this) {
                RESPONSE -> base.resolve("responses").resolve(name)
                REQUEST -> base.resolve("requests").resolve(name)
            }
        }
    }

    private fun writeToFile(type: Type, filename: String, obj: Any) {
        Files.newBufferedWriter(type.getPath(filename), Charsets.UTF_8).use {
            it.write(Json.toJson(obj))
        }
    }

    fun run() {
        writeToFile(Type.REQUEST, "construct-building.json", BuildBuildingRequest(BuildingType.COMMAND_CENTER.name))
        writeToFile(Type.REQUEST, "construct-ship.json", BuildShipRequest(ShipType.MOSQUITO.name))
        writeToFile(Type.REQUEST, "create-player.json", CreatePlayerRequest("player1", "s3cret"))
        writeToFile(Type.REQUEST, "start-flight.json", CreateFlightRequest("1.2.3", ShipsRequest(mapOf(
                ShipType.MOSQUITO.name to 5,
                ShipType.COLONY.name to 1
        )), FlightType.COLONIZE.name, CargoRequest(100, 50)))

        writeToFile(Type.RESPONSE, "application-information.json", ApplicationInformationResponse("1.0.0", "f1596555-2039-42a4-9a95-2db312871b6a"))
        writeToFile(Type.RESPONSE, "configuration.json", ConfigResponse(30))
        writeToFile(Type.RESPONSE, "construct-building.json", ConstructionSiteResponse(UUID.fromString("8c43bda7-3f91-48dc-baaa-9ba0f510a92c"), BuildingType.COMMAND_CENTER.name, 2, 102))
        writeToFile(Type.RESPONSE, "construct-ship.json", ShipInConstructionResponse(UUID.fromString("873bbd47-8a03-4407-a1d8-5665bd6b150b"), ShipType.MOSQUITO.name, 73))
        writeToFile(Type.RESPONSE, "list-buildings.json", BuildingsResponse(listOf(
                BuildingResponse(BuildingType.COMMAND_CENTER.name, 2),
                BuildingResponse(BuildingType.CRYSTAL_MINE.name, 3)
        )))
        writeToFile(Type.RESPONSE, "list-construction-sites.json", ConstructionSitesResponse(listOf(
                ConstructionSiteResponse(UUID.fromString("b2747307-b88b-4fc2-9236-84f59abc9ad6"), BuildingType.COMMAND_CENTER.name, 2, 102)
        )))
        writeToFile(Type.RESPONSE, "list-planets.json", PlanetsResponse(listOf(
                PlanetResponse(LocationResponse(1, 2, 3), ResourcesResponse(100, 50, 400)),
                PlanetResponse(LocationResponse(2, 3, 4), ResourcesResponse(200, 100, 800))
        )))
        writeToFile(Type.RESPONSE, "round-information.json", RoundResponse(13, 30))
        writeToFile(Type.RESPONSE, "round-websocket.json", RoundWebsocketResponse(17))
        writeToFile(Type.RESPONSE, "ships-in-construction.json", ShipsInConstructionResponse(listOf(
                ShipInConstructionResponse(UUID.randomUUID(), ShipType.COLONY.name, 55))
        ))
        writeToFile(Type.RESPONSE, "ships-in-hangar.json", ShipsResponse(listOf(
                ShipResponse(ShipType.MOSQUITO.name, 3),
                ShipResponse(ShipType.COLONY.name, 2))
        ))
        writeToFile(Type.RESPONSE, "start-flight.json", FlightResponse(LocationResponse(1, 2, 3), LocationResponse(4, 5, 6), 112, ShipsResponse(listOf(
                ShipResponse(ShipType.MOSQUITO.name, 3),
                ShipResponse(ShipType.COLONY.name, 2))
        )))
        writeToFile(Type.RESPONSE, "telescope-scan.json", ScanResponse(listOf(
                ScannedPlanetResponse(LocationResponse(1, 1, 1), "player1"),
                ScannedPlanetResponse(LocationResponse(1, 1, 2), "player2"),
                ScannedPlanetResponse(LocationResponse(1, 1, 3), "player1")
        )))
    }
}

fun main(args: Array<String>) {
    GenerateDocs.run()
}