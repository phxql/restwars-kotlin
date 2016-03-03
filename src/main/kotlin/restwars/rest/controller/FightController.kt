package restwars.rest.controller

import restwars.business.fight.FightService
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.FightResponse
import restwars.rest.api.FightsResponse
import restwars.rest.api.LocationResponse
import restwars.rest.api.ShipsResponse
import spark.Route
import javax.validation.ValidatorFactory

class FightController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val fightService: FightService
) : ControllerHelper {
    fun byPlayer(): Route {
        return Route { req, res ->
            val context = RequestContext.build(req, playerService)

            val fights = fightService.findWithPlayer(context.player)

            return@Route Json.toJson(res, FightsResponse(fights.map {
                val fight = it.fight
                FightResponse(
                        fight.id, it.attacker.username, it.defender.username, LocationResponse.fromLocation(it.planet.location),
                        ShipsResponse.fromShips(fight.attackerShips), ShipsResponse.fromShips(fight.defenderShips),
                        ShipsResponse.fromShips(fight.remainingAttackerShips), ShipsResponse.fromShips(fight.remainingDefenderShips)
                )
            }))
        }
    }

    fun byPlanet(): Route {
        return Route { req, res ->
            val context = RequestContext.build(req, playerService)
            val location = parseLocation(req)

            val planet = planetService.findByLocation(location) ?: return@Route Json.toJson(res, FightsResponse(listOf()))
            val fights = fightService.findWithPlayerAndPlanet(context.player, planet)

            return@Route Json.toJson(res, FightsResponse(fights.map {
                val fight = it.fight
                FightResponse(
                        fight.id, it.attacker.username, it.defender.username, LocationResponse.fromLocation(it.planet.location),
                        ShipsResponse.fromShips(fight.attackerShips), ShipsResponse.fromShips(fight.defenderShips),
                        ShipsResponse.fromShips(fight.remainingAttackerShips), ShipsResponse.fromShips(fight.remainingDefenderShips)
                )
            }))
        }

    }
}