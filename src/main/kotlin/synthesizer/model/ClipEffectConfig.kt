package synthesizer.model

class ClipEffectConfig(
    private val threshold: Double
) : EffectConfig {
    init {
        require(threshold > 0) { "Clip threshold must be positive, got $threshold" }
    }

    override fun getType(): String = "clip"
    fun getThreshold(): Double = threshold
}
