package synthesizer.synthesis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import synthesizer.model.ChannelSpec
import synthesizer.model.ClipEffectConfig
import synthesizer.model.Measure
import synthesizer.model.Song
import synthesizer.model.SongHeader
import synthesizer.model.TanhEffectConfig
import synthesizer.model.VolumeEffectConfig
import synthesizer.model.WaveformType
import synthesizer.testutil.TestNotes

class SongSynthesizerTest {
  private val pitchConverter = PitchConverter()
    private val synthesizer = SongSynthesizer(
        AudioPipelineFactory(pitchConverter),
        ChannelRenderer(),
        AudioMixer()
    )

    @Test
    fun `synthesizes and mixes multiple channels`() {
        val song = Song(
            SongHeader(1000, 4, 120.0),
            listOf(
                ChannelSpec(
                    WaveformType.SINE,
                    emptyList(),
                    listOf(Measure(listOf(TestNotes.note("A4", 0.1))))
                ),
                ChannelSpec(
                    WaveformType.SQUARE,
                    emptyList(),
                    listOf(Measure(listOf(TestNotes.note("C4", 0.1))))
                )
            )
        )

        val mixed = synthesizer.synthesize(song)
        assertTrue(mixed.isNotEmpty())
    }

    @Test
    fun `pipeline factory preserves effect order`() {
        val factory = AudioPipelineFactory(PitchConverter())
        val channel = ChannelSpec(
            WaveformType.SINE,
            listOf(
                VolumeEffectConfig(0.5),
                TanhEffectConfig(2.0),
                ClipEffectConfig(0.9)
            ),
            listOf(Measure(listOf(TestNotes.note("A4", 0.05))))
        )

        val pipeline = factory.create(channel)
        assertEquals("synthesizer.effects.ClipDecorator", pipeline::class.qualifiedName)
    }
}
