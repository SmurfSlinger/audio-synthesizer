package synthesizer.strategy

class SquareWaveStrategy : WaveformStrategy {
    override fun generateSample(phase: Double): Double {
        val normalizedPhase = phase - kotlin.math.floor(phase)
        return if (normalizedPhase < 0.5) 1.0 else -1.0
    }
}
