package restwars.rest.controller

import restwars.business.fight.FightService
import restwars.business.planet.PlanetService
import restwars.business.player.PlayerService
import restwars.rest.api.*
import restwars.rest.base.AuthenticatedRestReadMethod
import restwars.rest.base.ControllerHelper
import restwars.rest.base.HttpMethod
import restwars.rest.base.RestMethod
import javax.validation.ValidatorFactory

class FightController(
        val validation: ValidatorFactory,
        val playerService: PlayerService,
        val planetService: PlanetService,
        val fightService: FightService
) : ControllerHelper {
    fun byPlayer(): RestMethod<FightsResponse> {
        return AuthenticatedRestReadMethod(HttpMethod.GET, "/v1/player/fight", FightsResponse::class.java, playerService) { req, _, context ->
            val since = req.queryParams("since")?.toLong()

            val fights = fightService.findWithPlayer(context.player, since)

            FightsResponse(fights.map {
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

    fun byPlanet(): RestMethod<FightsResponse> {
        return AuthenticatedRestReadMethod(HttpMethod.GET, "/v1/planet/:location/fight", FightsResponse::class.java, playerService) { req, _, context ->
            val location = parseLocation(req)
            val since = req.queryParams("since")?.toLong()

            val planet = planetService.findByLocation(location) ?: return@AuthenticatedRestReadMethod FightsResponse(listOf())
            val fights = fightService.findWithPlayerAndPlanet(context.player, planet, since)

            FightsResponse(fights.map {
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