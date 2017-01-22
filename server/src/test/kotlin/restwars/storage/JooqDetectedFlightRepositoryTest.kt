package restwars.storage

import org.hamcrest.CoreMatchers
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import restwars.business.flight.DetectedFlight
import restwars.business.flight.Flight
import restwars.business.flight.FlightDirection
import restwars.business.flight.FlightType
import restwars.business.planet.Location
import restwars.business.planet.Resources
import restwars.business.player.Player
import restwars.business.ship.Ships
import java.util.*

class JooqDetectedFlightRepositoryTest : AbstractJooqTest() {
    private lateinit var sut: JooqDetectedFlightRepository

    @Before override fun setUp() {
        super.setUp()
        sut = JooqDetectedFlightRepository(jooq)
    }

    @Test fun testFindWithPlayer() {
        val playerRepository = JooqPlayerRepository(jooq)
        val playerId = UUID.randomUUID()
        playerRepository.insert(Player(playerId, "", ""))

        val flightId = UUID.randomUUID()
        val flightRepository = JooqFlightRepository(jooq)
        flightRepository.insert(Flight(flightId, playerId, Location(1, 1, 1), Location(2, 2, 2), 1, 2, Ships.none(), FlightDirection.OUTWARD, FlightType.ATTACK, Resources.none(), false, 0.5))

        val id = UUID.randomUUID()
        sut.insert(DetectedFlight(id, flightId, playerId, 1, 1))

        val detectedFlights = sut.findWithPlayer(playerId)

        assertThat(detectedFlights.size, CoreMatchers.equalTo(1))
    }
}