package restwars.business.fight

import org.junit.Test
import restwars.business.RandomNumberGeneratorImpl
import restwars.business.ShipFormulasImpl
import restwars.business.UUIDFactoryImpl
import restwars.business.ship.ShipType
import restwars.business.ship.Ships
import java.util.*

class FightCalculatorImplTest {
    @Test
    fun testName() {
        val calculator = FightCalculatorImpl(UUIDFactoryImpl, ShipFormulasImpl, RandomNumberGeneratorImpl)
        val attacker = UUID.randomUUID()
        val defender = UUID.randomUUID()
        val planet = UUID.randomUUID()
        val round = 1L

        val fight = calculator.attack(attacker, defender, planet, Ships.of(ShipType.MOSQUITO, 10), Ships.of(ShipType.COLONY, 1), round)
        println(fight)
    }
}