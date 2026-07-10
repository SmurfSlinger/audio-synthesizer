# audio-synthesizer

CS 5700 Assignment 2 — Audio Synthesizer in Kotlin.

## Design

See `CONCEPTUAL_MODEL.md` and `UML.puml` / `UML.png` in the project root.

## Build and test

```bash
./gradlew clean test
```

## Run a song file

```bash
./gradlew run --args="examples/demo.song"
```

## Example song

`examples/demo.song` — two-channel demo with volume, ADS, tanh, and clip effects.
