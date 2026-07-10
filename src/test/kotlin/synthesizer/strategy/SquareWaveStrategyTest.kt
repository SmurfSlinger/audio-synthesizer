package synthesizer.strategy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SquareWaveStrategyTest {
    private val strategy = SquareWaveStrategy()

    @Test
    fun `positive half-cycle`() {
        assertEquals(1.0, strategy.generateSample(0.1))
        assertEquals(1.0, strategy.generateSample(0.49))
    }

    @Test
    fun `negative half-cycle`() {
        assertEquals(-1.0, strategy.generateSample(0.5))
        assertEquals(-1.0, strategy.generateSample(0.9))
    }
}
