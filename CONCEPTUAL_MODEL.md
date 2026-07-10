# CS 5700 Assignment 2 — Audio Synthesizer Conceptual Model

## Overview

The synthesizer reads a structured song input file, parses it into domain objects, builds per-channel audio pipelines (oscillator + stacked effects), renders each channel to a sample buffer, mixes channels, and optionally plays the result. Synthesis is independent of playback hardware.

## Design Principles

| Principle | How it is applied |
|-----------|-------------------|
| **Abstraction** | `WaveformStrategy`, `AudioSource`, `EffectConfig`, and `AudioPlayer` hide implementation details behind interfaces. |
| **Encapsulation** | All class attributes are private; collaborators interact through public methods only. |
| **Modularity** | Parsing, pipeline construction, per-channel rendering, mixing, and playback live in separate types. |
| **Loose coupling** | High-level orchestration (`SongSynthesizer`) depends on interfaces, not concrete players or waveform classes. |
| **Program to interfaces** | Factories return `AudioSource`; oscillators hold `WaveformStrategy`; playback uses `AudioPlayer`. |

## Input → Output Flow

```
Song file
   → SongParser → Song (header + channels)
   → SongSynthesizer
        → for each ChannelSpec:
              AudioPipelineFactory → AudioSource (Oscillator + decorators)
              ChannelRenderer → DoubleArray (channel buffer)
        → AudioMixer → DoubleArray (mixed song)
   → AudioPlayer.play(mixed buffer)   [optional, separate from synthesis]
```

## Domain Model (Parsing)

### SongHeader
Immutable metadata from the file header: tempo (BPM) and sample rate (Hz).

### ChannelSpec
One instrument track: selected `WaveformType`, ordered list of `EffectConfig`, and ordered sequence of `NoteEvent` (notes and rests).

### NoteEvent
A single timed event on a channel. Carries pitch in scientific notation (e.g. `A4`) or a rest marker, plus duration in beats. Rendering uses tempo to convert beats to sample counts.

### WaveformType
Enumeration mapping input-file waveform keywords to the strategy chosen at runtime (`SINE`, `SQUARE`, `SAW`, `WHITE_NOISE`).

### EffectConfig (abstraction)
Common supertype for effect parameters parsed from the file. Concrete types:

| Class | Role |
|-------|------|
| `VolumeEffectConfig` | Linear gain multiplier |
| `AdsEffectConfig` | Attack, decay, sustain level, release (seconds / level) |
| `TanhEffectConfig` | Soft-clipping drive amount |
| `ClipEffectConfig` | Hard clip threshold |

`SongParser` produces these value objects; `AudioPipelineFactory` maps each to the matching decorator.

## Strategy Pattern — Waveform Generation

- **`WaveformStrategy`** — interface: `generateSample(phase: Double, frequency: Double, sampleRate: Int): Double`
- **Concrete strategies** — `SineWaveStrategy`, `SquareWaveStrategy`, `SawWaveStrategy`, `WhiteNoiseStrategy`
- **`Oscillator`** — context: holds a `WaveformStrategy`, advances phase per sample, delegates sample value to the strategy

The strategy instance is selected when `AudioPipelineFactory` reads `ChannelSpec.waveformType` (from the input file) and is injected into a new `Oscillator`.

## Decorator Pattern — Audio Effects

- **`AudioSource`** — interface: `renderNote(note: NoteEvent, sampleRate: Int, tempo: Double): DoubleArray`
- **`Oscillator`** — concrete component (also the Strategy context)
- **`AudioEffectDecorator`** — abstract decorator: implements `AudioSource`, wraps another `AudioSource`, forwards `renderNote` to the wrappee then post-processes (subclasses override processing)
- **Concrete decorators** — `VolumeDecorator`, `AdsDecorator`, `TanhDecorator`, `ClipDecorator`

Decorators are stacked in **input-file order**: the factory wraps the oscillator with the first effect, then wraps that result with the second, and so on. The outermost decorator is what `ChannelRenderer` calls.

## Synthesis Subsystem

### AudioPipelineFactory
`create(channel: ChannelSpec): AudioSource` — builds `Oscillator` with the correct `WaveformStrategy`, then wraps with decorators derived from `channel.effects` in order.

### PitchConverter
Utility: `toFrequency(pitch: String): Double` — scientific notation → Hz (e.g. `A4` → 440.0). Used by `Oscillator` / `ChannelRenderer` when rendering pitched notes.

### ChannelRenderer
`render(channel: ChannelSpec, pipeline: AudioSource, header: SongHeader): DoubleArray` — walks `noteEvents`, calls `pipeline.renderNote` for each note (or emits silence for rests), concatenates samples using tempo and sample rate.

### AudioMixer
`mix(channels: List<DoubleArray>): DoubleArray` — sample-by-sample sum across channels (with optional normalization left to assignment spec).

### SongSynthesizer
`synthesize(song: Song): DoubleArray` — orchestrates factory, renderer, and mixer; no knowledge of Java Sound or file parsing.

## Playback Subsystem

- **`AudioPlayer`** — `play(samples: DoubleArray, sampleRate: Int)` and/or `play(file: ...)` 
- **`JavaSoundPlayer`** — concrete implementation using `javax.sound.sampled`

Unit tests target `SongSynthesizer` and channel pipelines with in-memory `DoubleArray` output, injecting a test double or skipping `AudioPlayer` entirely.

## Why ADS Is Per-Note

Attack, decay, sustain, and release describe an amplitude envelope over the **duration of a single note**. Each `NoteEvent` has its own start and length, so `AdsDecorator` applies (or resets) the envelope inside `renderNote` for that note only. Stacking ADS at the channel level still means every note passing through the decorator gets a fresh envelope — which matches how synthesizers treat ADSR per note onset.

## Class Inventory (for UML cross-check)

**Interfaces:** `WaveformStrategy`, `AudioSource`, `AudioPlayer`, `EffectConfig`

**Strategy:** `SineWaveStrategy`, `SquareWaveStrategy`, `SawWaveStrategy`, `WhiteNoiseStrategy`, `Oscillator`

**Decorator:** `AudioEffectDecorator`, `VolumeDecorator`, `AdsDecorator`, `TanhDecorator`, `ClipDecorator`

**Parsing / domain:** `SongParser`, `Song`, `SongHeader`, `ChannelSpec`, `NoteEvent`, `WaveformType`, `VolumeEffectConfig`, `AdsEffectConfig`, `TanhEffectConfig`, `ClipEffectConfig`

**Synthesis:** `AudioPipelineFactory`, `PitchConverter`, `ChannelRenderer`, `AudioMixer`, `SongSynthesizer`

**Playback:** `JavaSoundPlayer`
