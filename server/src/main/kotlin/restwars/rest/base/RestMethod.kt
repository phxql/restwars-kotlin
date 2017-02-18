package restwars.rest.base

import restwars.business.admin.AdminService
import restwars.business.player.PlayerService
import restwars.rest.api.ErrorReason
import restwars.rest.api.ErrorResponse
import restwars.rest.api.Result
import restwars.rest.http.StatusCode
import spark.Request
import spark.Response
import javax.validation.ValidatorFactory

enum class HttpMethod {
    GET, POST, PUT, DELETE
}

interface RestMethod<T : Result> {
    val responseClass: Class<T>

    val verb: HttpMethod

    val path: String

    fun invoke(req: Request, res: Response): T
}

/**
 * A REST method which allows unauthenticated requests.
 */
class SimpleRestMethod<T : Result>(
        override val verb: HttpMethod,
        override val path: String,
        override val responseClass: Class<T>,
        val func: (res: Request, req: Response) -> T
) : RestMethod<T> {
    override fun invoke(req: Request, res: Response): T = func(req, res)
}

/**
 * A REST method which only allows admin authenticated requests.
 */
class AdminRestMethod<T : Result>(
        override val verb: HttpMethod,
        override val path: String,
        override val responseClass: Class<T>,
        private val adminService: AdminService,
        val func: (res: Request, req: Response) -> T
) : RestMethod<T> {
    override fun invoke(req: Request, res: Response): T {
        val header = req.headers("Authorization") ?: throw AuthenticationException("No Authorization header found")
        val authorization = BasicAuthorization.parse(header)

        if (!adminService.login(authorization.username, authorization.password)) {
            throw StatusCodeException(StatusCode.FORBIDDEN, ErrorResponse(ErrorReason.ADMIN_ACCOUNT_REQUIRED.name, "Admin account required"))
        }

        return func(req, res)
    }
}

/**
 * A REST method which allows unauthenticated requests with a payload.
 */
class PayloadRestMethod<T : Result, U : Any>(
        override val verb: HttpMethod,
        override val path: String,
        override val responseClass: Class<T>,
        private val requestClass: Class<U>,
        private val validatorFactory: ValidatorFactory,
        val func: (res: Request, req: Response, payload: U) -> T
) : RestMethod<T> {
    override fun invoke(req: Request, res: Response): T {
        val payload = validate(validatorFactory, Json.fromJson(req, requestClass))
        return func(req, res, payload)
    }
}

/**
 * A REST method which only allows authenticated requests and does only read operations.
 */
class AuthenticatedRestReadMethod<T : Result>(
        override val verb: HttpMethod,
        override val path: String,
        override val responseClass: Class<T>,
        private val playerService: PlayerService,
        val func: (res: Request, req: Response, context: RequestContext) -> T
) : RestMethod<T> {
    override fun invoke(req: Request, res: Response): T {
        val context = RequestContext.build(req, playerService)
        playerService.beforeReadRequest(context.player)
        try {
            return func(req, res, context)
        } finally {
            playerService.afterReadRequest(context.player)
        }
    }
}

/**
 * A REST method which only allows authenticated requests and does only write operations.
 */
class AuthenticatedRestWriteMethod<T : Result>(
        override val verb: HttpMethod,
        override val path: String,
        override val responseClass: Class<T>,
        private val playerService: PlayerService,
        val func: (res: Request, req: Response, context: RequestContext) -> T
) : RestMethod<T> {
    override fun invoke(req: Request, res: Response): T {
        val context = RequestContext.build(req, playerService)
        playerService.beforeWriteRequest(context.player)
        try {
            return func(req, res, context)
        } finally {
            playerService.afterWriteRequest(context.player)
        }
    }
}

/**
 * A REST method which only allows authenticated requests with a payload and does only write operations.
 */
class AuthenticatedPayloadRestWriteMethod<T : Result, U : Any>(
        override val verb: HttpMethod,
        override val path: String,
        override val responseClass: Class<T>,
        private val requestClass: Class<U>,
        private val playerService: PlayerService,
        private val validatorFactory: ValidatorFactory,
        val func: (res: Request, req: Response, context: RequestContext, payload: U) -> T
) : RestMethod<T> {
    override fun invoke(req: Request, res: Response): T {
        val payload = validate(validatorFactory, Json.fromJson(req, requestClass))
        val context = RequestContext.build(req, playerService)
        playerService.beforeWriteRequest(context.player)
        try {
            return func(req, res, context, payload)
        } finally {
            playerService.afterWriteRequest(context.player)
        }
    }
}

private fun <T> validate(validatorFactory: ValidatorFactory, obj: T?): T {
    obj ?: throw ValidationException("Object is null")

    val validation = validatorFactory.validator.validate(obj)
    if (validation.isNotEmpty()) {
        throw ValidationException("Validation failed")
    }

    return obj
}