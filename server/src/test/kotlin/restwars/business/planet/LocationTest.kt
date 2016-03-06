package restwars.business.planet

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import restwars.business.config.UniverseSize

class LocationTest {
    @Test
    fun testIsValid() {
        val universeSize = UniverseSize(3, 3, 3)
        assertThat(Location(3, 3, 3).isValid(universeSize), equalTo(true))
        assertThat(Location(3, 3, 4).isValid(universeSize), equalTo(false))
    }
}