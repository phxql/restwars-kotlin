package restwars.rest.base

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import spark.Request
import spark.Response

object Json {
    val contentType = "application/json"

    private val mapper = ObjectMapper().registerModule(KotlinModule()).enable(SerializationFeature.INDENT_OUTPUT)

    fun toJson(response: Response, model: Any): String {
        response.type(contentType)
        return toJson(model)
    }

    fun toJson(model: Any): String {
        return mapper.writeValueAsString(model)
    }

    fun <T> fromJson(request: Request, clazz: Class<T>): T? {
        return mapper.readValue(request.body(), clazz)
    }
}