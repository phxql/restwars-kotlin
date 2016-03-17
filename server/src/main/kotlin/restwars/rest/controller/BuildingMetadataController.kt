package restwars.rest.controller

import restwars.business.BuildingFormulas
import restwars.business.building.BuildingType
import restwars.rest.api.*
import restwars.rest.base.Method
import spark.Request
import spark.Response

class BuildingMetadataController(private val buildingFormulas: BuildingFormulas) {
    fun get(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val level = Math.max(1, req.queryParams("level")?.toInt() ?: 1)

                return BuildingsMetadataResponse(BuildingType.values().map {
                    BuildingMetadataResponse(
                            it.name, level, buildingFormulas.calculateBuildTime(it, level),
                            ResourcesResponse.fromResources(buildingFormulas.calculateBuildCost(it, level))
                    )
                })
            }
        }
    }
}