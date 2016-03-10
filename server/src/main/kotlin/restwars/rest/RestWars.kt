package restwars.rest

import com.fasterxml.jackson.core.JsonParseException
import org.slf4j.LoggerFactory
import restwars.business.*
import restwars.business.building.BuildingServiceImpl
import restwars.business.clock.Clock
import restwars.business.clock.ClockImpl
import restwars.business.clock.RoundService
import restwars.business.clock.RoundServiceImpl
import restwars.business.config.Config
import restwars.business.config.NewPlanet
import restwars.business.config.StarterPlanet
import restwars.business.config.UniverseSize
import restwars.business.fight.FightCalculatorImpl
import restwars.business.fight.FightServiceImpl
import restwars.business.flight.*
import restwars.business.planet.PlanetServiceImpl
import restwars.business.planet.Resources
import restwars.business.player.PlayerServiceImpl
import restwars.business.point.PointsServiceImpl
import restwars.business.resource.ResourceServiceImpl
import restwars.business.ship.ShipServiceImpl
import restwars.business.tournament.*
import restwars.rest.api.ErrorResponse
import restwars.rest.base.*
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
    val commandLine = CommandLine.parse(args)
    val config = loadConfig()

    val uuidFactory = UUIDFactoryImpl
    val randomNumberGenerator = RandomNumberGeneratorImpl

    val playerRepository = InMemoryPlayerRepository
    val planetRepository = InMemoryPlanetRepository(playerRepository)
    val buildingRepository = InMemoryBuildingRepository
    val constructionSiteRepository = InMemoryConstructionSiteRepository
    val roundRepository = InMemoryRoundRepository
    val hangarRepository = InMemoryHangarRepository
    val shipInConstructionRepository = InMemoryShipInConstructionRepository
    val flightRepository = InMemoryFlightRepository
    val fightRepository = InMemoryFightRepository(playerRepository, planetRepository)
    val pointsRepository = InMemoryPointsRepository(playerRepository)

    val resourceFormulas = ResourceFormulasImpl
    val buildingFormulas = BuildingFormulasImpl
    val shipFormulas = ShipFormulasImpl(resourceFormulas)
    val locationFormulas = LocationFormulasImpl

    val roundService = RoundServiceImpl(roundRepository)
    val playerService = PlayerServiceImpl(uuidFactory, playerRepository)
    val planetService = PlanetServiceImpl(uuidFactory, randomNumberGenerator, planetRepository, config, buildingFormulas)
    val buildingService = BuildingServiceImpl(uuidFactory, buildingRepository, constructionSiteRepository, buildingFormulas, roundService, planetRepository)
    val resourceService = ResourceServiceImpl
    val lockService = LockServiceImpl
    val shipService = ShipServiceImpl(uuidFactory, roundService, hangarRepository, shipInConstructionRepository, shipFormulas, buildingFormulas, planetRepository, buildingService)
    val applicationInformationService = ApplicationInformationServiceImpl
    val fightCalculator = FightCalculatorImpl(uuidFactory, shipFormulas, randomNumberGenerator)
    val fightService = FightServiceImpl(fightCalculator, roundService, fightRepository)

    val colonizeFlightHandler = ColonizeFlightHandler(planetService, buildingService, shipService)
    val attackFlightHandler = AttackFlightHandler(planetService, fightService, shipService)
    val transferFlightHandler = TransferFlightHandler(planetService, shipService)
    val transportFlightHandler = TransportFlightHandler(planetService)
    val flightService = FlightServiceImpl(config, roundService, uuidFactory, flightRepository, shipFormulas, locationFormulas, shipService, colonizeFlightHandler, attackFlightHandler, transferFlightHandler, transportFlightHandler, planetService)
    val tournamentService = buildTournamentService(commandLine, roundService)
    val pointsService = PointsServiceImpl(roundService, playerService, planetService, shipService, shipFormulas, pointsRepository, uuidFactory)

    val clock = ClockImpl(planetService, resourceService, buildingService, lockService, roundService, shipService, flightService, pointsService, config)

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
    val telescopeController = TelescopeController(validatorFactory, playerService, planetService, buildingService)
    val fightController = FightController(validatorFactory, playerService, planetService, fightService)
    val shipMetadataController = ShipMetadataController(shipFormulas)
    val buildingMetadataController = BuildingMetadataController(buildingFormulas)
    val tournamentController = TournamentController(tournamentService)
    val pointsController = PointsController(pointsService)

    configureSpark()
    addExceptionHandler()
    registerWebsockets(roundService, tournamentService)
    registerRoutes(
            lockService, playerController, planetController, buildingController, constructionSiteController,
            shipController, shipyardController, applicationInformationController, configurationController,
            roundController, flightController, telescopeController, fightController, shipMetadataController,
            buildingMetadataController, tournamentService, tournamentController, pointsController
    )

    Spark.awaitInitialization()

    startClock(clock, config)
    val persister = Persister(
            buildingRepository, constructionSiteRepository, playerRepository, roundRepository, hangarRepository,
            shipInConstructionRepository, flightRepository, planetRepository, fightRepository, pointsRepository
    )
    persister.start()
    logger.info("RESTwars started on port {}", port)
}

private fun buildTournamentService(commandLine: CommandLine, roundService: RoundServiceImpl): TournamentService {
    if (commandLine.startRound == null) return NoopTournamentService

    logger.info("Tournament mode active. Only accepting requests on round {}", commandLine.startRound)

    val tournamentService = TournamentServiceImpl
    // Add round listener which watches the clock to start the tournament
    roundService.addRoundListener(TournamentRoundListener(roundService, tournamentService, commandLine.startRound))
    return tournamentService
}

data class CommandLine(val startRound: Long?) {
    companion object {
        fun parse(args: Array<String>): CommandLine {
            // TODO: Use something like Commons CLI for commandline parsing

            val tournamentIndex = args.indexOf("--tournament")
            val tournamentRound = if (tournamentIndex > -1) {
                args[tournamentIndex + 1].toLong()
            } else null

            return CommandLine(tournamentRound)
        }
    }
}

fun registerWebsockets(roundService: RoundService, tournamentService: TournamentService) {
    RoundWebsocketController.roundService = roundService
    Spark.webSocket("/v1/round/websocket", RoundWebsocketController::class.java)

    TournamentWebsocketController.tournamentService = tournamentService
    Spark.webSocket("/v1/tournament/websocket", TournamentWebsocketController::class.java)
}

/**
 * Function to create a Spark route to a controller method.
 *
 * The result of the controller method will be serialized in JSON.
 *
 * @param lockService If not null, a lock will be aquired before the request and released afterwards.
 * @param tournamentService If not null, a check is executed if the tournament has already started. If the tournament hasn't been started, an exception is thrown.
 */
// May be obsolete after https://github.com/perwendel/spark/pull/406 has been merged
private fun route(method: Method, lockService: LockService? = null, tournamentService: TournamentService? = null): Route {
    return Route { request, response ->
        lockService?.beforeRequest()
        try {
            // Check if tournament has started
            if (tournamentService != null && !tournamentService.hasStarted()) throw TournamentNotStartedException()

            return@Route Json.toJson(response, method.invoke(request, response))
        } finally {
            lockService?.afterRequest()
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
        return Config(UniverseSize(1, 3, 3), StarterPlanet(Resources(200, 100, 800)), NewPlanet(Resources(100, 50, 400)), 5, 50)
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
        flightController: FlightController, telescopeController: TelescopeController,
        fightController: FightController, shipMetadataController: ShipMetadataController,
        buildingMetadataController: BuildingMetadataController, tournamentService: TournamentService,
        tournamentController: TournamentController, pointsController: PointsController
) {
    Spark.get("/", Json.contentType, route(RootController.get()))
    Spark.get("/v1/restwars", Json.contentType, route(applicationInformationController.get()))
    Spark.get("/v1/configuration", Json.contentType, route(configurationController.get()))
    Spark.get("/v1/round", Json.contentType, route(roundController.get(), lockService))
    Spark.get("/v1/points", Json.contentType, route(pointsController.get(), lockService))
    Spark.get("/v1/metadata/ship", Json.contentType, route(shipMetadataController.get()))
    Spark.get("/v1/metadata/building", Json.contentType, route(buildingMetadataController.get()))
    Spark.get("/v1/tournament/wait", Json.contentType, route(tournamentController.block()))

    Spark.post("/v1/player", Json.contentType, route(playerController.create(), lockService, tournamentService))
    Spark.get("/v1/player/fight", Json.contentType, route(fightController.byPlayer(), lockService, tournamentService))
    Spark.get("/v1/planet", Json.contentType, route(planetController.list(), lockService, tournamentService))
    Spark.get("/v1/planet/:location/building", Json.contentType, route(buildingController.listOnPlanet(), lockService, tournamentService))
    Spark.post("/v1/planet/:location/building", Json.contentType, route(buildingController.build(), lockService, tournamentService))
    Spark.get("/v1/planet/:location/construction-site", Json.contentType, route(constructionSiteController.listOnPlanet(), lockService, tournamentService))
    Spark.get("/v1/planet/:location/hangar", Json.contentType, route(shipController.listOnPlanet(), lockService, tournamentService))
    Spark.post("/v1/planet/:location/hangar", Json.contentType, route(shipController.build(), lockService, tournamentService))
    Spark.get("/v1/planet/:location/shipyard", Json.contentType, route(shipyardController.listOnPlanet(), lockService, tournamentService))
    Spark.post("/v1/planet/:location/flight", Json.contentType, route(flightController.create(), lockService, tournamentService))
    Spark.post("/v1/planet/:location/telescope/scan", Json.contentType, route(telescopeController.scan(), lockService, tournamentService))
    Spark.get("/v1/planet/:location/fight", Json.contentType, route(fightController.byPlanet(), lockService, tournamentService))
    Spark.get("/v1/flight/from/:location", Json.contentType, route(flightController.listFrom(), lockService, tournamentService))
    Spark.get("/v1/flight/to/:location", Json.contentType, route(flightController.listTo(), lockService, tournamentService))
    Spark.get("/v1/flight", Json.contentType, route(flightController.list(), lockService, tournamentService))
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

    Spark.exception(JsonParseException::class.java, fun(e, req, res) {
        res.status(StatusCode.UNPROCESSABLE_ENTITY)
        res.body(Json.toJson(res, ErrorResponse(e.message ?: "")))
    })

    Spark.exception(TournamentNotStartedException::class.java, fun(e, req, res) {
        res.status(StatusCode.SERVICE_UNAVAILABLE)
        res.body(Json.toJson(res, ErrorResponse(e.message ?: "")))
    })
}