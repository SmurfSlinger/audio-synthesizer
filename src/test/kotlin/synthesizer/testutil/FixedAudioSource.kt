package synthesizer.testutil

import synthesizer.audio.AudioSource
import synthesizer.model.NoteEvent

class FixedAudioSource(
    private val samples: DoubleArray
) : AudioSource {
    override fun renderNote(note: NoteEvent, sampleRate: Int, tempo: Double): DoubleArray = samples.copyOf()
}
