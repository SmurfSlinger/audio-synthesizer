package synthesizer.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import synthesizer.parser.SongParser
import synthesizer.playback.AudioPlayer
import synthesizer.synthesis.AudioMixer
import synthesizer.synthesis.AudioPipelineFactory
import synthesizer.synthesis.ChannelRenderer
import synthesizer.synthesis.PitchConverter
import synthesizer.synthesis.SongSynthesizer
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

class SynthesizerApplicationTest {
    @Test
    fun `run passes synthesized audio to player`(@TempDir tempDir: Path) {
        val songFile = tempDir.resolve("song.txt")
        Files.writeString(
            songFile,
            """
            1000 4 120
            sin|A4 0.1
            """.trimIndent()
        )

        val fakePlayer = RecordingAudioPlayer()
        val application = SynthesizerApplication(
            SongParser(),
            SongSynthesizer(
                AudioPipelineFactory(PitchConverter()),
                ChannelRenderer(),
                AudioMixer()
            ),
            fakePlayer
        )

        application.run(songFile.toString())

        assertTrue(fakePlayer.lastSamples != null)
        assertEquals(1000, fakePlayer.lastSampleRate)
    }

    @Test
    fun `run reports parsing errors gracefully`(@TempDir tempDir: Path) {
        val badFile = tempDir.resolve("bad.txt")
        Files.writeString(badFile, "invalid header")

        val stderr = ByteArrayOutputStream()
        val original = System.err
        System.setErr(PrintStream(stderr))
        try {
            SynthesizerApplication(
                SongParser(),
                SongSynthesizer(
                    AudioPipelineFactory(PitchConverter()),
                    ChannelRenderer(),
                    AudioMixer()
                ),
                RecordingAudioPlayer()
            ).run(badFile.toString())
        } finally {
            System.setErr(original)
        }

        assertTrue(stderr.toString().contains("Error:"))
    }

    private class RecordingAudioPlayer : AudioPlayer {
        var lastSamples: DoubleArray? = null
        var lastSampleRate: Int = 0

        override fun play(samples: DoubleArray, sampleRate: Int) {
            lastSamples = samples
            lastSampleRate = sampleRate
        }
    }
}
