package synthesizer.synthesis

import synthesizer.model.Song

class SongSynthesizer(
    private val pipelineFactory: AudioPipelineFactory,
    private val channelRenderer: ChannelRenderer,
    private val audioMixer: AudioMixer
) {
    fun synthesize(song: Song): DoubleArray {
        val channelBuffers = song.getChannels().map { channel ->
            val pipeline = pipelineFactory.create(channel)
            channelRenderer.render(channel, pipeline, song.getHeader())
        }
        return audioMixer.mix(channelBuffers)
    }
}
