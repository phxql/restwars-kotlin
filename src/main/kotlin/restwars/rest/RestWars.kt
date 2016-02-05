package restwars.rest

import org.slf4j.LoggerFactory
import restwars.business.RandomNumberGeneratorImpl
import restwars.business.UUIDFactoryImpl
import restwars.business.config.Config
import restwars.business.config.UniverseSize
import restwars.business.planet.PlanetServiceImpl
import restwars.business.player.PlayerServiceImpl
import restwars.rest.api.ErrorResponse
import restwars.rest.controller.Json
import restwars.rest.controller.PlayerController
import restwars.rest.controller.ValidationException
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

    configureSpark()
    addExceptionHandler()
    registerRoutes(playerController)

    Spark.awaitInitialization()
    logger.info("RESTwars started on port {}", port)
}

private fun loadConfig(): Config {
    return Config(UniverseSize(1, 3, 3)) // TODO: Load config from file
}

private fun configureSpark() {
    Spark.port(port)
}

private fun registerRoutes(playerController: PlayerController) {
    Spark.post("/v1/player", Json.contentType, playerController.create())
}

private fun addExceptionHandler() {
    Spark.exception(ValidationException::class.java, fun(e, req, res) {
        res.status(400)
        res.body(Json.toJson(res, ErrorResponse("Request validation failed")))
    })
}