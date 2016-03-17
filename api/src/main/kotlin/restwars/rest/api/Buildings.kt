package restwars.rest.api

import java.util.*

data class BuildingResponse(val type: String, val level: Int) : Result {
    companion object {}
}

data class BuildingsResponse(val buildings: List<BuildingResponse>) : Result {
    companion object {}
}

data class ConstructionSiteResponse(val id: UUID, val type: String, val level: Int, val done: Long) : Result {
    companion object {}
}

data class ConstructionSitesResponse(val constructionSites: List<ConstructionSiteResponse>) : Result {
    companion object {}
}

data class BuildBuildingRequest(
        @get:org.hibernate.validator.constraints.NotBlank
        val type: String
)

data class BuildingsMetadataResponse(val data: List<BuildingMetadataResponse>): Result

data class BuildingMetadataResponse(val type: String, val level: Int, val buildTime: Int, val buildCost: ResourcesResponse) : Result