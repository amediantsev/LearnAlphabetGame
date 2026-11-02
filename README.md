# Abetka

An Android app for learning the Ukrainian alphabet: a card for every letter (letter, picture, word), flipping through cards, Alphabet and Shuffle modes, a Find quiz, and speech for the letter and the word. The app's own UI is in Ukrainian; its name on the phone is «Абетка».

Design: [docs/superpowers/specs/2026-10-29-learn-alphabet-design.md](docs/superpowers/specs/2026-10-29-learn-alphabet-design.md)

## Modes

- **Alphabet** («Абетка») and **Shuffle** («Випадково»): learning cards (letter, picture, word), in order or shuffled.
- **Find** («Знайди»): a check to consolidate what was learnt. The app shows a picture, speaks "а… автобус" once by itself, and the child looks for that letter among four. A wrong tile turns red and is disabled, so the child always reaches the right one. The right one turns green with ✓, the word appears, and ▶ moves on (▶ can also skip a question). A right answer rings two bells, a wrong tile gives two soft wooden knocks; both follow the phone's media volume.

In ⚙️ «Налаштування» (Settings) you can choose what is spoken: the letter and the word ("А… автобус") or just the letter ("А"). The choice applies to all modes and is kept after a restart.

In the same settings, «Знайди → Літери» opens a picker for the letters Find asks about, so the set can grow as the child learns new letters. Questions come only from the chosen letters; the wrong options come from the whole alphabet. By default all 33 letters are chosen, and the choice is kept after a restart.

Find mode design: [docs/superpowers/specs/2026-10-30-quiz-mode-design.md](docs/superpowers/specs/2026-10-30-quiz-mode-design.md).

## Download a ready APK

Latest build from `main`: [abetka-debug.apk](https://github.com/amediantsev/LearnAlphabetGame/releases/latest/download/abetka-debug.apk). You can open the link right on the phone.

GitHub Actions builds the APK automatically (`.github/workflows/apk.yml`):

- for every PR: tests, a build and a `pr-<number>` pre-release; the PR gets a comment with a link to the APK. When the PR is closed, the pre-release is deleted;
- for every push to `main`: the "Abetka: latest build" release (link above).

All builds, from GitHub and from any Mac, are signed with the same key `app/debug.keystore`, so a new version installs over the old one. It is a debug key and not a secret; Google Play will need a separate release key.

## Build the APK yourself

You need JDK 17 (`brew install openjdk@17`) and the Android SDK (`~/Library/Android/sdk`).

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew testDebugUnitTest assembleDebug
mkdir -p dist && cp app/build/outputs/apk/debug/app-debug.apk dist/abetka-debug.apk
```

## Add pictures

1. Generate a picture in ChatGPT with the template from the design doc (section "Pictures: ChatGPT prompt template").
2. Save it as a PNG named after the "File" column, e.g. `01-avtobus.png`, in the `art/uk/` folder.
3. Run `tools/import-art.sh uk`. The script downscales the pictures to 512×512 and converts them to WebP (needs `brew install webp`).
4. Build the APK again.

Until a picture exists, the card shows an emoji or a tile with the letter.

## Install on a Xiaomi phone

1. Open the APK link (see above) on the phone, or send `dist/abetka-debug.apk` to it (Telegram, Google Drive, cable).
2. Open the file. The phone will ask to allow installing unknown apps for the app you opened the file with (for example, Files or Telegram). Allow it.
3. If HyperOS/MIUI shows a security check, choose "Install anyway". The warning appears because the app is not from Google Play.
4. Install new versions the same way, over the old one.

## Ukrainian voice

Speech uses "Speech Services by Google". If after pressing 🔊 the app says there is no voice:

Settings → Additional settings → Languages & input → Text-to-speech output → Speech Services by Google ⚙️ → Install voice data → Ukrainian.

(The exact menu names depend on the phone's language and firmware.) If Speech Services by Google is missing, install it from Google Play.

## Quiz sounds

The Find sounds (`app/src/main/res/raw/quiz_success.wav`, `quiz_fail.wav`) are synthesized by `tools/make-quiz-sounds.py` (needs `pip3 install numpy`). To tune one (louder, shorter, higher), change its numbers in the script, run `python3 tools/make-quiz-sounds.py` and build the APK again.

## Letter pronunciation

What synthesis says for each letter name is set in `app/src/main/java/app/learnalphabet/alphabet/Ukrainian.kt` (the third parameter, e.g. `"бе"`). If a letter sounds wrong, change that text and build the APK again.
