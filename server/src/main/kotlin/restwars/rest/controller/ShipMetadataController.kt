package restwars.rest.controller

import restwars.business.ShipFormulas
import restwars.business.ship.ShipType
import restwars.rest.api.*
import restwars.rest.base.Method
import spark.Request
import spark.Response

class ShipMetadataController(
        private val shipFormulas: ShipFormulas
) {
    fun get(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                return ShipsMetadata(ShipType.values().map {
                    ShipMetadata(
                            it.name, shipFormulas.calculateBuildTime(it), shipFormulas.calculateFlightSpeed(it),
                            ResourcesResponse.fromResources(shipFormulas.calculateBuildCost(it)),
                            shipFormulas.calculateFlightCostModifier(it), shipFormulas.calculateAttackPoints(it),
                            shipFormulas.calculateDefendPoints(it), shipFormulas.calculateCargoSpace(it)
                    )
                })
            }
        }
    }
}