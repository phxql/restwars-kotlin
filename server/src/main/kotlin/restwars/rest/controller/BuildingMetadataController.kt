package restwars.rest.controller

import restwars.business.BuildingFormulas
import restwars.business.building.BuildingType
import restwars.rest.api.BuildingMetadataResponse
import restwars.rest.api.BuildingsMetadataResponse
import restwars.rest.api.ResourcesResponse
import restwars.rest.api.fromResources
import restwars.rest.base.HttpMethod
import restwars.rest.base.RestMethod
import restwars.rest.base.SimpleRestMethod

class BuildingMetadataController(private val buildingFormulas: BuildingFormulas) {
    fun get(): RestMethod<BuildingsMetadataResponse> {
        return SimpleRestMethod(HttpMethod.GET, "/v1/metadata/building", BuildingsMetadataResponse::class.java) { req, _ ->
            val level = Math.max(1, req.queryParams("level")?.toInt() ?: 1)

            BuildingsMetadataResponse(BuildingType.values().map {
                BuildingMetadataResponse(
                        it.name, level, buildingFormulas.calculateBuildTime(it, level),
                        ResourcesResponse.fromResources(buildingFormulas.calculateBuildCost(it, level))
                )
            })
        }
    }
}