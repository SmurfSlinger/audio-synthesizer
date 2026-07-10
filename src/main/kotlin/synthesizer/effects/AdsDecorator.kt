package synthesizer.effects

import synthesizer.audio.AudioSource
import synthesizer.model.AdsEffectConfig
import synthesizer.model.NoteEvent

class AdsDecorator(
    source: AudioSource,
    config: AdsEffectConfig
) : AudioEffectDecorator(source) {
    private val attackEndSeconds: Double = config.getAttackEndSeconds()
    private val decayEndSeconds: Double = config.getDecayEndSeconds()
    private val sustainLevel: Double = config.getSustainLevel()

    override fun process(
        samples: DoubleArray,
        note: NoteEvent,
        sampleRate: Int,
        tempo: Double
    ): DoubleArray {
        return DoubleArray(samples.size) { index ->
            val timeSeconds = index.toDouble() / sampleRate
            samples[index] * computeEnvelope(timeSeconds)
        }
    }

    private fun computeEnvelope(timeSeconds: Double): Double {
        return when {
            timeSeconds < 0.0 -> 0.0
            attackEndSeconds == 0.0 && timeSeconds == 0.0 -> 1.0
            timeSeconds < attackEndSeconds -> timeSeconds / attackEndSeconds
            timeSeconds < decayEndSeconds -> {
                val decaySpan = decayEndSeconds - attackEndSeconds
                if (decaySpan == 0.0) {
                    sustainLevel
                } else {
                    val progress = (timeSeconds - attackEndSeconds) / decaySpan
                    1.0 + progress * (sustainLevel - 1.0)
                }
            }
            else -> sustainLevel
        }
    }
}
