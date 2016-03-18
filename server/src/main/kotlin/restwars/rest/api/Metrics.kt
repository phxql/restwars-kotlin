package restwars.rest.api

import com.codahale.metrics.Timer

data class MetricsResponse(val timer: List<TimerResponse>) : Result

data class TimerResponse(val name: String, val timer: Timer) : Result