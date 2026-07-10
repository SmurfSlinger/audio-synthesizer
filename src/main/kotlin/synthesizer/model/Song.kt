package synthesizer.model

class Song(
    private val header: SongHeader,
    private val channels: List<ChannelSpec>
) {
    init {
        require(channels.isNotEmpty()) { "Song must contain at least one channel" }
    }

    fun getHeader(): SongHeader = header
    fun getChannels(): List<ChannelSpec> = channels
}
