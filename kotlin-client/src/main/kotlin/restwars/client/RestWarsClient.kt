package restwars.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import feign.Feign
import feign.Headers
import feign.Param
import feign.RequestLine
import feign.auth.BasicAuthRequestInterceptor
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import restwars.rest.api.*

open class RestWarsClient(val baseUrl: String) {
    private val mapper = ObjectMapper().registerModule(KotlinModule())

    private val client: Restwars = feignBuilder()
            .target(Restwars::class.java, baseUrl)

    @Headers("Content-Type: application/json", "Accept: application/json")
    interface Restwars {
        @RequestLine("POST /v1/player")
        fun createPlayer(request: CreatePlayerRequest)

        @RequestLine("GET /v1/planet")
        fun listPlanets(): PlanetsResponse

        @RequestLine("GET /v1/player/fight")
        fun listFights(): FightsResponse

        @RequestLine("GET /v1/planet/{location}/fight")
        fun listFightsWithPlanet(@Param("location") location: String): FightsResponse

        @RequestLine("GET /v1/planet/{location}/telescope/scan")
        fun telescopeScan(@Param("location") location: String): ScanResponse

        @RequestLine("GET /v1/restwars")
        fun applicationInformation(): ApplicationInformationResponse

        @RequestLine("GET /v1/configuration")
        fun restwarsConfiguration(): ConfigResponse

        @RequestLine("GET /v1/round")
        fun roundInformation(): RoundResponse

        @RequestLine("GET /v1/planet/{location}/building")
        fun listBuildings(@Param("location") location: String): BuildingsResponse

        @RequestLine("POST /v1/planet/{location}/building")
        fun constructBuilding(@Param("location") location: String, request: BuildBuildingRequest): ConstructionSiteResponse

        @RequestLine("GET /v1/planet/{location}/construction-site")
        fun listConstructionSites(@Param("location") location: String): ConstructionSitesResponse

        @RequestLine("GET /v1/planet/{location}/hangar")
        fun listShipsInHangar(@Param("location") location: String): ShipsResponse

        @RequestLine("POST /v1/planet/{location}/hangar")
        fun constructShip(@Param("location") location: String, request: BuildShipRequest): ShipInConstructionResponse

        @RequestLine("GET /v1/planet/{location}/shipyard")
        fun listShipsInConstruction(@Param("location") location: String): ShipsInConstructionResponse

        @RequestLine("POST /v1/planet/{location}/flight")
        fun startFlight(@Param("location") location: String, request: CreateFlightRequest): FlightResponse

        @RequestLine("GET v1/flight")
        fun listFlights(): FlightsResponse

        @RequestLine("GET v1/flight/to/:location")
        fun listFlightsWithDestination(@Param("location") location: String): FlightsResponse

        @RequestLine("GET v1/flight/from/:location")
        fun listFlightsWithStart(@Param("location") location: String): FlightsResponse
    }

    fun createPlayer(username: String, password: String) {
        client.createPlayer(CreatePlayerRequest(username, password))
    }

    fun applicationInformation(): ApplicationInformationResponse = client.applicationInformation()

    fun restwarsConfiguration(): ConfigResponse = client.restwarsConfiguration()

    fun roundInformation(): RoundResponse = client.roundInformation()

    fun withCredentials(username: String, password: String): AuthorizedRestWarsClient {
        return AuthorizedRestWarsClient(baseUrl, username, password)
    }

    protected fun feignBuilder(): Feign.Builder {
        return Feign.builder()
                .encoder(JacksonEncoder(mapper))
                .decoder(JacksonDecoder(mapper))
                .logger(Slf4jLogger())
    }
}

class AuthorizedRestWarsClient(baseUrl: String, val username: String, val password: String) : RestWarsClient(baseUrl) {
    private val client: Restwars = feignBuilder()
            .requestInterceptor(BasicAuthRequestInterceptor(username, password))
            .target(Restwars::class.java, baseUrl)

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
}