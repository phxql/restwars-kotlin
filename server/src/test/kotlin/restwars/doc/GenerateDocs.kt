package restwars.doc

import restwars.business.building.BuildingType
import restwars.business.event.EventType
import restwars.business.flight.FlightDirection
import restwars.business.flight.FlightType
import restwars.business.ship.ShipType
import restwars.rest.api.*
import restwars.rest.base.Json
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
        val path = type.getPath(filename)
        println("Writing to ${path.toAbsolutePath()}")

        Files.newBufferedWriter(path, Charsets.UTF_8).use {
            it.write(Json.toJson(obj))
        }
    }

    private fun <T : Enum<*>> writeEnumList(type: Type, filename: String, values: Array<T>) {
        val path = type.getPath(filename)
        println("Writing enum to ${path.toAbsolutePath()}")

        Files.newBufferedWriter(path, Charsets.UTF_8).use {
            for (value in values) {
                it.write("* ${value.name}")
                it.newLine()
            }
        }
    }

    fun run() {
        writeToFile(Type.RESPONSE, "error.json", ErrorResponse(ErrorReason.PLANET_NOT_FOUND.name, "Planet at 1.2.3 not found"));
        writeEnumList(Type.RESPONSE, "error-reason.enum", ErrorReason.values())

        writeToFile(Type.REQUEST, "construct-building.json", BuildBuildingRequest(BuildingType.COMMAND_CENTER.name))
        writeToFile(Type.REQUEST, "construct-ship.json", BuildShipRequest(ShipType.MOSQUITO.name))
        writeToFile(Type.REQUEST, "create-player.json", CreatePlayerRequest("player1", "s3cret"))
        writeToFile(Type.REQUEST, "start-flight.json", CreateFlightRequest("1.2.3", ShipsRequest(mapOf(
                ShipType.MOSQUITO.name to 5,
                ShipType.COLONY.name to 1
        )), FlightType.COLONIZE.name, CargoRequest(100, 50)))

        writeToFile(Type.RESPONSE, "application-information.json", ApplicationInformationResponse("1.0.0", "e1d9d75d436a4adaca8d0fd56f5c13550e95e797"))
        writeToFile(Type.RESPONSE, "configuration.json", ConfigResponse(30, UniverseSizeResponse(1, 3, 1, 3, 1, 3)))
        writeToFile(Type.RESPONSE, "construct-building.json", ConstructionSiteResponse(UUID.fromString("f1596555-2039-42a4-9a95-2db312871b6a"), BuildingType.COMMAND_CENTER.name, 2, 102))
        writeEnumList(Type.REQUEST, "building.enum", BuildingType.values())

        writeToFile(Type.RESPONSE, "construct-ship.json", ShipInConstructionResponse(UUID.fromString("873bbd47-8a03-4407-a1d8-5665bd6b150b"), ShipType.MOSQUITO.name, 73))
        writeEnumList(Type.REQUEST, "ship.enum", ShipType.values())

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
        writeToFile(Type.RESPONSE, "round-information.json", RoundWithRoundTimeResponse(13, 30))
        writeToFile(Type.RESPONSE, "round-websocket.json", RoundResponse(17))
        writeToFile(Type.RESPONSE, "ships-in-construction.json", ShipsInConstructionResponse(listOf(
                ShipInConstructionResponse(UUID.fromString("a675a15c-aeec-47d8-8fc8-4bc0fd452800"), ShipType.COLONY.name, 55))
        ))
        writeToFile(Type.RESPONSE, "ships-in-hangar.json", ShipsResponse(listOf(
                ShipResponse(ShipType.MOSQUITO.name, 3),
                ShipResponse(ShipType.COLONY.name, 2))
        ))
        writeToFile(Type.RESPONSE, "start-flight.json", FlightResponse(LocationResponse(1, 2, 3), LocationResponse(4, 5, 6), 112, ShipsResponse(listOf(
                ShipResponse(ShipType.MOSQUITO.name, 3),
                ShipResponse(ShipType.COLONY.name, 2))
        ), FlightDirection.OUTWARD.name, ResourcesResponse(0, 0, 0)))
        writeToFile(Type.RESPONSE, "telescope-scan.json", ScanResponse(listOf(
                ScannedPlanetResponse(LocationResponse(1, 1, 1), "player1"),
                ScannedPlanetResponse(LocationResponse(1, 1, 2), "player2"),
                ScannedPlanetResponse(LocationResponse(1, 1, 3), "player1")
        )))
        writeToFile(Type.RESPONSE, "fights.json", FightsResponse(listOf(
                FightResponse(UUID.fromString("d5694ae1-a035-4561-a6f8-d0d82f5aa8a9"), "player1", "player2", LocationResponse(1, 2, 3), ShipsResponse(listOf(
                        ShipResponse(ShipType.MOSQUITO.name, 3),
                        ShipResponse(ShipType.MULE.name, 2)
                )), ShipsResponse(listOf(
                        ShipResponse(ShipType.MOSQUITO.name, 5),
                        ShipResponse(ShipType.COLONY.name, 1)
                )), ShipsResponse(listOf()), ShipsResponse(listOf(
                        ShipResponse(ShipType.MOSQUITO.name, 1),
                        ShipResponse(ShipType.COLONY.name, 1)
                )), ResourcesResponse(100, 500, 0)
                ))
        ))
        writeToFile(Type.RESPONSE, "flights.json", FlightsResponse(listOf(
                FlightResponse(LocationResponse(1, 2, 3), LocationResponse(4, 5, 6), 112, ShipsResponse(listOf(
                        ShipResponse(ShipType.MOSQUITO.name, 3),
                        ShipResponse(ShipType.COLONY.name, 2))
                ), FlightDirection.OUTWARD.name, ResourcesResponse(0, 0, 0)),
                FlightResponse(LocationResponse(1, 2, 3), LocationResponse(7, 8, 9), 144, ShipsResponse(listOf(
                        ShipResponse(ShipType.MOSQUITO.name, 1),
                        ShipResponse(ShipType.MULE.name, 1))
                ), FlightDirection.OUTWARD.name, ResourcesResponse(0, 0, 0))
        )))
        writeToFile(Type.RESPONSE, "metadata-ships.json", ShipsMetadataResponse(listOf(
                ShipMetadataResponse("MOSQUITO", 12, 1.0, ResourcesResponse(10, 5, 40), 1.0, 14, 1, 50),
                ShipMetadataResponse("MULE", 24, 0.5, ResourcesResponse(20, 10, 80), 1.2, 1, 30, 500)
        )))
        writeToFile(Type.RESPONSE, "metadata-buildings.json", BuildingsMetadataResponse(listOf(
                BuildingMetadataResponse("COMMAND_CENTER", 1, 50, ResourcesResponse(100, 50, 400)),
                BuildingMetadataResponse("CRYSTAL_MINE", 1, 25, ResourcesResponse(50, 25, 200))
        )))
        writeToFile(Type.RESPONSE, "tournament-started.json", SuccessResponse("Tournament has started"))
        writeToFile(Type.RESPONSE, "points.json", PointsResponse(listOf(
                PointResponse("player1", 120),
                PointResponse("player2", 200)
        )))
        writeToFile(Type.RESPONSE, "detected-flights.json", DetectedFlightsResponse(listOf(
                DetectedFlightResponse(UUID.fromString("b1503ca0-83a5-44d2-a7a2-469c1ef596e2"), 12, LocationResponse(1, 2, 3), 15, 20),
                DetectedFlightResponse(UUID.fromString("14985b09-a80e-4c92-8ba5-7073a41591d7"), 10, LocationResponse(4, 5, 6), 25, 37)
        )))
        writeToFile(Type.RESPONSE, "events.json", EventsResponse(listOf(
                EventResponse(UUID.fromString("06319f52-9dd6-488f-bf84-2757e92217b4"), EventType.BUILDING_COMPLETE.name, 120, LocationResponse(1, 2, 3)),
                EventResponse(UUID.fromString("09deb43b-f6fc-4f80-9651-00c766d67d97"), EventType.PLANET_COLONIZED.name, 224, LocationResponse(4, 5, 6))
        )))
        writeEnumList(Type.RESPONSE, "event.enum", EventType.values())

        writeToFile(Type.RESPONSE, "player.json", PlayerResponse("player1"))
    }
}

fun main(args: Array<String>) {
    GenerateDocs.run()
}