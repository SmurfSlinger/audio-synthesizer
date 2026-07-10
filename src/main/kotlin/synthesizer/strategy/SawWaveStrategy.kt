package synthesizer.strategy

class SawWaveStrategy : WaveformStrategy {
    override fun generateSample(phase: Double): Double {
        val normalizedPhase = phase - kotlin.math.floor(phase)
        return 2.0 * normalizedPhase - 1.0
    }
}
