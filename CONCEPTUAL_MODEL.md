# CS 5700 Assignment 2 — Audio Synthesizer Conceptual Model (Revised)

## Overview

The synthesizer reads a structured song input file, parses it into domain objects, builds per-channel audio pipelines (oscillator + stacked effects), renders each channel to a sample buffer, mixes channels, and plays the result. Synthesis logic is independent of playback hardware; the application layer wires parsing, synthesis, and playback together.

## Design Principles

| Principle | How it is applied |
|-----------|-------------------|
| **Abstraction** | `WaveformStrategy`, `AudioSource`, `EffectConfig`, and `AudioPlayer` hide implementation details behind interfaces. |
| **Encapsulation** | All class attributes are private; collaborators interact through public methods only. |
| **Modularity** | Parsing, pipeline construction, per-channel rendering, mixing, playback, and application orchestration live in separate types. |
| **Loose coupling** | `SongSynthesizer` and `SynthesizerApplication` depend on interfaces, not concrete players or waveform classes. |
| **Program to interfaces** | Factories return `AudioSource`; oscillators hold `WaveformStrategy`; playback uses `AudioPlayer`. |

## Input → Output Flow

```
Song file
   → SynthesizerApplication.run(filePath)
        → SongParser.parse() → Song
        → SongSynthesizer.synthesize() → DoubleArray
             → per ChannelSpec:
                  AudioPipelineFactory → AudioSource (Oscillator + decorators)
                  ChannelRenderer → DoubleArray (channel buffer)
             → AudioMixer → DoubleArray (mixed song)
        → AudioPlayer.play(samples, sampleRate)
```

`main` constructs `SynthesizerApplication(parser, synthesizer, JavaSoundPlayer)` and calls `run`.

## Song Header

The file header line contains exactly three values in order:

```
sampleRate beatsPerMeasure tempo
```

`SongHeader` models:

| Field | Type | Role |
|-------|------|------|
| `sampleRate` | `Int` | Samples per second (Hz) |
| `beatsPerMeasure` | `Int` | Expected beat count per measure (validation) |
| `tempo` | `Double` | Beats per minute |

Public getters: `getSampleRate()`, `getBeatsPerMeasure()`, `getTempo()`.

## SongParser

Method signatures align with the file format:

| Method | Role |
|--------|------|
| `parse(filePath: String): Song` | Read file; parse header line, then one channel line per remaining line |
| `parseHeader(line: String): SongHeader` | Parse `sampleRate beatsPerMeasure tempo` |
| `parseChannel(line: String, beatsPerMeasure: Int): ChannelSpec` | Parse one channel line: waveform token, effect tokens, then `\|`-delimited measure segments |
| `parseMeasure(segment: String): Measure` | Parse one measure segment by walking **note token / duration token** pairs |
| `parseNoteEvent(noteToken: String, durationToken: String): NoteEvent` | Build one note or rest from its two tokens |
| `parseEffect(token: String): EffectConfig` | Parse `vol$…`, `ads$…`, `tanh$…`, `clip$…` |
| `parseWaveform(token: String): WaveformType` | Map `sin`, `square`, `saw`, `whitenoise` to enum values |
| `validateMeasureDuration(measure: Measure, beatsPerMeasure: Int): void` | Optional validation against header |

## Measures and Channels

The input format explicitly organizes note sequences into **measures**. After a channel’s waveform and effect settings, each `|`-delimited segment is one measure.

### Measure
- `noteEvents: List<NoteEvent>` — notes and rests within one measure
- `getNoteEvents(): List<NoteEvent>`

### ChannelSpec
- `waveformType: WaveformType`
- `effects: List<EffectConfig>` — **preserves input-file order**
- `measures: List<Measure>` — **preserves measure order**

`SongParser.parseChannel(line, beatsPerMeasure)` parses one channel line (waveform, effects, and `|`-delimited measures). `SongParser.parseMeasure(segment)` produces a `Measure` per `|` segment by consuming **note token / duration token pairs** and calling `parseNoteEvent(noteToken, durationToken)` for each pair. `beatsPerMeasure` from `SongHeader` is passed into channel parsing and may be used to validate that each measure’s total duration matches the expected beat count.

### NoteEvent
Defined by two adjacent tokens in the file: a **note token** (pitch in scientific notation, e.g. `A4`, or a rest marker) and a **duration token** (length in beats). `SongParser.parseNoteEvent(noteToken, durationToken)` constructs the `NoteEvent`.

## WaveformType

Enumeration: `SINE`, `SQUARE`, `SAW`, `WHITE_NOISE`.

`SongParser.parseWaveform(token)` recognizes the exact assignment keywords:

| Input token | `WaveformType` |
|-------------|----------------|
| `sin` | `SINE` |
| `square` | `SQUARE` |
| `saw` | `SAW` |
| `whitenoise` | `WHITE_NOISE` |

## Effect Configuration

Syntax and modeled fields (no release anywhere):

| Input syntax | Config class | Fields |
|--------------|--------------|--------|
| `vol$<gain>` | `VolumeEffectConfig` | `gain: Double` |
| `ads$<attackEnd>$<decayEnd>$<sustain>` | `AdsEffectConfig` | `attackEndSeconds`, `decayEndSeconds`, `sustainLevel` |
| `tanh$<drive>` | `TanhEffectConfig` | `drive: Double` |
| `clip$<threshold>` | `ClipEffectConfig` | `threshold: Double` |

### ADS semantics (per sample, time in seconds)

`AdsDecorator` converts sample index → time via `sampleRate` (from `process` context):

1. **0 … attackEndSeconds** — linear ramp from 0 to 1
2. **attackEndSeconds … decayEndSeconds** — linear ramp from 1 to `sustainLevel`
3. **after decayEndSeconds** — hold at `sustainLevel` for the remainder of the note

There is **no release** parameter.

## Strategy Pattern — Waveform Generation

- **`WaveformStrategy`** — `generateSample(phase: Double): Double`
  - Converts current phase to waveform amplitude only.
  - `WhiteNoiseStrategy` may ignore `phase`.
- **Concrete strategies** — `SineWaveStrategy`, `SquareWaveStrategy`, `SawWaveStrategy`, `WhiteNoiseStrategy`
- **`Oscillator`** — **context**: owns `WaveformStrategy`, `PitchConverter`, frequency (from note pitch), `sampleRate`, and phase advancement; delegates amplitude to the strategy

Runtime selection: `AudioPipelineFactory.createStrategy(waveformType)` chooses the concrete strategy from the parsed channel waveform setting.

## Decorator Pattern — Audio Effects

- **`AudioSource`** — `renderNote(note, sampleRate, tempo): DoubleArray`
- **`Oscillator`** — concrete component (and Strategy context)
- **`AudioEffectDecorator`** — abstract decorator wrapping `AudioSource`

### Template method in `AudioEffectDecorator.renderNote`

1. `samples = wrappee.renderNote(note, sampleRate, tempo)`
2. `return process(samples, note, sampleRate, tempo)`

Subclasses override:

```text
# process(samples: DoubleArray, note: NoteEvent, sampleRate: Int, tempo: Double): DoubleArray
```

- **VolumeDecorator**, **TanhDecorator**, **ClipDecorator** — may ignore `note` / `tempo` (and ADS-irrelevant context).
- **AdsDecorator** — **must** use `sampleRate` to convert sample indices to seconds for envelope timing.

### Effect order (preserved from input file)

For channel settings `sin vol$.8 tanh$5 clip$.7`, `AudioPipelineFactory` wraps in file order:

```text
ClipDecorator(
    TanhDecorator(
        VolumeDecorator(
            Oscillator(SineWaveStrategy)
        )
    )
)
```

**Signal flow per note:** Oscillator → Volume → Tanh → Clip

The first effect in the file is the innermost decorator (closest to the oscillator); the last effect is outermost (first to receive `renderNote` from `ChannelRenderer`).

## Synthesis Subsystem

| Class | Responsibility |
|-------|----------------|
| `AudioPipelineFactory` | Build `Oscillator` + ordered decorators from `ChannelSpec` |
| `PitchConverter` | Scientific pitch → frequency (Hz) |
| `ChannelRenderer` | Iterate `measures` → `noteEvents`; call `pipeline.renderNote`; concatenate |
| `AudioMixer` | Sample-by-sample sum across channel buffers |
| `SongSynthesizer` | `synthesize(song): DoubleArray` — factory, render, mix; no I/O or playback |

## Application and Playback

| Class | Responsibility |
|-------|----------------|
| `SynthesizerApplication` | `run(filePath)`: parse → synthesize → play |
| `AudioPlayer` | Interface: `play(samples, sampleRate)` |
| `JavaSoundPlayer` | Java Sound implementation |

`SynthesizerApplication` depends on `SongParser`, `SongSynthesizer`, and `AudioPlayer` (interface). Unit tests exercise `SongSynthesizer` without `JavaSoundPlayer`.

## Error Handling

| Type | Role |
|------|------|
| `SongParseException` | Domain exception for malformed input or missing/unreadable files |
| `SongParser.parse` | Throws `SongParseException` with helpful messages |
| `SynthesizerApplication.run` | Catches expected file/parsing errors (`SongParseException`, `FileNotFoundException`, etc.); reports a helpful message; does **not** catch programming errors indiscriminately |

## Why ADS Is Per-Note

ADS defines an amplitude envelope over a **single note’s duration**. Each `renderNote` invocation produces one note’s samples; `AdsDecorator.process` applies a fresh envelope using that note’s sample count and `sampleRate` for time conversion. Rests bypass the pipeline (silence from `ChannelRenderer`).

## Class Inventory (UML cross-check)

**Interfaces:** `WaveformStrategy`, `AudioSource`, `AudioPlayer`, `EffectConfig`

**Strategy:** `SineWaveStrategy`, `SquareWaveStrategy`, `SawWaveStrategy`, `WhiteNoiseStrategy`, `Oscillator`

**Decorator:** `AudioEffectDecorator`, `VolumeDecorator`, `AdsDecorator`, `TanhDecorator`, `ClipDecorator`

**Parsing / domain:** `SongParser`, `Song`, `SongHeader`, `ChannelSpec`, `Measure`, `NoteEvent`, `WaveformType`, `VolumeEffectConfig`, `AdsEffectConfig`, `TanhEffectConfig`, `ClipEffectConfig`, `SongParseException`

**Synthesis:** `AudioPipelineFactory`, `PitchConverter`, `ChannelRenderer`, `AudioMixer`, `SongSynthesizer`

**Application / playback:** `SynthesizerApplication`, `JavaSoundPlayer`
