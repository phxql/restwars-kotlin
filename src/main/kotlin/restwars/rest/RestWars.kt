package restwars.rest

import org.slf4j.LoggerFactory
import restwars.business.*
import restwars.business.building.BuildingServiceImpl
import restwars.business.clock.Clock
import restwars.business.clock.ClockImpl
import restwars.business.clock.RoundServiceImpl
import restwars.business.config.Config
import restwars.business.config.StarterPlanet
import restwars.business.config.UniverseSize
import restwars.business.planet.PlanetServiceImpl
import restwars.business.planet.Resources
import restwars.business.player.PlayerServiceImpl
import restwars.business.resource.ResourceServiceImpl
import restwars.rest.api.ErrorResponse
import restwars.rest.controller.*
import restwars.rest.http.StatusCode
import restwars.storage.*
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

    val buildingFormula = BuildingFormulasImpl

    val roundService = RoundServiceImpl(roundRepository)
    val playerService = PlayerServiceImpl(uuidFactory, playerRepository)
    val planetService = PlanetServiceImpl(uuidFactory, randomNumberGenerator, planetRepository, config)
    val buildingService = BuildingServiceImpl(uuidFactory, buildingRepository, constructionSiteRepository, buildingFormula, roundService)
    val resourceService = ResourceServiceImpl
    val lockService = LockServiceImpl

    val clock = ClockImpl(planetService, resourceService, buildingService, lockService, roundService)

    val validatorFactory = Validation.buildDefaultValidatorFactory()
    val playerController = PlayerController(validatorFactory, playerService, planetService, buildingService)
    val planetController = PlanetController(playerService, planetService)
    val buildingController = BuildingController(validatorFactory, playerService, planetService, buildingService)
    val constructionSiteController = ConstructionSiteController(validatorFactory, playerService, planetService, buildingService)

    configureSpark()
    addExceptionHandler()
    addLocking(lockService)
    registerRoutes(playerController, planetController, buildingController, constructionSiteController)

    Spark.awaitInitialization()

    startClock(clock, config)
    logger.info("RESTwars started on port {}", port)
}

private fun addLocking(lockService: LockService) {
    Spark.before { request, response -> lockService.beforeRequest() }
    Spark.after { request, response -> lockService.afterRequest() }
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

private fun registerRoutes(playerController: PlayerController, planetController: PlanetController, buildingController: BuildingController, constructionSiteController: ConstructionSiteController) {
    Spark.post("/v1/player", Json.contentType, playerController.create())
    Spark.get("/v1/planet", Json.contentType, planetController.list())
    Spark.get("/v1/planet/:location/building", Json.contentType, buildingController.listOnPlanet())
    Spark.post("/v1/planet/:location/building", Json.contentType, buildingController.build())
    Spark.get("/v1/planet/:location/construction-site", Json.contentType, constructionSiteController.listOnPlanet())
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

    Spark.exception(BadRequestException::class.java, fun(e, req, res) {
        res.status(StatusCode.BAD_REQUEST)
        e as BadRequestException

        res.body(Json.toJson(res, e.response))
    })

}