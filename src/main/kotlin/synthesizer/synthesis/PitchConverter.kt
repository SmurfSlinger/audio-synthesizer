package synthesizer.synthesis

import synthesizer.parser.SongParseException

class PitchConverter {
    private val noteOffsets = mapOf(
        'C' to 0,
        'D' to 2,
        'E' to 4,
        'F' to 5,
        'G' to 7,
        'A' to 9,
        'B' to 11
    )

    fun toFrequency(pitch: String): Double {
        val trimmed = pitch.trim()
        if (trimmed.isEmpty()) {
            throw SongParseException("Pitch must not be empty")
        }

        val match = PITCH_PATTERN.matchEntire(trimmed)
            ?: throw SongParseException("Invalid pitch notation: $pitch")

        val noteLetter = match.groupValues[1][0].uppercaseChar()
        val accidental = match.groupValues[2]
        val octave = match.groupValues[3].toInt()

        var semitone = noteOffsets[noteLetter]
            ?: throw SongParseException("Invalid pitch notation: $pitch")

        when (accidental) {
            "#" -> semitone += 1
            "b" -> semitone -= 1
        }

        val midiNote = (octave + 1) * 12 + semitone
        return 440.0 * Math.pow(2.0, (midiNote - 69) / 12.0)
    }

    companion object {
        private val PITCH_PATTERN = Regex("^([A-Ga-g])(#{1}|b)?(\\d+)$")
    }
}
