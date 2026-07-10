package synthesizer.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import synthesizer.model.AdsEffectConfig
import synthesizer.model.ClipEffectConfig
import synthesizer.model.TanhEffectConfig
import synthesizer.model.VolumeEffectConfig
import synthesizer.model.WaveformType
import java.nio.file.Files
import java.nio.file.Path

class SongParserTest {
    private val parser = SongParser()

    @Test
    fun `parseHeader valid`() {
        val header = parser.parseHeader("44100 8 280")
        assertEquals(44100, header.getSampleRate())
        assertEquals(8, header.getBeatsPerMeasure())
        assertEquals(280.0, header.getTempo())
    }

    @Test
    fun `parseWaveform accepts sin square saw whitenoise`() {
        assertEquals(WaveformType.SINE, parser.parseWaveform("sin"))
        assertEquals(WaveformType.SQUARE, parser.parseWaveform("square"))
        assertEquals(WaveformType.SAW, parser.parseWaveform("saw"))
        assertEquals(WaveformType.WHITE_NOISE, parser.parseWaveform("whitenoise"))
    }

    @Test
    fun `parseChannel zero effects`() {
        val channel = parser.parseChannel("sin|A4 4", 4)
        assertEquals(WaveformType.SINE, channel.getWaveformType())
        assertTrue(channel.getEffects().isEmpty())
        assertEquals(1, channel.getMeasures().size)
    }

    @Test
    fun `parseChannel one effect`() {
        val channel = parser.parseChannel("""square vol$.8|C4 4""", 4)
        assertEquals(1, channel.getEffects().size)
        assertInstanceOf(VolumeEffectConfig::class.java, channel.getEffects().first())
    }

    @Test
    fun `parseChannel multiple effects in order`() {
        val channel = parser.parseChannel("""sin vol$.8 tanh$5 clip$.7|A4 4""", 4)
        assertEquals(listOf("vol", "tanh", "clip"), channel.getEffects().map { it.getType() })
    }

    @Test
    fun `parseEffect syntaxes`() {
        assertInstanceOf(VolumeEffectConfig::class.java, parser.parseEffect("""vol$.5"""))
        assertInstanceOf(AdsEffectConfig::class.java, parser.parseEffect("""ads$0.1$0.2$.75"""))
        assertInstanceOf(TanhEffectConfig::class.java, parser.parseEffect("""tanh$3"""))
        assertInstanceOf(ClipEffectConfig::class.java, parser.parseEffect("""clip$.9"""))
    }

    @Test
    fun `parseMeasure fractional durations rests sharps flats`() {
        val measure = parser.parseMeasure("A4 1 C5 .5 - .5 F#4 .25 Bb4 .25")
        assertEquals(5, measure.getNoteEvents().size)
        assertTrue(measure.getNoteEvents()[2].isRest())
        assertEquals("F#4", measure.getNoteEvents()[3].getPitch())
        assertEquals("Bb4", measure.getNoteEvents()[4].getPitch())
        assertEquals(0.5, measure.getNoteEvents()[1].getDurationBeats())
    }

    @Test
    fun `parse multiple channels from file`(@TempDir tempDir: Path) {
        val file = tempDir.resolve("song.txt")
        Files.writeString(
            file,
            """
            44100 4 120
            sin|A4 4
            square|C4 4
            """.trimIndent()
        )
        val song = parser.parse(file.toString())
        assertEquals(2, song.getChannels().size)
    }

    @Test
    fun `parse multiple measures`() {
        val channel = parser.parseChannel("saw|A4 4|C5 4", 4)
        assertEquals(2, channel.getMeasures().size)
    }

    @Test
    fun `malformed header`() {
        assertThrows(SongParseException::class.java) { parser.parseHeader("44100 8") }
    }

    @Test
    fun `unknown waveform`() {
        assertThrows(SongParseException::class.java) { parser.parseWaveform("triangle") }
        assertThrows(SongParseException::class.java) { parser.parseWaveform("sqr") }
        assertThrows(SongParseException::class.java) { parser.parseWaveform("noise") }
    }

    @Test
    fun `unknown effect`() {
        assertThrows(SongParseException::class.java) { parser.parseEffect("""echo$1""") }
    }

    @Test
    fun `missing effect arguments`() {
        assertThrows(SongParseException::class.java) { parser.parseEffect("vol") }
        assertThrows(SongParseException::class.java) { parser.parseEffect("""ads$0.1$0.2""") }
    }

    @Test
    fun `invalid numbers`() {
        assertThrows(SongParseException::class.java) { parser.parseHeader("abc 8 120") }
        assertThrows(SongParseException::class.java) { parser.parseEffect("vol" + "$" + "abc") }
        assertThrows(SongParseException::class.java) { parser.parseNoteEvent("A4", "x") }
    }

    @Test
    fun `odd note duration token count`() {
        assertThrows(SongParseException::class.java) { parser.parseMeasure("A4 1 C5") }
    }

    @Test
    fun `invalid pitch`() {
        assertThrows(SongParseException::class.java) { parser.parseNoteEvent("H4", "1") }
    }

    @Test
    fun `invalid duration`() {
        assertThrows(SongParseException::class.java) { parser.parseNoteEvent("A4", "0") }
    }

    @Test
    fun `missing file`(@TempDir tempDir: Path) {
        val missing = tempDir.resolve("missing.txt").toString()
        assertThrows(SongParseException::class.java) { parser.parse(missing) }
    }

    @Test
    fun `validateMeasureDuration accepts exact beatsPerMeasure`() {
        val measure = parser.parseMeasure("A4 4")
        parser.validateMeasureDuration(measure, 4)
    }

    @Test
    fun `validateMeasureDuration accepts fractional durations summing to beatsPerMeasure`() {
        val measure = parser.parseMeasure("A4 1.5 C5 .5 - 1 F4 1")
        parser.validateMeasureDuration(measure, 4)
    }

    @Test
    fun `validateMeasureDuration rejects too few beats`() {
        val measure = parser.parseMeasure("A4 1 C5 1")
        val ex = assertThrows(SongParseException::class.java) {
            parser.validateMeasureDuration(measure, 4)
        }
        assertTrue(ex.message!!.contains("does not match beatsPerMeasure"))
    }

    @Test
    fun `validateMeasureDuration rejects too many beats`() {
        val measure = parser.parseMeasure("A4 3 C5 3")
        val ex = assertThrows(SongParseException::class.java) {
            parser.validateMeasureDuration(measure, 4)
        }
        assertTrue(ex.message!!.contains("does not match beatsPerMeasure"))
    }

    @Test
    fun `parseChannel rejects measure with wrong total duration`() {
        assertThrows(SongParseException::class.java) {
            parser.parseChannel("sin|A4 1 C5 1", 4)
        }
    }
}
