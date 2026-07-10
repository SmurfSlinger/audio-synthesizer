package synthesizer.effects

import synthesizer.audio.AudioSource
import synthesizer.model.NoteEvent
import synthesizer.model.VolumeEffectConfig

class VolumeDecorator(
    source: AudioSource,
    config: VolumeEffectConfig
) : AudioEffectDecorator(source) {
    private val gain: Double = config.getGain()

    override fun process(
        samples: DoubleArray,
        note: NoteEvent,
        sampleRate: Int,
        tempo: Double
    ): DoubleArray = DoubleArray(samples.size) { index -> samples[index] * gain }
}
