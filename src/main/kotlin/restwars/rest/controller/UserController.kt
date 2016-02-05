package restwars.rest.controller

import restwars.business.user.UserService
import restwars.rest.api.CreateUserRequest
import restwars.rest.api.SuccessResponse
import restwars.rest.http.StatusCode
import spark.Route
import javax.validation.ValidatorFactory

class UserController(val validation: ValidatorFactory, val userService: UserService) : ControllerHelper {
    fun create(): Route {
        return Route { request, response ->
            val createUserRequest = validate(validation, Json.fromJson(request, CreateUserRequest::class.java))

            userService.create(createUserRequest.username, createUserRequest.password)

            response.status(StatusCode.created)
            Json.toJson(response, SuccessResponse("User created"))
        }
    }
}