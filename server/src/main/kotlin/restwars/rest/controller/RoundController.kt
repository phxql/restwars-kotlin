package restwars.rest.controller

import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.slf4j.LoggerFactory
import restwars.business.clock.RoundListener
import restwars.business.clock.RoundService
import restwars.business.config.Config
import restwars.rest.api.Result
import restwars.rest.api.RoundResponse
import restwars.rest.api.RoundWebsocketResponse
import restwars.rest.base.ControllerHelper
import restwars.rest.base.Method
import spark.Request
import spark.Response

class RoundController(
        val roundService: RoundService, val config: Config
) : ControllerHelper {
    fun get(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val currentRound = roundService.currentRound()
                return RoundResponse(currentRound, config.roundTime)
            }
        }
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
                broadcastJson(RoundWebsocketResponse(newRound))
            }
        })
    }

    companion object {
        lateinit var roundService: RoundService
    }
}