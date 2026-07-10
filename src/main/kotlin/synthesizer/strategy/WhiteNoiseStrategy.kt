package synthesizer.strategy

import kotlin.random.Random

class WhiteNoiseStrategy(
    private val random: Random = Random.Default
) : WaveformStrategy {
    override fun generateSample(phase: Double): Double = random.nextDouble() * 2.0 - 1.0
}
