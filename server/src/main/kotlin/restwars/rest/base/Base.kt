package restwars.rest.base

import restwars.business.planet.Location
import restwars.business.planet.Planet
import restwars.business.planet.PlanetService
import restwars.business.player.Player
import restwars.business.player.PlayerService
import restwars.rest.api.ErrorResponse
import restwars.rest.api.Result
import spark.Request
import spark.Response
import java.util.*
import javax.validation.ValidatorFactory

class ValidationException(message: String) : Exception(message)

class AuthenticationException(message: String) : Exception(message)

class BadRequestException(val response: Any) : Exception("Bad request")

class PlanetNotFoundOrOwnedException(location: Location) : Exception("No planet at $location found")

/**
 * A controller method.
 */
interface Method {
    /**
     * Invokes the method.
     *
     * @param req Request.
     * @param res Response.
     * @return Result.
     */
    fun invoke(req: Request, res: Response): Result
}

interface ControllerHelper {
    fun <T> validate(validatorFactory: ValidatorFactory, obj: T?): T {
        obj ?: throw ValidationException("Object is null")

        val validation = validatorFactory.validator.validate(obj)
        if (validation.isNotEmpty()) {
            throw ValidationException("Validation failed")
        }

        return obj
    }

    fun parseLocation(req: Request, parameter: String = ":location"): Location {
        val locationString = req.params(parameter) ?: throw BadRequestException(ErrorResponse("Path variable $parameter is missing"))
        try {
            return Location.parse(locationString)
        } catch(e: IllegalArgumentException) {
            throw BadRequestException(ErrorResponse(e.message ?: ""))
        }
    }

    fun getOwnPlanet(planetService: PlanetService, player: Player, location: Location): Planet {
        val planet = planetService.findByLocation(location)
        if (planet == null || planet.owner != player.id) {
            throw PlanetNotFoundOrOwnedException(location)
        }
        return planet
    }
}

class RequestContext(val player: Player) {
    companion object {
        fun build(request: Request, playerService: PlayerService): RequestContext {
            val header = request.headers("Authorization") ?: throw AuthenticationException("No Authorization header found")
            val player = extractFromHeader(header, playerService) ?: throw AuthenticationException("Invalid credentials")

            return RequestContext(player)
        }

        /**
         * Extracts the account from the request header.
         *
         * @return Account, if the ticket was valid.
         */
        private fun extractFromHeader(header: String, playerService: PlayerService): Player? {
            val authorization = BasicAuthorization.parse(header)
            return playerService.login(authorization.username, authorization.password)
        }
    }
}

class ParseException(message: String) : Exception(message)

data class BasicAuthorization(val username: String, val password: String) {
    companion object {
        fun parse(header: String): BasicAuthorization {
            val parts = header.split(delimiters = ' ', limit = 2)
            if (parts.size != 2) {
                throw ParseException("Expected 2 parts, found ${parts.size}")
            }
            if (parts[0] != "Basic") {
                throw ParseException("Expected 1st part to be 'Basic', found ${parts[0]}")
            }

            val decoded = String(Base64.getDecoder().decode(parts[1]), Charsets.UTF_8);
            val credentials = decoded.split(delimiters = ':', limit = 2)
            if (credentials.size != 2) {
                throw ParseException("Expected 2 credential parts, found ${credentials.size}")
            }

            return BasicAuthorization(credentials[0], credentials[1])
        }
    }
}