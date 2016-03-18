package restwars.rest

import com.codahale.metrics.MetricRegistry
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
import restwars.business.event.EventServiceImpl
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
    val metricRegistry = MetricRegistry()

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
    val detectedFlightRepository = InMemoryDetectedFlightRepository(flightRepository)
    val eventRepository = InMemoryEventRepository(planetRepository)

    val resourceFormulas = ResourceFormulasImpl
    val buildingFormulas = BuildingFormulasImpl
    val shipFormulas = ShipFormulasImpl(resourceFormulas)
    val locationFormulas = LocationFormulasImpl

    val roundService = RoundServiceImpl(roundRepository)
    val eventService = EventServiceImpl(uuidFactory, roundService, eventRepository)
    val playerService = PlayerServiceImpl(uuidFactory, playerRepository)
    val planetService = PlanetServiceImpl(uuidFactory, randomNumberGenerator, planetRepository, config, buildingFormulas)
    val buildingService = BuildingServiceImpl(uuidFactory, buildingRepository, constructionSiteRepository, buildingFormulas, roundService, planetRepository, eventService)
    val resourceService = ResourceServiceImpl
    val lockService = LockServiceImpl
    val shipService = ShipServiceImpl(uuidFactory, roundService, hangarRepository, shipInConstructionRepository, shipFormulas, buildingFormulas, planetRepository, buildingService, eventService)
    val applicationInformationService = ApplicationInformationServiceImpl
    val fightCalculator = FightCalculatorImpl(uuidFactory, shipFormulas, randomNumberGenerator)
    val fightService = FightServiceImpl(fightCalculator, roundService, fightRepository)

    val colonizeFlightHandler = ColonizeFlightHandler(planetService, buildingService, shipService, eventService)
    val attackFlightHandler = AttackFlightHandler(planetService, fightService, shipService, eventService)
    val transferFlightHandler = TransferFlightHandler(planetService, shipService, eventService)
    val transportFlightHandler = TransportFlightHandler(planetService, eventService)
    val flightService = FlightServiceImpl(config, roundService, uuidFactory, flightRepository, shipFormulas, locationFormulas, shipService, colonizeFlightHandler, attackFlightHandler, transferFlightHandler, transportFlightHandler, planetService, buildingService, buildingFormulas, detectedFlightRepository, eventService)
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
    val detectedFlightController = DetectedFlightController(flightService, playerService)
    val eventController = EventController(validatorFactory, playerService, planetService, eventService)
    val metricController = MetricController(metricRegistry)

    configureSpark()
    addExceptionHandler()
    registerWebsockets(roundService, tournamentService)
    registerRoutes(
            metricRegistry, lockService, playerController, planetController, buildingController, constructionSiteController,
            shipController, shipyardController, applicationInformationController, configurationController,
            roundController, flightController, telescopeController, fightController, shipMetadataController,
            buildingMetadataController, tournamentService, tournamentController, pointsController,
            detectedFlightController, eventController, metricController
    )

    Spark.awaitInitialization()

    startClock(clock, config)
    val persister = Persister(
            buildingRepository, constructionSiteRepository, playerRepository, roundRepository, hangarRepository,
            shipInConstructionRepository, flightRepository, planetRepository, fightRepository, pointsRepository,
            detectedFlightRepository, eventRepository
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
        metricRegistry: MetricRegistry, lockService: LockService, playerController: PlayerController, planetController: PlanetController,
        buildingController: BuildingController, constructionSiteController: ConstructionSiteController,
        shipController: ShipController, shipyardController: ShipyardController,
        applicationInformationController: ApplicationInformationController,
        configurationController: ConfigurationController, roundController: RoundController,
        flightController: FlightController, telescopeController: TelescopeController,
        fightController: FightController, shipMetadataController: ShipMetadataController,
        buildingMetadataController: BuildingMetadataController, tournamentService: TournamentService,
        tournamentController: TournamentController, pointsController: PointsController,
        detectedFlightController: DetectedFlightController, eventController: EventController, metricController: MetricController
) {
    registerRestMethod(metricRegistry, RootController.get())
    registerRestMethod(metricRegistry, applicationInformationController.get())
    registerRestMethod(metricRegistry, configurationController.get())
    registerRestMethod(metricRegistry, roundController.get(), lockService)
    registerRestMethod(metricRegistry, roundController.wait())

    registerRestMethod(metricRegistry, pointsController.get(), lockService)
    registerRestMethod(metricRegistry, shipMetadataController.get())
    registerRestMethod(metricRegistry, buildingMetadataController.get())
    registerRestMethod(metricRegistry, tournamentController.wait())

    registerRestMethod(metricRegistry, playerController.get(), lockService, tournamentService)
    registerRestMethod(metricRegistry, playerController.create(), lockService, tournamentService)
    registerRestMethod(metricRegistry, fightController.byPlayer(), lockService, tournamentService)
    registerRestMethod(metricRegistry, planetController.list(), lockService, tournamentService)
    registerRestMethod(metricRegistry, buildingController.listOnPlanet(), lockService, tournamentService)
    registerRestMethod(metricRegistry, buildingController.build(), lockService, tournamentService)
    registerRestMethod(metricRegistry, constructionSiteController.listOnPlanet(), lockService, tournamentService)
    registerRestMethod(metricRegistry, shipController.listOnPlanet(), lockService, tournamentService)
    registerRestMethod(metricRegistry, shipController.build(), lockService, tournamentService)
    registerRestMethod(metricRegistry, shipyardController.listOnPlanet(), lockService, tournamentService)
    registerRestMethod(metricRegistry, flightController.create(), lockService, tournamentService)
    registerRestMethod(metricRegistry, telescopeController.scan(), lockService, tournamentService)
    registerRestMethod(metricRegistry, fightController.byPlanet(), lockService, tournamentService)
    registerRestMethod(metricRegistry, flightController.listFrom(), lockService, tournamentService)
    registerRestMethod(metricRegistry, flightController.listTo(), lockService, tournamentService)
    registerRestMethod(metricRegistry, flightController.list(), lockService, tournamentService)
    registerRestMethod(metricRegistry, detectedFlightController.byPlayer(), lockService, tournamentService)
    registerRestMethod(metricRegistry, eventController.byPlayer(), lockService, tournamentService)

    registerRestMethod(metricRegistry, metricController.all())
}

/**
 * Function to create a Spark route to a controller method.
 *
 * The result of the controller method will be serialized in JSON.
 *
 * @param method Method to register.
 * @param lockService If not null, a lock will be acquired before the request and released afterwards.
 * @param tournamentService If not null, a check is executed if the tournament has already started. If the tournament hasn't been started, an exception is thrown.
 */
fun registerRestMethod(metricRegistry: MetricRegistry, method: RestMethod<*>, lockService: LockService? = null, tournamentService: TournamentService? = null) {
    val metricName = "${method.verb.name} ${method.path}"
    val requestTimer = metricRegistry.timer(metricName)
    val allRequestsMetric = metricRegistry.timer("All requests")

    val route: Route = Route { request, response ->
        val timer = Pair(requestTimer.time(), allRequestsMetric.time())
        lockService?.beforeRequest()
        try {
            // Check if tournament has started
            if (tournamentService != null && !tournamentService.hasStarted()) throw TournamentNotStartedException()

            return@Route Json.toJson(response, method.invoke(request, response))
        } finally {
            lockService?.afterRequest()
            timer.first.stop()
            timer.second.stop()
        }
    }

    when (method.verb) {
        HttpMethod.POST -> Spark.post(method.path, Json.contentType, route)
        HttpMethod.GET -> Spark.get(method.path, Json.contentType, route)
        HttpMethod.DELETE -> Spark.delete(method.path, Json.contentType, route)
        HttpMethod.PUT -> Spark.put(method.path, Json.contentType, route)
    }
}

private fun addExceptionHandler() {
    Spark.exception(ValidationException::class.java) { e, req, res ->
        res.status(StatusCode.BAD_REQUEST)
        res.body(Json.toJson(res, ErrorResponse("Request validation failed")))
    }

    Spark.exception(AuthenticationException::class.java) { e, req, res ->
        res.status(StatusCode.UNAUTHORIZED)
        res.body(Json.toJson(res, ErrorResponse("Invalid credentials")))
    }

    Spark.exception(PlanetNotFoundOrOwnedException::class.java) { e, req, res ->
        res.status(StatusCode.NOT_FOUND)
        res.body(Json.toJson(res, ErrorResponse(e.message ?: "")))
    }

    Spark.exception(BadRequestException::class.java) { e, req, res ->
        e as BadRequestException

        res.status(StatusCode.BAD_REQUEST)
        res.body(Json.toJson(res, e.response))
    }

    Spark.exception(JsonParseException::class.java) { e, req, res ->
        res.status(StatusCode.UNPROCESSABLE_ENTITY)
        res.body(Json.toJson(res, ErrorResponse(e.message ?: "")))
    }

    Spark.exception(TournamentNotStartedException::class.java) { e, req, res ->
        res.status(StatusCode.SERVICE_UNAVAILABLE)
        res.body(Json.toJson(res, ErrorResponse(e.message ?: "")))
    }

    Spark.exception(StatusCodeException::class.java, { e, req, res ->
        e as StatusCodeException

        res.status(e.statusCode)
        res.body(Json.toJson(res, e.response))
    })
}