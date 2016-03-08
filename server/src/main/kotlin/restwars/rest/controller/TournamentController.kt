package restwars.rest.controller

import restwars.business.tournament.TournamentService
import restwars.rest.api.Result
import restwars.rest.api.SuccessResponse
import restwars.rest.base.Method
import spark.Request
import spark.Response

class TournamentController(private val tournamentService: TournamentService) {
    fun block(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                tournamentService.blockUntilStart()
                return SuccessResponse("Tournament has started")
            }
        }
    }
}