#!/usr/bin/env python3
"""Synthesizes the Find quiz sounds into app/src/main/res/raw/ as 16-bit mono WAV.

    python3 tools/make-quiz-sounds.py      (needs numpy: pip3 install numpy)

- quiz_success.wav: two bells, G5 then C6 (the owner picked it by ear from several options).
- quiz_fail.wav: two soft wooden knocks on C4, the second a little lower. A mistake should be
  noticeable but never scary, so it is short, low and quieter than the success sound.

Change a number below and run the script again to tune a sound (louder, shorter, higher).
"""
import wave
from pathlib import Path

import numpy as np

RATE = 44100
OUT = Path(__file__).resolve().parent.parent / "app/src/main/res/raw"

# Timbres: (partial frequency ratio, gain, decay multiplier for that partial)
BELL = [(1, 1.0, 1.0), (2.0, 0.35, 0.6), (3.0, 0.12, 0.4), (4.2, 0.06, 0.3)]
MARIMBA = [(1, 1.0, 1.0), (4.0, 0.25, 0.25), (10.0, 0.05, 0.1)]

C4, G5, C6 = 261.63, 783.99, 1046.5


def note(freq, dur, partials, decay, attack=0.004):
    """One struck note: a few partials, a short attack, exponential decay."""
    t = np.arange(int(dur * RATE)) / RATE
    phase = 2 * np.pi * freq * (t + 1 / RATE)
    out = sum(gain * np.sin(phase * ratio) * np.exp(-t / (decay * mult)) for ratio, gain, mult in partials)
    return out * np.minimum(1, t / attack)


def mix(events, total):
    """events: (start in seconds, samples)."""
    buf = np.zeros(int(total * RATE))
    for start, samples in events:
        i = int(start * RATE)
        buf[i:i + len(samples)] += samples[: len(buf) - i]
    return buf


def save(name, x, peak):
    x = x / np.max(np.abs(x)) * peak
    fade = int(0.01 * RATE)  # 10 ms fade-out so the end does not click
    x[-fade:] *= np.linspace(1, 0, fade)
    with wave.open(str(OUT / name), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes((x * 32767).astype(np.int16).tobytes())


OUT.mkdir(parents=True, exist_ok=True)
save("quiz_success.wav", mix([(0.00, note(G5, 0.6, BELL, 0.22)), (0.13, note(C6, 0.9, BELL, 0.35))], 1.05), peak=0.75)
save("quiz_fail.wav", mix([(0.00, note(C4, 0.2, MARIMBA, 0.05, attack=0.002)),
                           (0.11, note(C4 * 0.94, 0.25, MARIMBA, 0.06, attack=0.002))], 0.38), peak=0.45)
print(f"wrote {OUT}/quiz_success.wav and quiz_fail.wav")
