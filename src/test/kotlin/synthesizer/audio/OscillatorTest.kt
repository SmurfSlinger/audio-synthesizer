package synthesizer.audio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import synthesizer.strategy.SineWaveStrategy
import synthesizer.strategy.SquareWaveStrategy
import synthesizer.synthesis.PitchConverter
import synthesizer.testutil.TestNotes

class OscillatorTest {
    private val pitchConverter = PitchConverter()

    @Test
    fun `renderNote produces expected sample count`() {
        val oscillator = Oscillator(SineWaveStrategy(), pitchConverter)
        val samples = oscillator.renderNote(TestNotes.note("A4", 1.0), 44100, 120.0)
        val expectedCount = (44100 * (60.0 / 120.0)).toInt()
        assertEquals(expectedCount, samples.size)
    }

    @Test
    fun `renderNote uses selected strategy`() {
        val oscillator = Oscillator(SquareWaveStrategy(), pitchConverter)
        val samples = oscillator.renderNote(TestNotes.note("A4", 0.1), 44100, 120.0)
        assertTrue(samples.all { it == 1.0 || it == -1.0 })
    }

    @Test
    fun `setStrategy changes waveform behavior`() {
        val oscillator = Oscillator(SineWaveStrategy(), pitchConverter)
        val sineFirst = oscillator.renderNote(TestNotes.note("A4", 0.01), 44100, 120.0).first()

        oscillator.setStrategy(SquareWaveStrategy())
        val squareFirst = oscillator.renderNote(TestNotes.note("A4", 0.01), 44100, 120.0).first()

        assertTrue(kotlin.math.abs(sineFirst - squareFirst) > 0.1)
    }
}
