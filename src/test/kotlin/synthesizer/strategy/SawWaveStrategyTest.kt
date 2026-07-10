package synthesizer.strategy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SawWaveStrategyTest {
    private val strategy = SawWaveStrategy()

    @Test
    fun `linear ramp across cycle`() {
        assertEquals(-1.0, strategy.generateSample(0.0), 1e-9)
        assertEquals(0.0, strategy.generateSample(0.5), 1e-9)
        assertEquals(0.998, strategy.generateSample(0.999), 1e-3)
    }

    @Test
    fun `wraps phase across cycles`() {
        assertEquals(strategy.generateSample(0.25), strategy.generateSample(1.25), 1e-9)
    }
}
