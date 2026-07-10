package synthesizer.audio

import synthesizer.model.NoteEvent

interface AudioSource {
    fun renderNote(note: NoteEvent, sampleRate: Int, tempo: Double): DoubleArray
}
