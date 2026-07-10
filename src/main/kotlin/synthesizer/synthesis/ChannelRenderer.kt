package synthesizer.synthesis

import synthesizer.audio.AudioSource
import synthesizer.model.ChannelSpec
import synthesizer.model.SongHeader

class ChannelRenderer {
    fun render(channel: ChannelSpec, pipeline: AudioSource, header: SongHeader): DoubleArray {
        val sampleRate = header.getSampleRate()
        val tempo = header.getTempo()
        val renderedNotes = mutableListOf<DoubleArray>()

        for (measure in channel.getMeasures()) {
            for (note in measure.getNoteEvents()) {
                renderedNotes += if (note.isRest()) {
                    renderSilence(beatsToSampleCount(note.getDurationBeats(), tempo, sampleRate))
                } else {
                    pipeline.renderNote(note, sampleRate, tempo)
                }
            }
        }

        if (renderedNotes.isEmpty()) {
            return DoubleArray(0)
        }

        val totalLength = renderedNotes.sumOf { it.size }
        val output = DoubleArray(totalLength)
        var offset = 0
        for (buffer in renderedNotes) {
            buffer.copyInto(output, offset)
            offset += buffer.size
        }
        return output
    }

    private fun beatsToSampleCount(durationBeats: Double, tempo: Double, sampleRate: Int): Int {
        val secondsPerBeat = 60.0 / tempo
        val durationSeconds = durationBeats * secondsPerBeat
        return maxOf(1, (durationSeconds * sampleRate).toInt())
    }

    private fun renderSilence(sampleCount: Int): DoubleArray = DoubleArray(sampleCount)
}
