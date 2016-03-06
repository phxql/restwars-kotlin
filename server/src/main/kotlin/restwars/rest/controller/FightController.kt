package restwars.rest.controller

import restwars.business.fight.FightService
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.*
import restwars.rest.base.ControllerHelper
import restwars.rest.base.Method
import restwars.rest.base.RequestContext
import spark.Request
import spark.Response
import javax.validation.ValidatorFactory

class FightController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val fightService: FightService
) : ControllerHelper {
    fun byPlayer(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)
                val since = req.queryParams("since")?.toLong()

                val fights = fightService.findWithPlayer(context.player, since)

                return FightsResponse(fights.map {
                    val fight = it.fight
                    FightResponse(
                            fight.id, it.attacker.username, it.defender.username, LocationResponse.fromLocation(it.planet.location),
                            ShipsResponse.fromShips(fight.attackerShips), ShipsResponse.fromShips(fight.defenderShips),
                            ShipsResponse.fromShips(fight.remainingAttackerShips), ShipsResponse.fromShips(fight.remainingDefenderShips),
                            ResourcesResponse.fromResources(fight.loot)
                    )
                })
            }
        }
    }

    fun byPlanet(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                val context = RequestContext.build(req, playerService)
                val location = parseLocation(req)
                val since = req.queryParams("since")?.toLong()

                val planet = planetService.findByLocation(location) ?: return FightsResponse(listOf())
                val fights = fightService.findWithPlayerAndPlanet(context.player, planet, since)

                return FightsResponse(fights.map {
                    val fight = it.fight
                    FightResponse(
                            fight.id, it.attacker.username, it.defender.username, LocationResponse.fromLocation(it.planet.location),
                            ShipsResponse.fromShips(fight.attackerShips), ShipsResponse.fromShips(fight.defenderShips),
                            ShipsResponse.fromShips(fight.remainingAttackerShips), ShipsResponse.fromShips(fight.remainingDefenderShips),
                            ResourcesResponse.fromResources(fight.loot)
                    )
                })
            }
        }
    }
}