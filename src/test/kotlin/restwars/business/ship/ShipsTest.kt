package restwars.business.ship

import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Test

class ShipsTest {
    @Test
    fun testGet() {
        val ships = Ships(listOf(Ship(ShipType.MOSQUITO, 1)))
        assertThat(ships[ShipType.MOSQUITO], CoreMatchers.equalTo(1))
    }

    @Test
    fun testGetEmpty() {
        val ships = Ships(listOf())
        assertThat(ships[ShipType.MOSQUITO], CoreMatchers.equalTo(0))
    }

    @Test
    fun testPlus() {
        val ships = Ships(listOf(Ship(ShipType.MOSQUITO, 1)))
        val newShips = ships.with(ShipType.MOSQUITO, 2)
        assertThat(newShips[ShipType.MOSQUITO], CoreMatchers.equalTo(2))
    }

    @Test
    fun testPlusEmpty() {
        val ships = Ships(listOf())
        val newShips = ships.with(ShipType.MOSQUITO, 1)
        assertThat(newShips[ShipType.MOSQUITO], CoreMatchers.equalTo(1))
    }
}