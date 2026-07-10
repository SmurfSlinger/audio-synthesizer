package synthesizer.effects

import synthesizer.audio.AudioSource
import synthesizer.model.ClipEffectConfig
import synthesizer.model.NoteEvent

class ClipDecorator(
    source: AudioSource,
    config: ClipEffectConfig
) : AudioEffectDecorator(source) {
    private val threshold: Double = config.getThreshold()

    override fun process(
        samples: DoubleArray,
        note: NoteEvent,
        sampleRate: Int,
        tempo: Double
    ): DoubleArray = DoubleArray(samples.size) { index ->
        samples[index].coerceIn(-threshold, threshold)
    }
}
