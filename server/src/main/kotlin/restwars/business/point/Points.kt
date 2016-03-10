package restwars.business.point

import org.slf4j.LoggerFactory
import restwars.business.ShipFormulas
import restwars.business.UUIDFactory
import restwars.business.clock.RoundService
import restwars.business.planet.Planet
import restwars.business.planet.PlanetService
import restwars.business.player.Player
import restwars.business.player.PlayerService
import restwars.business.ship.ShipService
import restwars.business.sumByLong
import java.io.Serializable
import java.util.*

data class Points(val id: UUID, val playerId: UUID, val points: Long, val round: Long) : Serializable

data class PointsWithPlayer(val player: Player, val points: Points)

interface PointsRepository {
    fun insert(points: Points)

    fun listMostRecentPoints(): List<PointsWithPlayer>
}

interface PointsService {
    fun calculatePoints()

    fun listMostRecentPoints(): List<PointsWithPlayer>
}

class PointsServiceImpl(
        private val roundService: RoundService,
        private val playerService: PlayerService,
        private val planetService: PlanetService,
        private val shipService: ShipService,
        private val shipFormulas: ShipFormulas,
        private val pointsRepository: PointsRepository,
        private val uuidFactory: UUIDFactory
) : PointsService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun calculatePoints() {
        logger.info("Calculating points...")

        val planets = planetService.findAllInhabited()
        val pointsByPlayer = mutableMapOf<UUID, Long>()

        for (planet in planets) {
            var points = pointsByPlayer.getOrElse(planet.owner, { 0L })
            points += calculatePointsForPlanet(planet)
            pointsByPlayer[planet.owner] = points
        }

        val round = roundService.currentRound()
        for ((playerId, points) in pointsByPlayer) {
            pointsRepository.insert(Points(uuidFactory.create(), playerId, points, round))
        }

        logger.debug("Done calculating points")
    }

    private fun calculatePointsForPlanet(planet: Planet): Long {
        val ships = shipService.findShipsByPlanet(planet)

        val shipPoints = ships.ships.sumByLong { shipFormulas.calculatePoints(it.type) }
        return shipPoints
    }

    override fun listMostRecentPoints(): List<PointsWithPlayer> {
        return pointsRepository.listMostRecentPoints()
    }
}