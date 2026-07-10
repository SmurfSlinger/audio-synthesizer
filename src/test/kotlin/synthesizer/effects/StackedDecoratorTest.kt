package synthesizer.effects

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import synthesizer.model.ClipEffectConfig
import synthesizer.model.TanhEffectConfig
import synthesizer.model.VolumeEffectConfig
import synthesizer.testutil.FixedAudioSource
import synthesizer.testutil.TestNotes
import kotlin.math.tanh

class StackedDecoratorTest {
    @Test
    fun `stack order volume then tanh then clip`() {
        val source = FixedAudioSource(doubleArrayOf(1.0))
        val pipeline = ClipDecorator(
            TanhDecorator(
                VolumeDecorator(source, VolumeEffectConfig(0.8)),
                TanhEffectConfig(5.0)
            ),
            ClipEffectConfig(0.7)
        )

        val output = pipeline.renderNote(TestNotes.note("A4"), 44100, 120.0)
        val expected = tanh(5.0 * 0.8).coerceIn(-0.7, 0.7)
        assertEquals(expected, output[0], 1e-9)
    }
}
