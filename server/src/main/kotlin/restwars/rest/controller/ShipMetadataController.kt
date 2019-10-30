package restwars.rest.controller

import restwars.business.ShipFormulas
import restwars.business.ship.ShipType
import restwars.rest.api.ResourcesResponse
import restwars.rest.api.ShipMetadataResponse
import restwars.rest.api.ShipsMetadataResponse
import restwars.rest.api.fromResources
import restwars.rest.base.HttpMethod
import restwars.rest.base.RestMethod
import restwars.rest.base.SimpleRestMethod

class ShipMetadataController(
        private val shipFormulas: ShipFormulas
) {
    fun get(): RestMethod<ShipsMetadataResponse> {
        return SimpleRestMethod(HttpMethod.GET, "/v1/metadata/ship", ShipsMetadataResponse::class.java) { _, _ ->
            ShipsMetadataResponse(ShipType.values().map {
                ShipMetadataResponse(
                        it.name, shipFormulas.calculateBuildTime(it), shipFormulas.calculateFlightSpeed(it),
                        ResourcesResponse.fromResources(shipFormulas.calculateBuildCost(it)),
                        shipFormulas.calculateFlightCostModifier(it), shipFormulas.calculateAttackPoints(it),
                        shipFormulas.calculateDefendPoints(it), shipFormulas.calculateCargoSpace(it)
                )
            })
        }
    }
}