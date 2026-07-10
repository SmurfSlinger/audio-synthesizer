package synthesizer.effects

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import synthesizer.model.ClipEffectConfig
import synthesizer.testutil.FixedAudioSource
import synthesizer.testutil.TestNotes

class ClipDecoratorTest {
    @Test
    fun `clamps to threshold`() {
        val decorator = ClipDecorator(
            FixedAudioSource(doubleArrayOf(2.0, -2.0, 0.3)),
            ClipEffectConfig(0.7)
        )
        val output = decorator.renderNote(TestNotes.note("A4"), 44100, 120.0)
        assertEquals(0.7, output[0], 1e-9)
        assertEquals(-0.7, output[1], 1e-9)
        assertEquals(0.3, output[2], 1e-9)
    }
}
