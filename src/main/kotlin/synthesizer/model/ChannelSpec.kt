package synthesizer.model

class ChannelSpec(
    private val waveformType: WaveformType,
    private val effects: List<EffectConfig>,
    private val measures: List<Measure>
) {
    fun getWaveformType(): WaveformType = waveformType
    fun getEffects(): List<EffectConfig> = effects
    fun getMeasures(): List<Measure> = measures
}
