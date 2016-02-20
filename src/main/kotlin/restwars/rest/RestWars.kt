package restwars.rest

import org.slf4j.LoggerFactory
import restwars.business.*
import restwars.business.building.BuildingServiceImpl
import restwars.business.clock.Clock
import restwars.business.clock.ClockImpl
import restwars.business.clock.RoundService
import restwars.business.clock.RoundServiceImpl
import restwars.business.config.Config
import restwars.business.config.StarterPlanet
import restwars.business.config.UniverseSize
import restwars.business.flight.AttackFlightHandler
import restwars.business.flight.ColonizeFlightHandler
import restwars.business.flight.FlightServiceImpl
import restwars.business.planet.PlanetServiceImpl
import restwars.business.planet.Resources
import restwars.business.player.PlayerServiceImpl
import restwars.business.resource.ResourceServiceImpl
import restwars.business.ship.ShipServiceImpl
import restwars.rest.api.ErrorResponse
import restwars.rest.controller.*
import restwars.rest.http.StatusCode
import restwars.storage.*
import spark.Route
import spark.Spark
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.validation.Validation

val port = 7777

val logger = LoggerFactory.getLogger("restwars.rest.RestWars")

fun main(args: Array<String>) {
    val config = loadConfig()

    val uuidFactory = UUIDFactoryImpl
    val randomNumberGenerator = RandomNumberGeneratorImpl

    val playerRepository = InMemoryPlayerRepository
    val planetRepository = InMemoryPlanetRepository
    val buildingRepository = InMemoryBuildingRepository
    val constructionSiteRepository = InMemoryConstructionSiteRepository
    val roundRepository = InMemoryRoundRepository
    val hangarRepository = InMemoryHangarRepository
    val shipInConstructionRepository = InMemoryShipInConstructionRepository
    val flightRepository = InMemoryFlightRepository

    val buildingFormula = BuildingFormulasImpl
    val shipFormulas = ShipFormulasImpl
    val locationFormulas = LocationFormulasImpl

    val roundService = RoundServiceImpl(roundRepository)
    val playerService = PlayerServiceImpl(uuidFactory, playerRepository)
    val planetService = PlanetServiceImpl(uuidFactory, randomNumberGenerator, planetRepository, config)
    val buildingService = BuildingServiceImpl(uuidFactory, buildingRepository, constructionSiteRepository, buildingFormula, roundService)
    val resourceService = ResourceServiceImpl
    val lockService = LockServiceImpl
    val shipService = ShipServiceImpl(uuidFactory, roundService, hangarRepository, shipInConstructionRepository, shipFormulas)
    val applicationInformationService = ApplicationInformationServiceImpl

    val colonizeFlightHandler = ColonizeFlightHandler()
    val attackFlightHandler = AttackFlightHandler()
    val flightService = FlightServiceImpl(config, roundService, uuidFactory, flightRepository, shipFormulas, locationFormulas, shipService, colonizeFlightHandler, attackFlightHandler)

    val clock = ClockImpl(planetService, resourceService, buildingService, lockService, roundService, shipService, flightService)

    val validatorFactory = Validation.buildDefaultValidatorFactory()
    val playerController = PlayerController(validatorFactory, playerService, planetService, buildingService)
    val planetController = PlanetController(playerService, planetService)
    val buildingController = BuildingController(validatorFactory, playerService, planetService, buildingService)
    val constructionSiteController = ConstructionSiteController(validatorFactory, playerService, planetService, buildingService)
    val shipController = ShipController(validatorFactory, playerService, planetService, shipService)
    val shipyardController = ShipyardController(validatorFactory, playerService, planetService, shipService)
    val applicationInformationController = ApplicationInformationController(applicationInformationService)
    val configurationController = ConfigurationController(config)
    val roundController = RoundController(roundService, config)
    val flightController = FlightController(validatorFactory, playerService, planetService, flightService)

    configureSpark()
    addExceptionHandler()
    registerWebsockets(roundService)
    registerRoutes(
            lockService, playerController, planetController, buildingController, constructionSiteController,
            shipController, shipyardController, applicationInformationController, configurationController,
            roundController, flightController
    )

    Spark.awaitInitialization()

    startClock(clock, config)
    Persister.start()
    logger.info("RESTwars started on port {}", port)
}

fun registerWebsockets(roundService: RoundService) {
    RoundWebsocketController.roundService = roundService
    Spark.webSocket("/v1/round/websocket", RoundWebsocketController::class.java)
}

/**
 * Function to create a route which acquires a lock before the request and reliably releases the lock afterwards.
 */
// May be obsolete after https://github.com/perwendel/spark/pull/406 has been merged
private fun route(lockService: LockService, route: Route): Route {
    return Route { request, response ->
        lockService.beforeRequest()
        try {
            return@Route route.handle(request, response)
        } finally {
            lockService.afterRequest()
        }
    }
}

private fun startClock(clock: Clock, config: Config) {
    val executor = Executors.newSingleThreadScheduledExecutor({ runnable -> Thread(runnable, "Clock") })
    executor.scheduleAtFixedRate({
        clock.tick()
    }, config.roundTime.toLong(), config.roundTime.toLong(), TimeUnit.SECONDS)
}

private fun loadConfig(): Config {
    val configFile = Paths.get("config.yaml")
    if (!Files.exists(configFile)) {
        logger.warn("No config file at ${configFile.toAbsolutePath()} found, using default values")
        return Config(UniverseSize(1, 3, 3), StarterPlanet(Resources(200, 100, 800)), 5)
    }

    logger.info("Loading config from file ${configFile.toAbsolutePath()}")
    return Config.loadFromFile(configFile)
}

private fun configureSpark() {
    Spark.port(port)
}

private fun registerRoutes(
        lockService: LockService, playerController: PlayerController, planetController: PlanetController,
        buildingController: BuildingController, constructionSiteController: ConstructionSiteController,
        shipController: ShipController, shipyardController: ShipyardController,
        applicationInformationController: ApplicationInformationController,
        configurationController: ConfigurationController, roundController: RoundController,
        flightController: FlightController
) {
    Spark.get("/v1/restwars", Json.contentType, route(lockService, applicationInformationController.get()))
    Spark.get("/v1/configuration", Json.contentType, route(lockService, configurationController.get()))
    Spark.get("/v1/round", Json.contentType, route(lockService, roundController.get()))
    Spark.post("/v1/player", Json.contentType, route(lockService, playerController.create()))
    Spark.get("/v1/planet", Json.contentType, route(lockService, planetController.list()))
    Spark.get("/v1/planet/:location/building", Json.contentType, route(lockService, buildingController.listOnPlanet()))
    Spark.post("/v1/planet/:location/building", Json.contentType, route(lockService, buildingController.build()))
    Spark.get("/v1/planet/:location/construction-site", Json.contentType, route(lockService, constructionSiteController.listOnPlanet()))
    Spark.get("/v1/planet/:location/hangar", Json.contentType, route(lockService, shipController.listOnPlanet()))
    Spark.post("/v1/planet/:location/hangar", Json.contentType, route(lockService, shipController.build()))
    Spark.get("/v1/planet/:location/shipyard", Json.contentType, route(lockService, shipyardController.listOnPlanet()))
    Spark.post("/v1/planet/:location/flight", Json.contentType, route(lockService, flightController.create()))
}

private fun addExceptionHandler() {
    Spark.exception(ValidationException::class.java, fun(e, req, res) {
        res.status(StatusCode.BAD_REQUEST)
        res.body(Json.toJson(res, ErrorResponse("Request validation failed")))
    })

    Spark.exception(AuthenticationException::class.java, fun(e, req, res) {
        res.status(StatusCode.UNAUTHORIZED)
        res.body(Json.toJson(res, ErrorResponse("Invalid credentials")))
    })

    Spark.exception(PlanetNotFoundOrOwnedException::class.java, fun(e, req, res) {
        res.status(StatusCode.NOT_FOUND)
        res.body(Json.toJson(res, ErrorResponse(e.message ?: "")))
    })

    Spark.exception(BadRequestException::class.java, fun(e, req, res) {
        res.status(StatusCode.BAD_REQUEST)
        e as BadRequestException
        res.body(Json.toJson(res, e.response))
    })

}