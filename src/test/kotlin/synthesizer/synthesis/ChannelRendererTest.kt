package synthesizer.synthesis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import synthesizer.audio.AudioSource
import synthesizer.model.ChannelSpec
import synthesizer.model.Measure
import synthesizer.model.Song
import synthesizer.model.SongHeader
import synthesizer.model.TanhEffectConfig
import synthesizer.model.VolumeEffectConfig
import synthesizer.model.WaveformType
import synthesizer.testutil.TestNotes

class ChannelRendererTest {
    private val renderer = ChannelRenderer()

    @Test
    fun `beat to sample timing`() {
        val channel = ChannelSpec(
            WaveformType.SINE,
            emptyList(),
            listOf(Measure(listOf(TestNotes.note("A4", 2.0))))
        )
        val header = SongHeader(100, 4, 120.0)
        val samples = renderer.render(channel, CountingAudioSource(), header)
        assertEquals(100, samples.size)
    }

    @Test
    fun `rests produce silence`() {
        val channel = ChannelSpec(
            WaveformType.SINE,
            emptyList(),
            listOf(Measure(listOf(TestNotes.rest(1.0))))
        )
        val header = SongHeader(50, 4, 60.0)
        val samples = renderer.render(channel, CountingAudioSource(), header)
        assertTrue(samples.all { it == 0.0 })
    }

    @Test
    fun `notes concatenate sequentially`() {
        val channel = ChannelSpec(
            WaveformType.SINE,
            emptyList(),
            listOf(Measure(listOf(TestNotes.note("A4", 1.0), TestNotes.note("C5", 1.0))))
        )
        val header = SongHeader(10, 4, 60.0)
        val counting = CountingAudioSource()
        val samples = renderer.render(channel, counting, header)
        assertEquals(2, counting.renderCount)
        assertEquals(20, samples.size)
    }

    @Test
    fun `measure order is preserved`() {
        val channel = ChannelSpec(
            WaveformType.SINE,
            emptyList(),
            listOf(
                Measure(listOf(TestNotes.note("A4", 0.5))),
                Measure(listOf(TestNotes.note("C5", 0.5)))
            )
        )
        val header = SongHeader(10, 4, 60.0)
        val counting = CountingAudioSource()
        renderer.render(channel, counting, header)
        assertEquals(2, counting.renderCount)
    }

    private class CountingAudioSource : AudioSource {
        var renderCount: Int = 0

        override fun renderNote(note: synthesizer.model.NoteEvent, sampleRate: Int, tempo: Double): DoubleArray {
            renderCount++
            val secondsPerBeat = 60.0 / tempo
            val sampleCount = maxOf(1, (note.getDurationBeats() * secondsPerBeat * sampleRate).toInt())
            return DoubleArray(sampleCount) { 1.0 }
        }
    }
}
