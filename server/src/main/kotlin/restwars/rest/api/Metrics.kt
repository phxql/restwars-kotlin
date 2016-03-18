package restwars.rest.api

import com.codahale.metrics.Timer
import java.util.concurrent.TimeUnit

fun TimerResponse.Companion.fromTimer(name: String, rateUnit: TimeUnit, durationUnit: TimeUnit, timer: Timer): TimerResponse {
    val rateFactor = rateUnit.toSeconds(1)
    val durationFactor = 1.0 / durationUnit.toNanos(1)
    val snapshot = timer.snapshot

    return TimerResponse(
            name, timer.count, timer.meanRate * rateFactor, timer.oneMinuteRate * rateFactor,
            timer.fiveMinuteRate * rateFactor, timer.fifteenMinuteRate * rateFactor, snapshot.min * durationFactor,
            snapshot.max * durationFactor, snapshot.mean * durationFactor
    )
}