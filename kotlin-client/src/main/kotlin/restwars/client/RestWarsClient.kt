package restwars.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import feign.Feign
import feign.Headers
import feign.RequestLine
import feign.auth.BasicAuthRequestInterceptor
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import restwars.rest.api.CreatePlayerRequest
import restwars.rest.api.PlanetsResponse

open class RestWarsClient(val baseUrl: String) {
    private val mapper = ObjectMapper().registerModule(KotlinModule())

    private val client: Restwars = feignBuilder()
            .target(Restwars::class.java, baseUrl)

    @Headers("Content-Type: application/json", "Accept: application/json")
    interface Restwars {
        @RequestLine("POST /v1/player")

        fun createPlayer(request: CreatePlayerRequest)

        @RequestLine("GET /v1/planet")
        fun listPlanets(): PlanetsResponse
    }

    fun createPlayer(username: String, password: String) {
        client.createPlayer(CreatePlayerRequest(username, password))
    }

    fun withCredentials(username: String, password: String): AuthorizedRestWarsClient {
        return AuthorizedRestWarsClient(baseUrl, username, password)
    }

    protected fun feignBuilder(): Feign.Builder {
        return Feign.builder()
                .encoder(JacksonEncoder(mapper))
                .decoder(JacksonDecoder(mapper))
                .logger(Slf4jLogger())
    }
}

class AuthorizedRestWarsClient(baseUrl: String, val username: String, val password: String) : RestWarsClient(baseUrl) {
    private val client: Restwars = feignBuilder()
            .requestInterceptor(BasicAuthRequestInterceptor(username, password))
            .target(Restwars::class.java, baseUrl)

    fun listPlanets(): PlanetsResponse = client.listPlanets()
}