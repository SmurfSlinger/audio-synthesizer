package synthesizer.model

class SongHeader(
    private val sampleRate: Int,
    private val beatsPerMeasure: Int,
    private val tempo: Double
) {
    init {
        require(sampleRate > 0) { "sampleRate must be positive, got $sampleRate" }
        require(beatsPerMeasure > 0) { "beatsPerMeasure must be positive, got $beatsPerMeasure" }
        require(tempo > 0) { "tempo must be positive, got $tempo" }
    }

    fun getSampleRate(): Int = sampleRate
    fun getBeatsPerMeasure(): Int = beatsPerMeasure
    fun getTempo(): Double = tempo
}
