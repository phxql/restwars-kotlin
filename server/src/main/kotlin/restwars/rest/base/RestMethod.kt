package restwars.rest.base

import restwars.business.player.PlayerService
import restwars.rest.api.Result
import spark.Request
import spark.Response

enum class HttpMethod {
    GET, POST, PUT, DELETE
}

interface RestMethod<T : Result> {
    val responseClass: Class<T>

    val verb: HttpMethod

    val path: String

    fun invoke(req: Request, res: Response): T
}

class DefaultRestMethod<T : Result>(
        override val responseClass: Class<T>,
        override val verb: HttpMethod,
        override val path: String,
        val func: (res: Request, req: Response) -> T
) : RestMethod<T> {
    override fun invoke(req: Request, res: Response): T = func(req, res)
}

class AuthenticatedRestMethod<T : Result>(
        override val responseClass: Class<T>,
        override val verb: HttpMethod,
        override val path: String,
        private val playerService: PlayerService,
        val func: (res: Request, req: Response, context: RequestContext) -> T
) : RestMethod<T> {
    override fun invoke(req: Request, res: Response): T {
        val context = RequestContext.build(req, playerService)
        return func(req, res, context)
    }
}