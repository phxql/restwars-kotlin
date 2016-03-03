package restwars.rest.api

import restwars.business.building.Building
import restwars.business.building.ConstructionSite
import restwars.rest.base.Result
import java.util.*

data class BuildingResponse(val type: String, val level: Int) : Result {
    companion object {
        fun fromBuilding(building: Building) = BuildingResponse(building.type.name, building.level)
    }
}

data class BuildingsResponse(val buildings: List<BuildingResponse>) : Result {
    companion object {
        fun fromBuildings(buildings: List<Building>) = BuildingsResponse(buildings.map { BuildingResponse.fromBuilding(it) })
    }
}

data class ConstructionSiteResponse(val id: UUID, val type: String, val level: Int, val done: Long) : Result {
    companion object {
        fun fromConstructionSite(constructionSite: ConstructionSite) = ConstructionSiteResponse(constructionSite.id, constructionSite.type.name, constructionSite.level, constructionSite.done)
    }
}

data class ConstructionSitesResponse(val constructionSites: List<ConstructionSiteResponse>) : Result {
    companion object {
        fun fromConstructionSites(constructionSites: List<ConstructionSite>) = ConstructionSitesResponse(constructionSites.map { ConstructionSiteResponse.fromConstructionSite(it) })
    }
}

data class BuildBuildingRequest(
        @get:org.hibernate.validator.constraints.NotBlank
        val type: String
)