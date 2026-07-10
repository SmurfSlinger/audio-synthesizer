package synthesizer.model

class NoteEvent(
    private val pitch: String,
    private val durationBeats: Double,
    private val isRest: Boolean
) {
    init {
        require(durationBeats > 0) { "Note duration must be positive, got $durationBeats" }
        if (!isRest) {
            require(pitch.isNotBlank()) { "Pitch must not be blank for a note event" }
        }
    }

    fun getPitch(): String = pitch
    fun getDurationBeats(): Double = durationBeats
    fun isRest(): Boolean = isRest
}
