package restwars

import com.codahale.metrics.MetricRegistry
import com.fasterxml.jackson.core.JsonParseException
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.h2.Driver
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import restwars.business.*
import restwars.business.admin.Admin
import restwars.business.admin.AdminServiceImpl
import restwars.business.building.BuildingServiceImpl
import restwars.business.clock.Clock
import restwars.business.clock.ClockImpl
import restwars.business.clock.RoundService
import restwars.business.clock.RoundServiceImpl
import restwars.business.config.*
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
import restwars.rest.api.ErrorReason
import restwars.rest.api.ErrorResponse
import restwars.rest.base.*
import restwars.rest.controller.*
import restwars.rest.http.StatusCode
import restwars.storage.*
import spark.Filter
import spark.Route
import spark.Spark
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.sql.DataSource
import javax.validation.Validation


const val port = 7777

val logger = LoggerFactory.getLogger("restwars.rest.RestWars")

fun main(args: Array<String>) {
    val commandLine = CommandLine.parse(args)
    val gameConfig = loadGameConfig(commandLine.configFile)
    val balancingConfig = loadBalancingConfig(commandLine.balancingFile)

    val dataSource = connectToDatabase(gameConfig)
    FlywayMigrationService(dataSource).migrate()
    val jooq = createJooq(dataSource, gameConfig)

    val uuidFactory = UUIDFactoryImpl
    val randomNumberGenerator = RandomNumberGeneratorImpl
    val metricRegistry = MetricRegistry()

    val playerRepository = JooqPlayerRepository(jooq)
    val planetRepository = JooqPlanetRepository(jooq)
    val buildingRepository = JooqBuildingRepository(jooq)
    val constructionSiteRepository = JooqConstructionSiteRepository(jooq)
    val roundRepository = JooqRoundRepository(jooq)
    val hangarRepository = JooqHangarRepository(jooq)
    val shipInConstructionRepository = JooqShipInConstructionRepository(jooq)
    val flightRepository = JooqFlightRepository(jooq)
    val fightRepository = JooqFightRepository(jooq)
    val pointsRepository = JooqPointsRepository(jooq)
    val detectedFlightRepository = JooqDetectedFlightRepository(jooq)
    val eventRepository = JooqEventRepository(jooq)

    val resourceFormulas = ResourceFormulasImpl(balancingConfig)
    val buildingFormulas = BuildingFormulasImpl(balancingConfig)
    val shipFormulas = ShipFormulasImpl(resourceFormulas, balancingConfig)
    val locationFormulas = LocationFormulasImpl(gameConfig.universeSize)

    val roundService = RoundServiceImpl(roundRepository)
    val eventService = EventServiceImpl(uuidFactory, roundService, eventRepository)
    val playerService = PlayerServiceImpl(uuidFactory, playerRepository)
    val planetService = PlanetServiceImpl(uuidFactory, randomNumberGenerator, planetRepository, gameConfig, buildingFormulas, hangarRepository)
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
    val flightService = FlightServiceImpl(gameConfig, roundService, uuidFactory, flightRepository, shipFormulas, locationFormulas, shipService, colonizeFlightHandler, attackFlightHandler, transferFlightHandler, transportFlightHandler, planetService, buildingService, buildingFormulas, detectedFlightRepository, eventService)
    val tournamentService = buildTournamentService(commandLine, roundService)
    val pointsService = PointsServiceImpl(roundService, playerService, planetService, shipService, shipFormulas, pointsRepository, uuidFactory, flightService)
    val adminService = AdminServiceImpl(gameConfig)

    val clock = ClockImpl(planetService, resourceService, buildingService, lockService, roundService, shipService, flightService, pointsService, gameConfig)

    val validatorFactory = Validation.buildDefaultValidatorFactory()
    val playerController = PlayerController(validatorFactory, playerService, planetService, buildingService)
    val planetController = PlanetController(playerService, planetService)
    val buildingController = BuildingController(validatorFactory, playerService, planetService, buildingService)
    val constructionSiteController = ConstructionSiteController(validatorFactory, playerService, planetService, buildingService)
    val shipController = ShipController(validatorFactory, playerService, planetService, shipService)
    val shipyardController = ShipyardController(validatorFactory, playerService, planetService, shipService)
    val applicationInformationController = ApplicationInformationController(applicationInformationService)
    val configurationController = ConfigurationController(gameConfig)
    val roundController = RoundController(roundService, gameConfig)
    val flightController = FlightController(validatorFactory, playerService, planetService, flightService)
    val telescopeController = TelescopeController(validatorFactory, playerService, planetService, buildingService)
    val fightController = FightController(validatorFactory, playerService, planetService, fightService)
    val shipMetadataController = ShipMetadataController(shipFormulas)
    val buildingMetadataController = BuildingMetadataController(buildingFormulas)
    val tournamentController = TournamentController(tournamentService)
    val pointsController = PointsController(pointsService)
    val detectedFlightController = DetectedFlightController(flightService, playerService)
    val eventController = EventController(validatorFactory, playerService, planetService, eventService)
    val metricController = MetricController(metricRegistry, adminService)

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
    enableCors()

    Spark.awaitInitialization()

    roundService.initialize()
    startClock(clock, gameConfig)

    logger.info("RESTwars started on port {}", port)
}

private fun enableCors() {
    Spark.before(Filter { request, response ->
        val headers = request.headers("Access-Control-Request-Headers")
        if (headers != null) response.header("Access-Control-Allow-Headers", headers)

        val method = request.headers("Access-Control-Request-Method")
        if (method != null) response.header("Access-Control-Allow-Methods", method)

        val origin = request.headers("Origin")
        if (origin == null) response.header("Access-Control-Allow-Origin", "*") else response.header("Access-Control-Allow-Origin", origin)

        response.header("Access-Control-Allow-Credentials", "true")
    })
}

private fun createJooq(dataSource: DataSource, gameConfig: GameConfig): DSLContext {
    val dialect = SQLDialect.valueOf(gameConfig.database.dialect)
    return DSL.using(dataSource, dialect)
}

private fun connectToDatabase(gameConfig: GameConfig): DataSource {
    val pool = ComboPooledDataSource()
    pool.driverClass = gameConfig.database.driver.name
    pool.jdbcUrl = gameConfig.database.url
    pool.user = gameConfig.database.username
    pool.password = gameConfig.database.password

    return pool
}

private fun buildTournamentService(commandLine: CommandLine, roundService: RoundServiceImpl): TournamentService {
    if (commandLine.startRound == null) return NoopTournamentService

    logger.info("Tournament mode active. Only accepting requests on round {}", commandLine.startRound)

    val tournamentService = TournamentServiceImpl
    // Add round listener which watches the clock to start the tournament
    roundService.addRoundListener(TournamentRoundListener(roundService, tournamentService, commandLine.startRound))
    return tournamentService
}

data class CommandLine(val startRound: Long?, val configFile: String?, val balancingFile: String?) {
    companion object {
        fun parse(args: Array<String>): CommandLine {
            // TODO: Use something like Commons CLI for commandline parsing

            val tournamentIndex = args.indexOf("--tournament")
            val tournamentRound = if (tournamentIndex > -1) {
                args[tournamentIndex + 1].toLong()
            } else null

            val gameConfigFileIndex = args.indexOf("--config")
            val gameConfigFile = if (gameConfigFileIndex > -1) {
                args[gameConfigFileIndex + 1]
            } else null

            val balancingConfigFileIndex = args.indexOf("--balancing")
            val balancingConfigFile = if (balancingConfigFileIndex > -1) {
                args[gameConfigFileIndex + 1]
            } else null

            return CommandLine(tournamentRound, gameConfigFile, balancingConfigFile)
        }
    }
}

fun registerWebsockets(roundService: RoundService, tournamentService: TournamentService) {
    RoundWebsocketController.roundService = roundService
    Spark.webSocket("/v1/round/websocket", RoundWebsocketController::class.java)

    TournamentWebsocketController.tournamentService = tournamentService
    Spark.webSocket("/v1/tournament/websocket", TournamentWebsocketController::class.java)
}

private fun startClock(clock: Clock, gameConfig: GameConfig) {
    val executor = Executors.newSingleThreadScheduledExecutor { runnable -> Thread(runnable, "Clock") }
    executor.scheduleAtFixedRate({
        clock.tick()
    }, gameConfig.roundTime.toLong(), gameConfig.roundTime.toLong(), TimeUnit.SECONDS)
}

private fun loadGameConfig(configFile: String?): GameConfig {
    val effectiveConfigFile = Paths.get(configFile ?: "config.yaml")
    if (!Files.exists(effectiveConfigFile)) {
        logger.warn("No game config file at ${effectiveConfigFile.toAbsolutePath()} found, using default values")
        return GameConfig(
                UniverseSize(1, 3, 3), StarterPlanet(Resources(200, 100, 800)), NewPlanet(Resources(100, 50, 400)),
                5, 50, Admin("admin", "admin"), Database("jdbc:h2:./data/restwars", Driver::class.java, "", "", "H2")
        )
    }

    logger.info("Loading config from file ${effectiveConfigFile.toAbsolutePath()}")
    return GameConfig.loadFromFile(effectiveConfigFile)
}

private fun loadBalancingConfig(configFile: String?): BalancingConfig {
    val effectiveConfigFile = Paths.get(configFile ?: "balancing-config.yaml")
    if (!Files.exists(effectiveConfigFile)) {
        logger.warn("No balance config file at ${effectiveConfigFile.toAbsolutePath()} found, using default values")
        return (BalancingConfig(
                BuildingsProperties(
                        commandCenterProperties = BuildingProperties(BuildingBuildTime(50, 25), BuildingBuildCost(Resources(200, 100, 800), Resources(100, 50, 400))),
                        crystalMineProperties = BuildingProperties(BuildingBuildTime(30, 10), BuildingBuildCost(Resources(100, 50, 400), Resources(50, 25, 200))),
                        gasRefineryProperties = BuildingProperties(BuildingBuildTime(30, 10), BuildingBuildCost(Resources(100, 50, 400), Resources(50, 25, 200))),
                        solarPanelsProperties = BuildingProperties(BuildingBuildTime(30, 10), BuildingBuildCost(Resources(100, 50, 400), Resources(50, 25, 200))),
                        telescopeProperties = BuildingProperties(BuildingBuildTime(50, 10), BuildingBuildCost(Resources(100, 50, 400), Resources(50, 25, 200))),
                        shipyardProperties = BuildingProperties(BuildingBuildTime(100, 50), BuildingBuildCost(Resources(300, 150, 1200), Resources(150, 75, 600))),
                        buildTimeAttenuation = 0.05),
                ShipsProperties(
                        mosquitoProperties = ShipProperties(10, Resources(100, 20, 270), 1.0, 1.0, 14, 10, 10),
                        colonyProperties = ShipProperties(60, Resources(350, 150, 1750), 0.5, 2.0, 0, 75, 500),
                        muleProperties = ShipProperties(20, Resources(200, 100, 1225), 1.0, 1.5, 0, 20, 750),
                        buildTimeAttenuation = 0.05),
                ScoringProperties(4L, 8L))
                )
    }

    logger.info("Loading balancing config from file ${effectiveConfigFile.toAbsolutePath()}")
    return BalancingConfig.loadFromFile(effectiveConfigFile)
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

    val route = Route { request, response ->
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
    Spark.exception(ValidationException::class.java) { _, _, res ->
        res.status(StatusCode.BAD_REQUEST)
        res.body(Json.toJson(res, ErrorResponse(ErrorReason.REQUEST_VALIDATION_FAILED.name, "Request validation failed")))
    }

    Spark.exception(AuthenticationException::class.java) { _, _, res ->
        res.status(StatusCode.UNAUTHORIZED)
        res.body(Json.toJson(res, ErrorResponse(ErrorReason.INVALID_CREDENTIALS.name, "Invalid credentials")))
    }

    Spark.exception(PlanetNotFoundOrOwnedException::class.java) { e, _, res ->
        res.status(StatusCode.NOT_FOUND)
        res.body(Json.toJson(res, ErrorResponse(ErrorReason.PLANET_NOT_FOUND.name, e.message ?: "")))
    }

    Spark.exception(BadRequestException::class.java) { e, _, res ->
        e as BadRequestException

        res.status(StatusCode.BAD_REQUEST)
        res.body(Json.toJson(res, e.response))
    }

    Spark.exception(JsonParseException::class.java) { e, _, res ->
        res.status(StatusCode.UNPROCESSABLE_ENTITY)
        res.body(Json.toJson(res, ErrorResponse(ErrorReason.UNPROCESSABLE_ENTITY.name, e.message ?: "")))
    }

    Spark.exception(TournamentNotStartedException::class.java) { e, _, res ->
        res.status(StatusCode.SERVICE_UNAVAILABLE)
        res.body(Json.toJson(res, ErrorResponse(ErrorReason.TOURNAMENT_NOT_STARTED.name, e.message ?: "")))
    }

    Spark.exception(StatusCodeException::class.java) { e, _, res ->
        e as StatusCodeException

        res.status(e.statusCode)
        res.body(Json.toJson(res, e.response))
    }

    Spark.exception(Exception::class.java) { e, _, res ->
        val errorUUID = UUID.randomUUID()

        logger.error("Unhandled exception $errorUUID occurred", e)
        res.status(StatusCode.INTERNAL_SERVER_ERROR)
        res.body(Json.toJson(res, ErrorResponse(ErrorReason.INTERNAL_SERVER_ERROR.name, "Error ID: $errorUUID")))
    }
}