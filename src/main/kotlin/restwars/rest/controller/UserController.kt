package restwars.rest.controller

import restwars.business.user.UserService
import restwars.rest.api.CreateUserRequest
import restwars.rest.api.Success
import restwars.rest.http.StatusCode
import spark.Route

class UserController(val userService: UserService) {
    fun create(): Route {
        return Route { request, response ->
            val createUserRequest = Json.fromJson(request, CreateUserRequest::class.java)

            userService.create(createUserRequest.username, createUserRequest.password)

            response.status(StatusCode.created)
            Json.toJson(response, Success("User created"))
        }
    }
}