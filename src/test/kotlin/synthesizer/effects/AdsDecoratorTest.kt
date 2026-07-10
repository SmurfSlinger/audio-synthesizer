package synthesizer.effects

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import synthesizer.model.AdsEffectConfig
import synthesizer.testutil.FixedAudioSource
import synthesizer.testutil.TestNotes

class AdsDecoratorTest {
    @Test
    fun `attack ramps from zero to one`() {
        val decorator = AdsDecorator(
            FixedAudioSource(DoubleArray(4) { 1.0 }),
            AdsEffectConfig(0.002, 0.004, 0.5)
        )
        val output = decorator.renderNote(TestNotes.note("A4", 0.01), 1000, 120.0)
        assertEquals(0.0, output[0], 1e-9)
        assertEquals(0.5, output[1], 1e-9)
        assertEquals(1.0, output[2], 1e-9)
    }

    @Test
    fun `decay ramps to sustain`() {
        val decorator = AdsDecorator(
            FixedAudioSource(DoubleArray(5) { 1.0 }),
            AdsEffectConfig(0.0, 0.004, 0.25)
        )
        val output = decorator.renderNote(TestNotes.note("A4", 0.01), 1000, 120.0)
        assertEquals(1.0, output[0], 1e-9)
        assertEquals(0.625, output[2], 1e-9)
        assertEquals(0.25, output[4], 1e-9)
    }

    @Test
    fun `sustain holds after decay end`() {
        val decorator = AdsDecorator(
            FixedAudioSource(DoubleArray(6) { 1.0 }),
            AdsEffectConfig(0.0, 0.002, 0.4)
        )
        val output = decorator.renderNote(TestNotes.note("A4", 0.01), 1000, 120.0)
        assertEquals(0.4, output[4], 1e-9)
        assertEquals(0.4, output[5], 1e-9)
    }

    @Test
    fun `boundary at attack end`() {
        val decorator = AdsDecorator(
            FixedAudioSource(doubleArrayOf(1.0, 1.0)),
            AdsEffectConfig(0.001, 0.002, 0.5)
        )
        val output = decorator.renderNote(TestNotes.note("A4"), 1000, 120.0)
        assertEquals(1.0, output[1], 1e-9)
    }
}
