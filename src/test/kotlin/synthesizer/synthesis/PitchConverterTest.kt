package synthesizer.synthesis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import synthesizer.parser.SongParseException

class PitchConverterTest {
    private val converter = PitchConverter()

    @Test
    fun `A4 is 440 Hz`() {
        assertEquals(440.0, converter.toFrequency("A4"), 0.01)
    }

    @Test
    fun `natural note C4`() {
        assertEquals(261.63, converter.toFrequency("C4"), 0.1)
    }

    @Test
    fun `sharp note`() {
        assertEquals(converter.toFrequency("C4") * Math.pow(2.0, 1.0 / 12.0), converter.toFrequency("C#4"), 0.1)
    }

    @Test
    fun `flat note`() {
        assertEquals(converter.toFrequency("A#4"), converter.toFrequency("Bb4"), 0.1)
    }

    @Test
    fun `different octave`() {
        assertEquals(converter.toFrequency("A4") * 2.0, converter.toFrequency("A5"), 0.1)
    }

    @Test
    fun `invalid pitch throws`() {
        assertThrows(SongParseException::class.java) {
            converter.toFrequency("H4")
        }
    }
}
