package synthesizer.model

class AdsEffectConfig(
    private val attackEndSeconds: Double,
    private val decayEndSeconds: Double,
    private val sustainLevel: Double
) : EffectConfig {
    init {
        require(attackEndSeconds >= 0) { "attackEndSeconds must be non-negative, got $attackEndSeconds" }
        require(decayEndSeconds >= attackEndSeconds) {
            "decayEndSeconds must be >= attackEndSeconds, got $decayEndSeconds and $attackEndSeconds"
        }
        require(sustainLevel in 0.0..1.0) { "sustainLevel must be in [0, 1], got $sustainLevel" }
    }

    override fun getType(): String = "ads"
    fun getAttackEndSeconds(): Double = attackEndSeconds
    fun getDecayEndSeconds(): Double = decayEndSeconds
    fun getSustainLevel(): Double = sustainLevel
}
