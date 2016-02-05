package restwars.rest.controller

import com.google.gson.Gson
import spark.Request
import spark.Response

object Json {
    val contentType = "application/json"

    private val gson = Gson()

    fun toJson(response: Response, model: Any): String {
        response.type(contentType)
        return gson.toJson(model)
    }

    fun <T> fromJson(request: Request, clazz: Class<T>): T? {
        return gson.fromJson(request.body(), clazz)
    }
}