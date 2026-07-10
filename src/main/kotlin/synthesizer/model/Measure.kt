package synthesizer.model

class Measure(
    private val noteEvents: List<NoteEvent>
) {
    fun getNoteEvents(): List<NoteEvent> = noteEvents
}
