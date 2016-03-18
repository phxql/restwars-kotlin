package restwars.rest.base

import restwars.business.player.PlayerService
import restwars.rest.api.Result
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
 * A REST method which only allows authenticated requests.
 */
class AuthenticatedRestMethod<T : Result>(
        override val verb: HttpMethod,
        override val path: String,
        override val responseClass: Class<T>,
        private val playerService: PlayerService,
        val func: (res: Request, req: Response, context: RequestContext) -> T
) : RestMethod<T> {
    override fun invoke(req: Request, res: Response): T {
        val context = RequestContext.build(req, playerService)
        return func(req, res, context)
    }
}

/**
 * A REST method which only allows authenticated requests with a payload.
 */
class AuthenticatedPayloadRestMethod<T : Result, U : Any>(
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
        return func(req, res, context, payload)
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