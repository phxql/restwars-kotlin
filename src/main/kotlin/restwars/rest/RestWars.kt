package restwars.rest

import org.slf4j.LoggerFactory
import restwars.business.RandomNumberGeneratorImpl
import restwars.business.UUIDFactoryImpl
import restwars.business.building.BuildingService
import restwars.business.building.BuildingServiceImpl
import restwars.business.config.Config
import restwars.business.config.StarterPlanet
import restwars.business.config.UniverseSize
import restwars.business.planet.PlanetServiceImpl
import restwars.business.planet.Resources
import restwars.business.player.PlayerServiceImpl
import restwars.rest.api.ErrorResponse
import restwars.rest.controller.*
import restwars.rest.http.StatusCode
import restwars.storage.InMemoryBuildingRepository
import restwars.storage.InMemoryPlanetRepository
import restwars.storage.InMemoryPlayerRepository
import spark.Spark
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

    val playerService = PlayerServiceImpl(uuidFactory, playerRepository)
    val planetService = PlanetServiceImpl(uuidFactory, randomNumberGenerator, planetRepository, config)
    val buildingService = BuildingServiceImpl(uuidFactory, buildingRepository)
    val validatorFactory = Validation.buildDefaultValidatorFactory()

    val playerController = PlayerController(validatorFactory, playerService, planetService, buildingService)
    val planetController = PlanetController(playerService, planetService)
    val buildingController = BuildingController(playerService, planetService, buildingService)

    configureSpark()
    addExceptionHandler()
    registerRoutes(playerController, planetController, buildingController)

    Spark.awaitInitialization()
    logger.info("RESTwars started on port {}", port)
}

private fun loadConfig(): Config {
    return Config(UniverseSize(1, 3, 3), StarterPlanet(Resources(200, 100, 800))) // TODO: Load config from file
}

private fun configureSpark() {
    Spark.port(port)
}

private fun registerRoutes(playerController: PlayerController, planetController: PlanetController, buildingController: BuildingController) {
    Spark.post("/v1/player", Json.contentType, playerController.create())
    Spark.get("/v1/planet", Json.contentType, planetController.list())
    Spark.get("/v1/planet/:location/building", Json.contentType, buildingController.listOnPlanet())
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