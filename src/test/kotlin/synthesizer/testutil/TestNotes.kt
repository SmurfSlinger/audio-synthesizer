package synthesizer.testutil

import synthesizer.model.NoteEvent

object TestNotes {
    fun note(pitch: String, durationBeats: Double = 1.0): NoteEvent =
        NoteEvent(pitch, durationBeats, false)

    fun rest(durationBeats: Double = 1.0): NoteEvent =
        NoteEvent("", durationBeats, true)
}
