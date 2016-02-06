package restwars.rest.api

import restwars.business.building.Building

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