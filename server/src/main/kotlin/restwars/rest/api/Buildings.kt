package restwars.rest.api

import restwars.business.building.Building
import restwars.business.building.ConstructionSite

fun BuildingResponse.Companion.fromBuilding(building: Building) = BuildingResponse(building.type.name, building.level)

fun BuildingsResponse.Companion.fromBuildings(buildings: List<Building>) = BuildingsResponse(buildings.map { BuildingResponse.fromBuilding(it) })

fun ConstructionSiteResponse.Companion.fromConstructionSite(constructionSite: ConstructionSite) = ConstructionSiteResponse(constructionSite.id, constructionSite.type.name, constructionSite.level, constructionSite.done)

fun ConstructionSitesResponse.Companion.fromConstructionSites(constructionSites: List<ConstructionSite>) = ConstructionSitesResponse(constructionSites.map { ConstructionSiteResponse.fromConstructionSite(it) })