package restwars.rest.api

data class MetricsResponse(val timer: List<TimerResponse>) : Result {
    companion object {}
}

data class TimerResponse(val name: String, val count: Long, val meanRate: Double, val oneMinuteRate: Double, val fiveMinuteRate: Double, val fifteenMinuteRate: Double, val min: Double, val max: Double, val mean: Double) : Result {
    companion object {}
}