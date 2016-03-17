package restwars.rest.base

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

class RestMethodImpl<T : Result>(
        override val responseClass: Class<T>,
        override val verb: HttpMethod,
        override val path: String,
        val func: (res: Request, req: Response) -> T
) : RestMethod<T> {
    override fun invoke(req: Request, res: Response): T = func(req, res)
}