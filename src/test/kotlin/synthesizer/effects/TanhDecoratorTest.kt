package synthesizer.effects

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import synthesizer.model.TanhEffectConfig
import synthesizer.testutil.FixedAudioSource
import synthesizer.testutil.TestNotes
import kotlin.math.tanh

class TanhDecoratorTest {
    @Test
    fun `applies tanh distortion`() {
        val input = 0.5
        val decorator = TanhDecorator(
            FixedAudioSource(doubleArrayOf(input)),
            TanhEffectConfig(5.0)
        )
        val output = decorator.renderNote(TestNotes.note("A4"), 44100, 120.0)
        assertEquals(tanh(5.0 * input), output[0], 1e-9)
    }
}
