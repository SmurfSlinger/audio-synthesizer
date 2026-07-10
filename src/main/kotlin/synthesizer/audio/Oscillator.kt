package synthesizer.audio

import synthesizer.model.NoteEvent
import synthesizer.strategy.WaveformStrategy
import synthesizer.synthesis.PitchConverter
import kotlin.math.max

class Oscillator(
    private var waveformStrategy: WaveformStrategy,
    private val pitchConverter: PitchConverter
) : AudioSource {
    private var phase: Double = 0.0

    fun setStrategy(strategy: WaveformStrategy) {
        waveformStrategy = strategy
    }

    override fun renderNote(note: NoteEvent, sampleRate: Int, tempo: Double): DoubleArray {
        if (note.isRest()) {
            return DoubleArray(sampleCountFor(note, sampleRate, tempo))
        }
        return generateRawSamples(note, sampleRate, tempo)
    }

    private fun generateRawSamples(note: NoteEvent, sampleRate: Int, tempo: Double): DoubleArray {
        val sampleCount = sampleCountFor(note, sampleRate, tempo)
        val frequency = pitchConverter.toFrequency(note.getPitch())
        val samples = DoubleArray(sampleCount)
        phase = 0.0

        for (index in 0 until sampleCount) {
            samples[index] = waveformStrategy.generateSample(phase)
            advancePhase(frequency, sampleRate)
        }

        return samples
    }

    private fun advancePhase(frequency: Double, sampleRate: Int) {
        phase += frequency / sampleRate
    }

    private fun sampleCountFor(note: NoteEvent, sampleRate: Int, tempo: Double): Int {
        val secondsPerBeat = 60.0 / tempo
        val durationSeconds = note.getDurationBeats() * secondsPerBeat
        return max(1, (durationSeconds * sampleRate).toInt())
    }
}
