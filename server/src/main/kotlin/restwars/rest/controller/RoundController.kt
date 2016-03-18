package restwars.rest.controller

import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.slf4j.LoggerFactory
import restwars.business.clock.RoundListener
import restwars.business.clock.RoundService
import restwars.business.config.Config
import restwars.rest.api.RoundResponse
import restwars.rest.api.RoundWithRoundTimeResponse
import restwars.rest.base.ControllerHelper
import restwars.rest.base.HttpMethod
import restwars.rest.base.RestMethod
import restwars.rest.base.SimpleRestMethod

class RoundController(
        val roundService: RoundService, val config: Config
) : ControllerHelper {
    fun get(): RestMethod<RoundWithRoundTimeResponse> {
        return SimpleRestMethod(HttpMethod.GET, "/v1/round", RoundWithRoundTimeResponse::class.java, { req, res ->
            val currentRound = roundService.currentRound()
            RoundWithRoundTimeResponse(currentRound, config.roundTime)
        })
    }

    fun wait(): RestMethod<RoundResponse> {
        return SimpleRestMethod(HttpMethod.GET, "/v1/round/wait", RoundResponse::class.java, { req, res ->
            val round = roundService.blockUntilNextRound()
            RoundResponse(round)
        })
    }
}

@WebSocket
class RoundWebsocketController() : AbstractWebsocketController() {
    val logger = LoggerFactory.getLogger(javaClass)

    init {
        // This is a stupid hack as Spark only accepts a Class as parameter and not an instance of the
        // RoundWebsocketController
        roundService.addRoundListener(object : RoundListener {
            override fun onNewRound(newRound: Long) {
                logger.debug("Notifying websocket clients")
                broadcastJson(RoundResponse(newRound))
            }
        })
    }

    companion object {
        lateinit var roundService: RoundService
    }
}