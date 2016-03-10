package restwars.rest.api

import restwars.business.point.PointsWithPlayer

fun PointsResponse.Companion.fromPoints(points: List<PointsWithPlayer>): PointsResponse {
    return PointsResponse(points.map { PointResponse.fromPoints(it) })
}

fun PointResponse.Companion.fromPoints(points: PointsWithPlayer): PointResponse = PointResponse(points.player.username, points.points.points)