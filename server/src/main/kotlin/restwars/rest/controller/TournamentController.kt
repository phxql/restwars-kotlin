package restwars.rest.controller

import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.slf4j.LoggerFactory
import restwars.business.tournament.TournamentService
import restwars.business.tournament.TournamentStartListener
import restwars.rest.api.Result
import restwars.rest.api.SuccessResponse
import restwars.rest.base.Method
import spark.Request
import spark.Response

class TournamentController(private val tournamentService: TournamentService) {
    fun wait(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                tournamentService.blockUntilStart()
                return SuccessResponse("Tournament has started")
            }
        }
    }
}

@WebSocket
class TournamentWebsocketController() : AbstractWebsocketController() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val event = SuccessResponse("Tournament has started")

    init {
        // This is a stupid hack as Spark only accepts a Class as parameter and not an instance of the
        // TournamentWebsocketController
        tournamentService.addStartListener(object : TournamentStartListener {
            override fun onStart() {
                logger.debug("Notifying websocket clients")
                broadcastJson(event)
                tournamentService.removeStartListener(this)
            }
        })
    }

    override fun onConnect(session: Session) {
        super.onConnect(session)

        if (tournamentService.hasStarted()) {
            sendJson(event, session)
        }
    }

    companion object {
        lateinit var tournamentService: TournamentService
    }
}