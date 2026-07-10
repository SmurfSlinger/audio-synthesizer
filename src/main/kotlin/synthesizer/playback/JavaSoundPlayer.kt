package synthesizer.playback

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineUnavailableException
import kotlin.math.roundToInt

class JavaSoundPlayer : AudioPlayer {
    override fun play(samples: DoubleArray, sampleRate: Int) {
        if (samples.isEmpty()) {
            return
        }

        val format = AudioFormat(
            sampleRate.toFloat(),
            BITS_PER_SAMPLE,
            CHANNELS,
            SIGNED,
            BIG_ENDIAN
        )

        val pcmBytes = toPcmBytes(samples)

        try {
            val line = AudioSystem.getSourceDataLine(format)
            line.open(format)
            line.start()
            try {
                line.write(pcmBytes, 0, pcmBytes.size)
                line.drain()
            } finally {
                line.stop()
                line.close()
            }
        } catch (ex: LineUnavailableException) {
            throw IllegalStateException("Unable to open audio output line", ex)
        }
    }

    private fun toPcmBytes(samples: DoubleArray): ByteArray {
        val bytes = ByteArray(samples.size * 2)
        for (index in samples.indices) {
            val clamped = samples[index].coerceIn(-1.0, 1.0)
            val pcm = (clamped * Short.MAX_VALUE).roundToInt().toShort()
            bytes[index * 2] = (pcm.toInt() shr 8).toByte()
            bytes[index * 2 + 1] = pcm.toByte()
        }
        return bytes
    }

    companion object {
        private const val BITS_PER_SAMPLE = 16
        private const val CHANNELS = 1
        private const val SIGNED = true
        private const val BIG_ENDIAN = true
    }
}
