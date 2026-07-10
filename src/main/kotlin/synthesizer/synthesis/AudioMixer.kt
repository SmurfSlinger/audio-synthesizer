package synthesizer.synthesis

class AudioMixer {
    fun mix(channelBuffers: List<DoubleArray>): DoubleArray {
        if (channelBuffers.isEmpty()) {
            return DoubleArray(0)
        }

        val outputLength = findLongestLength(channelBuffers)
        if (outputLength == 0) {
            return DoubleArray(0)
        }

        val mixed = DoubleArray(outputLength)
        for (channel in channelBuffers) {
            for (index in channel.indices) {
                mixed[index] += channel[index]
            }
        }

        val peak = mixed.maxOfOrNull { kotlin.math.abs(it) } ?: 0.0
        if (peak > 1.0) {
            for (index in mixed.indices) {
                mixed[index] /= peak
            }
        }

        return mixed
    }

    private fun findLongestLength(buffers: List<DoubleArray>): Int =
        buffers.maxOfOrNull { it.size } ?: 0
}
