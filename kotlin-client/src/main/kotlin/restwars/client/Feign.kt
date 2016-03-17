package restwars.client

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Feign
import feign.Headers
import feign.Param
import feign.RequestLine
import feign.auth.BasicAuthRequestInterceptor
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import restwars.rest.api.*

@Headers("Content-Type: application/json", "Accept: application/json")
interface Restwars {
    @RequestLine("POST /v1/player")
    fun createPlayer(request: CreatePlayerRequest)

    @RequestLine("GET /v1/planet")
    fun listPlanets(): PlanetsResponse

    @RequestLine("GET /v1/player/fight")
    fun listFights(): FightsResponse

    @RequestLine("GET /v1/player/fight?since={since}")
    fun listFights(@Param("since") since: Long): FightsResponse

    @RequestLine("GET /v1/planet/{location}/fight")
    fun listFightsWithPlanet(@Param("location") location: String): FightsResponse

    @RequestLine("GET /v1/planet/{location}/fight?since={since}")
    fun listFightsWithPlanet(@Param("location") location: String, @Param("since") since: Long): FightsResponse

    @RequestLine("POST /v1/planet/{location}/telescope/scan")
    fun telescopeScan(@Param("location") location: String): ScanResponse

    @RequestLine("GET /v1/restwars")
    fun applicationInformation(): ApplicationInformationResponse

    @RequestLine("GET /v1/configuration")
    fun restwarsConfiguration(): ConfigResponse

    @RequestLine("GET /v1/round")
    fun roundInformation(): RoundWithRoundTimeResponse

    @RequestLine("GET /v1/round/wait")
    fun waitForNextRound(): RoundResponse

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

    @RequestLine("GET /v1/flight")
    fun listFlights(): FlightsResponse

    @RequestLine("GET /v1/flight/to/{location}")
    fun listFlightsWithDestination(@Param("location") location: String): FlightsResponse

    @RequestLine("GET /v1/flight/from/{location}")
    fun listFlightsWithStart(@Param("location") location: String): FlightsResponse

    @RequestLine("GET /v1/flight/detected")
    fun listDetectedFlights(): DetectedFlightsResponse

    @RequestLine("GET /v1/flight/detected?since={since}")
    fun listDetectedFlights(@Param("since") since: Long): DetectedFlightsResponse

    @RequestLine("GET /v1/event")
    fun listEvents(): EventsResponse

    @RequestLine("GET /v1/event?since={since}")
    fun listEvents(@Param("since") since: Long): EventsResponse

    @RequestLine("GET /v1/metadata/ship")
    fun listShipMetadata(): ShipsMetadataResponse

    @RequestLine("GET /v1/metadata/building")
    fun listBuildingMetadata(): BuildingsMetadataResponse

    @RequestLine("GET /v1/tournament/wait")
    fun waitForTournamentStart(): SuccessResponse

    @RequestLine("GET /v1/points")
    fun listsPoints(): PointsResponse

    companion object {
        fun create(baseUrl: String, mapper: ObjectMapper): Restwars {
            return feignBuilder(mapper).target(Restwars::class.java, baseUrl)
        }

        fun create(baseUrl: String, username: String, password: String, mapper: ObjectMapper): Restwars {
            return feignBuilder(mapper).requestInterceptor(BasicAuthRequestInterceptor(username, password)).target(Restwars::class.java, baseUrl)
        }

        private fun feignBuilder(mapper: ObjectMapper): Feign.Builder {
            return Feign.builder()
                    .encoder(JacksonEncoder(mapper))
                    .decoder(JacksonDecoder(mapper))
                    .logger(Slf4jLogger())
        }
    }
}