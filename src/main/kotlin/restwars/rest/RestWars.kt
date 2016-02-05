package restwars.rest

import org.slf4j.LoggerFactory
import restwars.business.UUIDFactoryImpl
import restwars.business.user.UserService
import restwars.business.user.UserServiceImpl
import restwars.rest.controller.Json
import restwars.rest.controller.UserController
import restwars.storage.InMemoryUserRepository
import spark.Spark

val port = 7777

val logger = LoggerFactory.getLogger("restwars.rest.RestWars")

fun main(args: Array<String>) {
    val uuidFactory = UUIDFactoryImpl
    val userRepository = InMemoryUserRepository
    val userService = UserServiceImpl(uuidFactory, userRepository)

    val userController = UserController(userService)

    Spark.port(port)

    Spark.post("/v1/user", Json.contentType, userController.create())

    Spark.awaitInitialization()
    logger.info("RESTwars started on port {}", port)
}