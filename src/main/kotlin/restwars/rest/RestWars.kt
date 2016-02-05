package restwars.rest

import org.slf4j.LoggerFactory
import restwars.business.UUIDFactoryImpl
import restwars.business.player.PlayerServiceImpl
import restwars.rest.api.ErrorResponse
import restwars.rest.controller.Json
import restwars.rest.controller.PlayerController
import restwars.rest.controller.ValidationException
import restwars.storage.InMemoryPlayerRepository
import spark.Spark
import javax.validation.Validation

val port = 7777

val logger = LoggerFactory.getLogger("restwars.rest.RestWars")

fun main(args: Array<String>) {
    val uuidFactory = UUIDFactoryImpl
    val playerRepository = InMemoryPlayerRepository
    val playerService = PlayerServiceImpl(uuidFactory, playerRepository)
    val validatorFactory = Validation.buildDefaultValidatorFactory()

    val playerController = PlayerController(validatorFactory, playerService)

    configureSpark()
    addExceptionHandler()
    registerRoutes(playerController)

    Spark.awaitInitialization()
    logger.info("RESTwars started on port {}", port)
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