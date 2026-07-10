package synthesizer.strategy

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class WhiteNoiseStrategyTest {
    @Test
    fun `values stay within range`() {
        val strategy = WhiteNoiseStrategy(Random(42))
        repeat(100) {
            val sample = strategy.generateSample(0.0)
            assertTrue(sample in -1.0..1.0)
        }
    }

    @Test
    fun `output is not constant`() {
        val strategy = WhiteNoiseStrategy(Random(7))
        val samples = List(10) { strategy.generateSample(0.0) }
        assertTrue(samples.distinct().size > 1)
    }

    @Test
    fun `seeded random is deterministic`() {
        val first = WhiteNoiseStrategy(Random(99))
        val second = WhiteNoiseStrategy(Random(99))
        repeat(5) {
            assertTrue(first.generateSample(0.0) == second.generateSample(0.0))
        }
    }
}
