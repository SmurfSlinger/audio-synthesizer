package synthesizer.effects

import synthesizer.audio.AudioSource
import synthesizer.model.NoteEvent
import synthesizer.model.TanhEffectConfig
import kotlin.math.tanh

class TanhDecorator(
    source: AudioSource,
    config: TanhEffectConfig
) : AudioEffectDecorator(source) {
    private val drive: Double = config.getDrive()

    override fun process(
        samples: DoubleArray,
        note: NoteEvent,
        sampleRate: Int,
        tempo: Double
    ): DoubleArray = DoubleArray(samples.size) { index -> tanh(drive * samples[index]) }
}
