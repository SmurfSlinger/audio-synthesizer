package synthesizer.effects

import synthesizer.audio.AudioSource
import synthesizer.model.NoteEvent

abstract class AudioEffectDecorator(
    private val wrappee: AudioSource
) : AudioSource {
    override fun renderNote(note: NoteEvent, sampleRate: Int, tempo: Double): DoubleArray {
        val samples = wrappee.renderNote(note, sampleRate, tempo)
        return process(samples, note, sampleRate, tempo)
    }

    protected abstract fun process(
        samples: DoubleArray,
        note: NoteEvent,
        sampleRate: Int,
        tempo: Double
    ): DoubleArray
}
