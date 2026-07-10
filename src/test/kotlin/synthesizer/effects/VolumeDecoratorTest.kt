package synthesizer.effects

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import synthesizer.model.VolumeEffectConfig
import synthesizer.testutil.FixedAudioSource
import synthesizer.testutil.TestNotes

class VolumeDecoratorTest {
    @Test
    fun `scales samples by gain`() {
        val source = FixedAudioSource(doubleArrayOf(0.5, -1.0))
        val decorator = VolumeDecorator(source, VolumeEffectConfig(0.8))
        val output = decorator.renderNote(TestNotes.note("A4"), 44100, 120.0)
        assertArrayEquals(doubleArrayOf(0.4, -0.8), output, 1e-9)
    }
}
