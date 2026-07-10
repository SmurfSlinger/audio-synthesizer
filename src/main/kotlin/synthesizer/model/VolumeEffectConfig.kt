package synthesizer.model

class VolumeEffectConfig(
    private val gain: Double
) : EffectConfig {
    init {
        require(gain >= 0) { "Volume gain must be non-negative, got $gain" }
    }

    override fun getType(): String = "vol"
    fun getGain(): Double = gain
}
