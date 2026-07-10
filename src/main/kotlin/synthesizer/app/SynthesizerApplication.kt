package synthesizer.app

import synthesizer.parser.SongParseException
import synthesizer.parser.SongParser
import synthesizer.playback.AudioPlayer
import synthesizer.synthesis.SongSynthesizer
import java.nio.file.NoSuchFileException

class SynthesizerApplication(
    private val parser: SongParser,
    private val synthesizer: SongSynthesizer,
    private val player: AudioPlayer
) {
    fun run(filePath: String) {
        try {
            val song = parser.parse(filePath)
            val samples = synthesizer.synthesize(song)
            player.play(samples, song.getHeader().getSampleRate())
        } catch (ex: SongParseException) {
            reportError(ex.message ?: "Failed to parse song file")
        } catch (ex: NoSuchFileException) {
            reportError("Song file not found: $filePath")
        } catch (ex: java.nio.file.AccessDeniedException) {
            reportError("Unable to read song file: $filePath")
        }
    }

    private fun reportError(message: String) {
        System.err.println("Error: $message")
    }
}
