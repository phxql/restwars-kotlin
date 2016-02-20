package restwars.business.ship

import org.hamcrest.CoreMatchers
import org.junit.Assert.assertThat
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
    fun testWith() {
        val ships = Ships(listOf(Ship(ShipType.MOSQUITO, 1)))
        val newShips = ships.with(ShipType.MOSQUITO, 2)
        assertThat(newShips[ShipType.MOSQUITO], CoreMatchers.equalTo(2))
    }

    @Test
    fun testWithEmpty() {
        val ships = Ships(listOf())
        val newShips = ships.with(ShipType.MOSQUITO, 1)
        assertThat(newShips[ShipType.MOSQUITO], CoreMatchers.equalTo(1))
    }

    @Test
    fun testPlus() {
        val ships = Ships(listOf(Ship(ShipType.MOSQUITO, 1), Ship(ShipType.COLONY, 1)))
        val other = Ships(listOf(Ship(ShipType.MOSQUITO, 2)))

        val sum = ships + other

        assertThat(sum[ShipType.MOSQUITO], CoreMatchers.equalTo(3))
        assertThat(sum[ShipType.COLONY], CoreMatchers.equalTo(1))
    }

    @Test
    fun testPlusEmpty() {
        val ships = Ships.none()
        val other = Ships(listOf(Ship(ShipType.MOSQUITO, 2), Ship(ShipType.COLONY, 1)))

        val sum = ships + other

        assertThat(sum[ShipType.MOSQUITO], CoreMatchers.equalTo(2))
        assertThat(sum[ShipType.COLONY], CoreMatchers.equalTo(1))
    }

    @Test
    fun testMinus() {
        val ships = Ships(listOf(Ship(ShipType.MOSQUITO, 2), Ship(ShipType.COLONY, 1)))
        val other = Ships(listOf(Ship(ShipType.MOSQUITO, 1)))

        val sum = ships - other

        assertThat(sum[ShipType.MOSQUITO], CoreMatchers.equalTo(1))
        assertThat(sum[ShipType.COLONY], CoreMatchers.equalTo(1))
    }

    @Test
    fun testMinusEmpty() {
        val ships = Ships.none()
        val other = Ships(listOf(Ship(ShipType.MOSQUITO, 1)))

        val sum = ships - other

        assertThat(sum[ShipType.MOSQUITO], CoreMatchers.equalTo(0))
    }
}