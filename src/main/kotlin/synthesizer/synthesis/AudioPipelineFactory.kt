package synthesizer.synthesis

import synthesizer.audio.AudioSource
import synthesizer.audio.Oscillator
import synthesizer.effects.AdsDecorator
import synthesizer.effects.ClipDecorator
import synthesizer.effects.TanhDecorator
import synthesizer.effects.VolumeDecorator
import synthesizer.model.AdsEffectConfig
import synthesizer.model.ChannelSpec
import synthesizer.model.ClipEffectConfig
import synthesizer.model.EffectConfig
import synthesizer.model.TanhEffectConfig
import synthesizer.model.VolumeEffectConfig
import synthesizer.model.WaveformType
import synthesizer.strategy.SawWaveStrategy
import synthesizer.strategy.SineWaveStrategy
import synthesizer.strategy.SquareWaveStrategy
import synthesizer.strategy.WaveformStrategy
import synthesizer.strategy.WhiteNoiseStrategy

class AudioPipelineFactory(
    private val pitchConverter: PitchConverter
) {
    fun create(channel: ChannelSpec): AudioSource {
        val strategy = createStrategy(channel.getWaveformType())
        var source: AudioSource = Oscillator(strategy, pitchConverter)

        for (effect in channel.getEffects()) {
            source = wrapWithEffect(source, effect)
        }

        return source
    }

    private fun createStrategy(waveformType: WaveformType): WaveformStrategy {
        return when (waveformType) {
            WaveformType.SINE -> SineWaveStrategy()
            WaveformType.SQUARE -> SquareWaveStrategy()
            WaveformType.SAW -> SawWaveStrategy()
            WaveformType.WHITE_NOISE -> WhiteNoiseStrategy()
        }
    }

    private fun wrapWithEffect(source: AudioSource, config: EffectConfig): AudioSource {
        return when (config) {
            is VolumeEffectConfig -> VolumeDecorator(source, config)
            is AdsEffectConfig -> AdsDecorator(source, config)
            is TanhEffectConfig -> TanhDecorator(source, config)
            is ClipEffectConfig -> ClipDecorator(source, config)
            else -> throw IllegalArgumentException("Unsupported effect config: ${config.getType()}")
        }
    }
}
