package restwars.rest.controller

import restwars.business.point.PointsService
import restwars.rest.api.PointsResponse
import restwars.rest.api.fromPoints
import restwars.rest.base.HttpMethod
import restwars.rest.base.RestMethod
import restwars.rest.base.SimpleRestMethod

class PointsController(
        private val pointsService: PointsService
) {
    fun get(): RestMethod<PointsResponse> {
        return SimpleRestMethod(HttpMethod.GET, "/v1/points", PointsResponse::class.java, { req, res ->
            val points = pointsService.listMostRecentPoints()
            PointsResponse.fromPoints(points)
        })
    }
}