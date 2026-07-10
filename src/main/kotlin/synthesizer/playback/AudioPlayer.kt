package synthesizer.playback

interface AudioPlayer {
    fun play(samples: DoubleArray, sampleRate: Int)
}
