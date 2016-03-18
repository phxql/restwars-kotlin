package restwars.rest.controller

import com.codahale.metrics.MetricRegistry
import restwars.rest.api.MetricsResponse
import restwars.rest.api.TimerResponse
import restwars.rest.base.HttpMethod
import restwars.rest.base.RestMethod
import restwars.rest.base.SimpleRestMethod

class MetricController(
        private val metricRegistry: MetricRegistry
) {
    fun all(): RestMethod<MetricsResponse> {
        return SimpleRestMethod(HttpMethod.GET, "/v1/admin/metric", MetricsResponse::class.java, { req, res ->
            MetricsResponse(
                    metricRegistry.timers.map { TimerResponse(it.key, it.value) }
            )
        })
    }
}