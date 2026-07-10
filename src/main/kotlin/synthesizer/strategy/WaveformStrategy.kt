package synthesizer.strategy

interface WaveformStrategy {
    fun generateSample(phase: Double): Double
}
