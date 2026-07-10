package synthesizer.strategy

import kotlin.math.PI
import kotlin.math.sin

class SineWaveStrategy : WaveformStrategy {
    override fun generateSample(phase: Double): Double = sin(2.0 * PI * phase)
}
