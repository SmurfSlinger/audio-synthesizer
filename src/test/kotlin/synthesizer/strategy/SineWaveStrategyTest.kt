package synthesizer.strategy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.sin

class SineWaveStrategyTest {
    private val strategy = SineWaveStrategy()

    @Test
    fun `generateSample at zero phase`() {
        assertEquals(0.0, strategy.generateSample(0.0), 1e-9)
    }

    @Test
    fun `generateSample at quarter cycle`() {
        assertEquals(1.0, strategy.generateSample(0.25), 1e-9)
    }

    @Test
    fun `generateSample matches sine formula`() {
        val phase = 0.33
        assertEquals(sin(2.0 * PI * phase), strategy.generateSample(phase), 1e-9)
    }
}
