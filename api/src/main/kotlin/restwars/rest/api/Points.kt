package restwars.rest.api

data class PointsResponse(val players: List<PointResponse>) : Result {
    companion object {}
}

data class PointResponse(val player: String, val points: Long) : Result {
    companion object {}
}