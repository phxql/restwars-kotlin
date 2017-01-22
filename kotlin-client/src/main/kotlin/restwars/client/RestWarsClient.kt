package restwars.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import restwars.rest.api.*

open class RestWarsClient(val hostname: String, val port: Int) {
    protected val httpBaseUrl = "http://$hostname:$port/"
    private val webSocketBaseUrl = "ws://$hostname:$port/"

    protected val mapper = ObjectMapper()
            .registerModule(KotlinModule())
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)

    private val client: Restwars = Restwars.create(httpBaseUrl, mapper)

    private val roundWebSocket = WebSocket(mapper, RoundResponse::class.java, webSocketBaseUrl + "v1/round/websocket")
    private val tournamentWebSocket = WebSocket(mapper, SuccessResponse::class.java, webSocketBaseUrl + "v1/tournament/websocket")

    fun getPlayer(): PlayerResponse = client.getPlayer()

    fun createPlayer(username: String, password: String) {
        client.createPlayer(CreatePlayerRequest(username, password))
    }

    fun applicationInformation(): ApplicationInformationResponse = client.applicationInformation()

    fun restwarsConfiguration(): ConfigResponse = client.restwarsConfiguration()

    fun roundInformation(): RoundWithRoundTimeResponse = client.roundInformation()

    fun waitForNextRound(): RoundResponse = client.waitForNextRound()

    fun listShipMetadata(): ShipsMetadataResponse = client.listShipMetadata()

    fun listBuildingMetadata(level: Int = 1): BuildingsMetadataResponse = client.listBuildingMetadata(level)

    fun waitForTournamentStart(): SuccessResponse = client.waitForTournamentStart()

    fun listsPoints(): PointsResponse = client.listsPoints()

    fun withCredentials(username: String, password: String): AuthenticatingRestWarsClient {
        return AuthenticatingRestWarsClient(hostname, port, username, password)
    }

    /**
     * Adds a [callback] which is called on the start of a new round.
     */
    fun addRoundCallback(callback: WebSocketCallback<RoundResponse>) {
        roundWebSocket.addCallback(callback)
    }

    /**
     * Adds a [callback] which is called on the start of a new round.
     */
    fun addRoundCallback(callback: (RoundResponse) -> Unit) {
        addRoundCallback(object : WebSocketCallback<RoundResponse> {
            override fun call(response: RoundResponse) {
                callback(response)
            }
        })
    }

    /**
     * Removes a round [callback].
     */
    fun removeRoundCallback(callback: WebSocketCallback<RoundResponse>) {
        roundWebSocket.removeCallback(callback)
    }

    /**
     * Adds a [callback] which is called on the start of the tournament.
     */
    fun addTournamentCallback(callback: WebSocketCallback<SuccessResponse>) {
        tournamentWebSocket.addCallback(callback)
    }

    /**
     * Adds a [callback] which is called on the start of the tournament.
     */
    fun addTournamentCallback(callback: (SuccessResponse) -> Unit) {
        addTournamentCallback(object : WebSocketCallback<SuccessResponse> {
            override fun call(response: SuccessResponse) {
                callback(response)
            }
        })
    }

    /**
     * Removes a tournament [callback].
     */
    fun removeTournamentCallback(callback: WebSocketCallback<SuccessResponse>) {
        tournamentWebSocket.removeCallback(callback)
    }
}

class AuthenticatingRestWarsClient(hostname: String, port: Int, val username: String, val password: String) : RestWarsClient(hostname, port) {
    private val client: Restwars = Restwars.create(httpBaseUrl, username, password, mapper)

    init {
        // Test authorization
        client.getPlayer()
    }

    fun listPlanets(): PlanetsResponse = client.listPlanets()

    fun listFights(): FightsResponse = client.listFights()

    fun listFightsWithPlanet(location: String): FightsResponse = client.listFightsWithPlanet(location)

    fun telescopeScan(location: String): ScanResponse = client.telescopeScan(location)

    fun listBuildings(location: String): BuildingsResponse = client.listBuildings(location)

    fun constructBuilding(location: String, type: String): ConstructionSiteResponse = client.constructBuilding(location, BuildBuildingRequest(type))

    fun listConstructionSites(location: String): ConstructionSitesResponse = client.listConstructionSites(location)

    fun listShipsInHangar(location: String): ShipsResponse = client.listShipsInHangar(location)

    fun constructShip(location: String, type: String): ShipInConstructionResponse = client.constructShip(location, BuildShipRequest(type))

    fun listShipsInConstruction(location: String): ShipsInConstructionResponse = client.listShipsInConstruction(location)

    fun startFlight(location: String, destination: String, ships: ShipsRequest, type: String, cargo: CargoRequest): FlightResponse = client.startFlight(location, CreateFlightRequest(destination, ships, type, cargo))

    fun listFlights() = client.listFlights()

    fun listFlightsWithDestination(location: String) = client.listFlightsWithDestination(location)

    fun listFlightsWithStart(location: String) = client.listFlightsWithStart(location)

    fun listFights(since: Long): FightsResponse = client.listFights(since)

    fun listFightsWithPlanet(location: String, since: Long): FightsResponse = client.listFightsWithPlanet(location, since)

    fun listDetectedFlights(): DetectedFlightsResponse = client.listDetectedFlights()

    fun listDetectedFlights(since: Long): DetectedFlightsResponse = client.listDetectedFlights(since)

    fun listEvents(): EventsResponse = client.listEvents()

    fun listEvents(since: Long): EventsResponse = client.listEvents(since)
}