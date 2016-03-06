package restwars.business

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class BuildingFormulasImplTest {
    @Test
    fun testCalculateBuildingBuildTimeModifier() {
        assertThat(BuildingFormulasImpl.calculateBuildingBuildTimeModifier(0), equalTo(1.0))
        assertThat(BuildingFormulasImpl.calculateBuildingBuildTimeModifier(1), equalTo(1.0))
        assertThat(BuildingFormulasImpl.calculateBuildingBuildTimeModifier(2), equalTo(0.95))
        assertThat(BuildingFormulasImpl.calculateBuildingBuildTimeModifier(3), equalTo(0.90))
        assertThat(BuildingFormulasImpl.calculateBuildingBuildTimeModifier(4), equalTo(0.85))
        assertThat(BuildingFormulasImpl.calculateBuildingBuildTimeModifier(5), equalTo(0.80))

        assertThat(BuildingFormulasImpl.calculateBuildingBuildTimeModifier(21), equalTo(0.0))
        assertThat(BuildingFormulasImpl.calculateBuildingBuildTimeModifier(100), equalTo(0.0))
    }
}