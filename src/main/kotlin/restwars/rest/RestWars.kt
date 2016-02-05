package restwars.rest

import org.slf4j.LoggerFactory
import restwars.business.UUIDFactoryImpl
import restwars.business.user.UserServiceImpl
import restwars.rest.api.ErrorResponse
import restwars.rest.controller.Json
import restwars.rest.controller.UserController
import restwars.rest.controller.ValidationException
import restwars.storage.InMemoryUserRepository
import spark.Spark
import javax.validation.Validation

val port = 7777

val logger = LoggerFactory.getLogger("restwars.rest.RestWars")

fun main(args: Array<String>) {
    val uuidFactory = UUIDFactoryImpl
    val userRepository = InMemoryUserRepository
    val userService = UserServiceImpl(uuidFactory, userRepository)
    val validatorFactory = Validation.buildDefaultValidatorFactory()

    val userController = UserController(validatorFactory, userService)

    configureSpark()
    addExceptionHandler()
    registerRoutes(userController)

    Spark.awaitInitialization()
    logger.info("RESTwars started on port {}", port)
}

private fun configureSpark() {
    Spark.port(port)
}

private fun registerRoutes(userController: UserController) {
    Spark.post("/v1/user", Json.contentType, userController.create())
}

private fun addExceptionHandler() {
    Spark.exception(ValidationException::class.java, fun(e, req, res) {
        res.status(400)
        res.body(Json.toJson(res, ErrorResponse("Request validation failed")))
    })
}