package synthesizer.synthesis

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AudioMixerTest {
    private val mixer = AudioMixer()

    @Test
    fun `one channel passes through`() {
        val mixed = mixer.mix(listOf(doubleArrayOf(0.5, -0.5)))
        assertArrayEquals(doubleArrayOf(0.5, -0.5), mixed, 1e-9)
    }

    @Test
    fun `two channels sum simultaneously`() {
        val mixed = mixer.mix(
            listOf(
                doubleArrayOf(0.5, 0.25),
                doubleArrayOf(0.25, 0.5)
            )
        )
        assertArrayEquals(doubleArrayOf(0.75, 0.75), mixed, 1e-9)
    }

    @Test
    fun `different length channels pad with silence`() {
        val mixed = mixer.mix(
            listOf(
                doubleArrayOf(1.0, 0.5),
                doubleArrayOf(0.25)
            )
        )
        assertEquals(2, mixed.size)
        assertEquals(1.0, mixed[0], 1e-9)
        assertEquals(0.4, mixed[1], 1e-9)
    }

    @Test
    fun `normalizes when peak exceeds one`() {
        val mixed = mixer.mix(
            listOf(
                doubleArrayOf(1.0),
                doubleArrayOf(1.0)
            )
        )
        assertEquals(1.0, mixed[0], 1e-9)
    }

    @Test
    fun `empty input returns empty array`() {
        assertEquals(0, mixer.mix(emptyList()).size)
    }
}
