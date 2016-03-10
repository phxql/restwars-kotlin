package restwars.rest.controller

import restwars.business.point.PointsService
import restwars.rest.api.PointsResponse
import restwars.rest.api.Result
import restwars.rest.api.fromPoints
import restwars.rest.base.Method
import spark.Request
import spark.Response

class PointsController(
        private val pointsService: PointsService
) {
    fun get(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val points = pointsService.listMostRecentPoints()
                return PointsResponse.fromPoints(points)
            }
        }
    }
}