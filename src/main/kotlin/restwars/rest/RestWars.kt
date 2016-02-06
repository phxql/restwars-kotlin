package restwars.rest

import org.slf4j.LoggerFactory
import restwars.business.RandomNumberGeneratorImpl
import restwars.business.UUIDFactoryImpl
import restwars.business.config.Config
import restwars.business.config.StarterPlanet
import restwars.business.config.UniverseSize
import restwars.business.planet.PlanetServiceImpl
import restwars.business.planet.Resources
import restwars.business.player.PlayerServiceImpl
import restwars.rest.api.ErrorResponse
import restwars.rest.controller.*
import restwars.rest.http.StatusCode
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

    val playerService = PlayerServiceImpl(uuidFactory, playerRepository)
    val planetService = PlanetServiceImpl(uuidFactory, randomNumberGenerator, planetRepository, config)
    val validatorFactory = Validation.buildDefaultValidatorFactory()

    val playerController = PlayerController(validatorFactory, playerService, planetService)
    val planetController = PlanetController(playerService, planetService)

    configureSpark()
    addExceptionHandler()
    registerRoutes(playerController, planetController)

    Spark.awaitInitialization()
    logger.info("RESTwars started on port {}", port)
}

private fun loadConfig(): Config {
    return Config(UniverseSize(1, 3, 3), StarterPlanet(Resources(200, 100, 800))) // TODO: Load config from file
}

private fun configureSpark() {
    Spark.port(port)
}

private fun registerRoutes(playerController: PlayerController, planetController: PlanetController) {
    Spark.post("/v1/player", Json.contentType, playerController.create())
    Spark.get("/v1/planet", Json.contentType, planetController.list())
}

private fun addExceptionHandler() {
    Spark.exception(ValidationException::class.java, fun(e, req, res) {
        res.status(StatusCode.badRequest)
        res.body(Json.toJson(res, ErrorResponse("Request validation failed")))
    })

    Spark.exception(AuthenticationException::class.java, fun(e, req, res) {
        res.status(StatusCode.unauthorized)
        res.body(Json.toJson(res, ErrorResponse("Invalid credentials")))
    })
}