package synthesizer.model

class TanhEffectConfig(
    private val drive: Double
) : EffectConfig {
    init {
        require(drive > 0) { "Tanh drive must be positive, got $drive" }
    }

    override fun getType(): String = "tanh"
    fun getDrive(): Double = drive
}
