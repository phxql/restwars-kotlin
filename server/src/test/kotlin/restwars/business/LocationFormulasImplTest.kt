package restwars.business

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import restwars.business.config.UniverseSize
import restwars.business.planet.Location

class LocationFormulasImplTest {
    private lateinit var sut: LocationFormulas

    @Before
    fun setUp() {
        sut = LocationFormulasImpl(UniverseSize(3, 3, 3))
    }

    @Test
    fun calculateDistance() {
        assertThat(sut.calculateDistance(Location(1, 1, 1), Location(1, 1, 2)), equalTo(1L))
        assertThat(sut.calculateDistance(Location(1, 1, 1), Location(1, 2, 1)), equalTo(3L))
        assertThat(sut.calculateDistance(Location(1, 1, 1), Location(2, 1, 1)), equalTo(9L))

        assertThat(sut.calculateDistance(Location(1, 1, 1), Location(3, 1, 1)), equalTo(18L))
    }
}