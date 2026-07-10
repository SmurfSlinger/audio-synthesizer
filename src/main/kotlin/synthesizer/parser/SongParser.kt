package synthesizer.parser

import synthesizer.model.AdsEffectConfig
import synthesizer.model.ChannelSpec
import synthesizer.model.ClipEffectConfig
import synthesizer.model.EffectConfig
import synthesizer.model.Measure
import synthesizer.model.NoteEvent
import synthesizer.model.Song
import synthesizer.model.SongHeader
import synthesizer.model.TanhEffectConfig
import synthesizer.model.VolumeEffectConfig
import synthesizer.model.WaveformType
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

class SongParser {
    fun parse(filePath: String): Song {
        val path = Path.of(filePath)
        if (!Files.exists(path)) {
            throw SongParseException("Song file not found: $filePath")
        }
        if (!path.isRegularFile()) {
            throw SongParseException("Path is not a regular file: $filePath")
        }

        val lines = try {
            path.readText().lines().map { it.trim() }.filter { it.isNotEmpty() }
        } catch (ex: Exception) {
            throw SongParseException("Unable to read song file: $filePath", ex)
        }

        if (lines.isEmpty()) {
            throw SongParseException("Song file is empty: $filePath")
        }

        val header = parseHeader(lines.first())
        val channels = lines.drop(1).mapIndexed { index, line ->
            try {
                parseChannel(line, header.getBeatsPerMeasure())
            } catch (ex: SongParseException) {
                throw SongParseException("Error parsing channel ${index + 1}: ${ex.message}", ex)
            }
        }

        if (channels.isEmpty()) {
            throw SongParseException("Song file must contain at least one channel line")
        }

        return Song(header, channels)
    }

    internal fun parseHeader(line: String): SongHeader {
        val tokens = tokenize(line)
        if (tokens.size != 3) {
            throw SongParseException("Invalid header: expected 'sampleRate beatsPerMeasure tempo', got: $line")
        }

        val sampleRate = parsePositiveInt(tokens[0], "sampleRate")
        val beatsPerMeasure = parsePositiveInt(tokens[1], "beatsPerMeasure")
        val tempo = parsePositiveDouble(tokens[2], "tempo")

        return SongHeader(sampleRate, beatsPerMeasure, tempo)
    }

    internal fun parseChannel(line: String, beatsPerMeasure: Int): ChannelSpec {
        val parts = line.split("|")
        if (parts.isEmpty() || parts[0].isBlank()) {
            throw SongParseException("Channel line must include settings before measures: $line")
        }

        val settingsTokens = tokenize(parts[0])
        if (settingsTokens.isEmpty()) {
            throw SongParseException("Channel settings must begin with a waveform token")
        }

        val waveformType = parseWaveform(settingsTokens[0])
        val effects = settingsTokens.drop(1).map { token ->
            try {
                parseEffect(token)
            } catch (ex: SongParseException) {
                throw SongParseException("Invalid effect token '$token': ${ex.message}", ex)
            }
        }

        if (parts.size < 2) {
            throw SongParseException("Channel must contain at least one measure after settings")
        }

        val measures = parts.drop(1).mapIndexed { index, segment ->
            try {
                parseMeasure(segment)
            } catch (ex: SongParseException) {
                throw SongParseException("Invalid measure ${index + 1}: ${ex.message}", ex)
            }
        }

        measures.forEach { measure ->
            validateMeasureDuration(measure, beatsPerMeasure)
        }

        return ChannelSpec(waveformType, effects, measures)
    }

    internal fun parseMeasure(segment: String): Measure {
        val tokens = tokenize(segment)
        if (tokens.isEmpty()) {
            throw SongParseException("Measure segment is empty")
        }
        if (tokens.size % 2 != 0) {
            throw SongParseException("Measure must contain note/duration pairs, found odd token count: $segment")
        }

        val noteEvents = tokens.chunked(2).map { (noteToken, durationToken) ->
            parseNoteEvent(noteToken, durationToken)
        }

        return Measure(noteEvents)
    }

    internal fun parseNoteEvent(noteToken: String, durationToken: String): NoteEvent {
        val durationBeats = try {
            val value = durationToken.toDouble()
            if (value <= 0) {
                throw SongParseException("Invalid duration '$durationToken': duration must be positive")
            }
            value
        } catch (_: NumberFormatException) {
            throw SongParseException("Invalid duration '$durationToken': expected numeric beat value")
        }

        if (noteToken == "-") {
            return NoteEvent("", durationBeats, true)
        }

        PITCH_PATTERN.matchEntire(noteToken)
            ?: throw SongParseException("Invalid pitch '$noteToken'")

        return NoteEvent(noteToken, durationBeats, false)
    }

    internal fun parseEffect(token: String): EffectConfig {
        val typeEnd = token.indexOf('$')
        if (typeEnd <= 0) {
            throw SongParseException("Effect token must use type\$value syntax: $token")
        }

        val type = token.substring(0, typeEnd)
        val argumentSection = token.substring(typeEnd + 1)

        return when (type) {
            "vol" -> {
                val gain = parseNonNegativeDouble(argumentSection, "vol")
                VolumeEffectConfig(gain)
            }
            "ads" -> {
                val parts = argumentSection.split('$')
                if (parts.size != 3) {
                    throw SongParseException("ads effect requires ads\$attackEnd\$decayEnd\$sustain, got: $token")
                }
                val attackEnd = parseNonNegativeDouble(parts[0], "ads attackEnd")
                val decayEnd = parseNonNegativeDouble(parts[1], "ads decayEnd")
                val sustain = parseDoubleInRange(parts[2], 0.0, 1.0, "ads sustain")
                if (decayEnd < attackEnd) {
                    throw SongParseException("ads decayEnd must be >= attackEnd in token: $token")
                }
                AdsEffectConfig(attackEnd, decayEnd, sustain)
            }
            "tanh" -> {
                val drive = parsePositiveDouble(argumentSection, "tanh drive")
                TanhEffectConfig(drive)
            }
            "clip" -> {
                val threshold = parsePositiveDouble(argumentSection, "clip threshold")
                ClipEffectConfig(threshold)
            }
            else -> throw SongParseException("Unknown effect type: $type")
        }
    }

    internal fun parseWaveform(token: String): WaveformType {
        return when (token) {
            "sin" -> WaveformType.SINE
            "square" -> WaveformType.SQUARE
            "saw" -> WaveformType.SAW
            "whitenoise" -> WaveformType.WHITE_NOISE
            else -> throw SongParseException("Unknown waveform token: $token")
        }
    }

    internal fun validateMeasureDuration(@Suppress("UNUSED_PARAMETER") measure: Measure, @Suppress("UNUSED_PARAMETER") beatsPerMeasure: Int) {
        // beatsPerMeasure is preserved for future validation; not enforced as a hard requirement.
    }

    private fun tokenize(value: String): List<String> =
        value.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

    private fun parsePositiveInt(value: String, fieldName: String): Int {
        val parsed = value.toIntOrNull()
            ?: throw SongParseException("Invalid $fieldName '$value': expected positive integer")
        if (parsed <= 0) {
            throw SongParseException("Invalid $fieldName '$value': must be positive")
        }
        return parsed
    }

    private fun parsePositiveDouble(value: String, fieldName: String): Double {
        val parsed = value.toDoubleOrNull()
            ?: throw SongParseException("Invalid $fieldName '$value': expected positive number")
        if (parsed <= 0) {
            throw SongParseException("Invalid $fieldName '$value': must be positive")
        }
        return parsed
    }

    private fun parseNonNegativeDouble(value: String, fieldName: String): Double {
        val parsed = value.toDoubleOrNull()
            ?: throw SongParseException("Invalid $fieldName '$value': expected non-negative number")
        if (parsed < 0) {
            throw SongParseException("Invalid $fieldName '$value': must be non-negative")
        }
        return parsed
    }

    private fun parseDoubleInRange(value: String, min: Double, max: Double, fieldName: String): Double {
        val parsed = value.toDoubleOrNull()
            ?: throw SongParseException("Invalid $fieldName '$value': expected number")
        if (parsed !in min..max) {
            throw SongParseException("Invalid $fieldName '$value': must be between $min and $max")
        }
        return parsed
    }

    companion object {
        private val PITCH_PATTERN = Regex("^([A-Ga-g])(#{1}|b)?(\\d+)$")
    }
}
