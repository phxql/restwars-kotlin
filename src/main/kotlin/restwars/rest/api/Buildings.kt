package restwars.rest.api

import restwars.business.building.Building
import restwars.business.building.ConstructionSite
import java.util.*

data class BuildingResponse(val type: String, val level: Int) {
    companion object {
        fun fromBuilding(building: Building) = BuildingResponse(building.type.name, building.level)
    }
}

data class BuildingsResponse(val buildings: BuildingResponse) {
    companion object {
        fun fromBuildings(buildings: List<Building>) = buildings.map { BuildingResponse.fromBuilding(it) }
    }
}

data class ConstructionSiteResponse(val id: UUID, val type: String, val level: Int, val done: Long) {
    companion object {
        fun fromConstructionSite(constructionSite: ConstructionSite) = ConstructionSiteResponse(constructionSite.id, constructionSite.type.name, constructionSite.level, constructionSite.done)
    }
}

data class ConstructionSitesResponse(val constructionSites: List<ConstructionSiteResponse>) {
    companion object {
        fun fromConstructionSites(constructionSites: List<ConstructionSite>) = constructionSites.map { ConstructionSiteResponse.fromConstructionSite(it) }
    }
}

data class BuildBuildingRequest(
        @get:org.hibernate.validator.constraints.NotBlank
        val type: String
)