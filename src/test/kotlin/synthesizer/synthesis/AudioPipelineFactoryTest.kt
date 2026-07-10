package synthesizer.synthesis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import synthesizer.model.ChannelSpec
import synthesizer.model.Measure
import synthesizer.model.WaveformType
import synthesizer.testutil.TestNotes

class AudioPipelineFactoryTest {
    private val factory = AudioPipelineFactory(PitchConverter())

    @Test
    fun `selects sine strategy`() {
        val source = factory.create(simpleChannel(WaveformType.SINE))
        assertEquals("synthesizer.audio.Oscillator", source::class.qualifiedName)
    }

    @Test
    fun `selects square strategy`() {
        val samples = factory.create(simpleChannel(WaveformType.SQUARE))
            .renderNote(TestNotes.note("A4", 0.01), 44100, 120.0)
        assertTrue(samples.all { it == 1.0 || it == -1.0 })
    }

  private fun simpleChannel(waveformType: WaveformType): ChannelSpec =
        ChannelSpec(
            waveformType,
            emptyList(),
            listOf(Measure(listOf(TestNotes.note("A4", 1.0))))
        )
}
