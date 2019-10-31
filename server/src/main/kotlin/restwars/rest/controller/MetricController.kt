package restwars.rest.controller

import com.codahale.metrics.MetricRegistry
import restwars.business.admin.AdminService
import restwars.rest.api.MetricsResponse
import restwars.rest.api.TimerResponse
import restwars.rest.api.fromTimer
import restwars.rest.base.AdminRestMethod
import restwars.rest.base.HttpMethod
import restwars.rest.base.RestMethod
import java.util.concurrent.TimeUnit

class MetricController(
        private val metricRegistry: MetricRegistry, private val adminService: AdminService
) {
    // Rates are in x per second
    private val rateUnit = TimeUnit.SECONDS
    // Durations are milliseconds
    private val durationUnit = TimeUnit.MILLISECONDS

    fun all(): RestMethod<MetricsResponse> {
        return AdminRestMethod(HttpMethod.GET, "/v1/admin/metric", MetricsResponse::class.java, adminService) { _, _ ->
            MetricsResponse(
                    metricRegistry.timers.map { TimerResponse.fromTimer(it.key, rateUnit, durationUnit, it.value) }
            )
        }
    }
}