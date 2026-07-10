package synthesizer.app

import synthesizer.parser.SongParser
import synthesizer.playback.JavaSoundPlayer
import synthesizer.synthesis.AudioMixer
import synthesizer.synthesis.AudioPipelineFactory
import synthesizer.synthesis.ChannelRenderer
import synthesizer.synthesis.PitchConverter
import synthesizer.synthesis.SongSynthesizer

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        System.err.println("Usage: audio-synthesizer <song-file>")
        return
    }

    val pitchConverter = PitchConverter()
    val pipelineFactory = AudioPipelineFactory(pitchConverter)
    val synthesizer = SongSynthesizer(
        pipelineFactory,
        ChannelRenderer(),
        AudioMixer()
    )
    val application = SynthesizerApplication(
        SongParser(),
        synthesizer,
        JavaSoundPlayer()
    )

    application.run(args[0])
}
