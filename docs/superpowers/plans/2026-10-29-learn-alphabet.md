# Абетка MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** An Android app (debug APK) with 33 Ukrainian letter cards (letter, picture, word), forward/back and swipe navigation, alphabetical and shuffled modes, and a sound button that speaks the letter name and then the word.

**Architecture:** Single-module Compose app. Each language is a plain Kotlin data object (letters, spoken texts, words, image file names); UI never hard-codes letters. Card order lives in two pure-Kotlin navigators. Speech is behind a `CardSpeaker` interface implemented with Android `TextToSpeech`, preferring Google's engine.

**Tech Stack:** Kotlin 2.0.20, AGP 8.5.2, Gradle 8.9 (wrapper), Jetpack Compose (BOM 2024.09.02, Material 3), androidx.activity-compose 1.9.2, lifecycle-viewmodel-compose 2.8.6, JUnit 4.13.2. JDK 17.

**Spec:** `docs/superpowers/specs/2026-10-29-learn-alphabet-design.md`

## Global Constraints

- Package / applicationId: `app.learnalphabet`. App label: `Абетка`.
- minSdk 26, compileSdk 34, targetSdk 34 (SDK platform android-34 and build-tools 34.0.0 are already installed at `~/Library/Android/sdk`).
- Portrait only (`android:screenOrientation="portrait"`).
- No permissions, no network, no analytics.
- Letter is spoken by its **name** ("бе", "йот", "м'який знак"), then the word.
- Word shown lowercase with one highlighted character at `highlightIndex`; highlighted char equals the card's lowercase letter.
- Card layout = spec mockup A: big "А а" on top, picture in the middle, word below, bottom row ◀ 🔊 ▶ with arrow buttons ~48 dp and sound button ~60 dp; mode switch ("Абетка" / "Випадково") and ⚙️ at the top. Cream background `#FFF8EC`, letter color `#E4572E`, arrow buttons `#4C9F70`, sound button `#F4A259`.
- Languages menu: "Українська" (available), "English" (shown, disabled, labelled "скоро").
- UI strings in `res/values/strings.xml` (Ukrainian), not hard-coded in Kotlin.
- Debug-signed APK; target devices Xiaomi Redmi Note 9 (MIUI, Android 10–12) and Xiaomi 14T Pro (HyperOS, Android 14+).
- Art: owner drops 1024×1024 PNGs named per spec table into `art/uk/`; the app ships them as 512×512 WebP in `app/src/main/assets/images/uk/` with the same base name (`01-avtobus.webp`).
- `git init` happens in Task 1; every task ends with a commit. `.superpowers/` and build outputs are git-ignored.

## Review Focus

1. **No Ukrainian voice / TTS init failure** (likely on Xiaomi without Google voice data): app shows the hint with a button to the system TTS settings; cards keep working. → Task 4 test `stateFor_missingData_isMissingVoice` and friends.
2. **Missing picture file** for a letter (art arrives gradually): placeholder shown, no crash. → Task 5 test `imageAssetPath_missingFile_returnsNull`.
3. **Mode switch mid-session**: the current card stays on screen; the new order continues from it. → Task 3 tests `alphabet_startsAt_givenIndex`, `shuffle_firstCard_isGivenStart`.
4. **Rapid repeated taps on sound / next**: previous speech is cut off (`QUEUE_FLUSH`), never queued up. → Task 4 test `utterances_firstUsesFlush`.
5. **App sent to background while speaking**: speech stops; TTS released on destroy. → Task 5 manual check step on emulator.

---

### Task 1: Toolchain and empty app that builds an APK

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradlew` + `gradle/wrapper/*`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `app/src/main/java/app/learnalphabet/MainActivity.kt`, `app/src/main/res/values/strings.xml`, `local.properties`, `.gitignore`

**Interfaces:**
- Produces: a building Gradle project; `MainActivity` (ComponentActivity, `setContent {}`) that later tasks fill.

- [ ] **Step 1: Install JDK 17 and Gradle**

Run: `brew install openjdk@17 gradle webp`
Then use `export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || echo /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home)` in every build command (do not change the user's shell profile).
Expected: `$JAVA_HOME/bin/java -version` prints 17.

- [ ] **Step 2: Create the project files**

`local.properties`: `sdk.dir=/Users/oleksandrmediantsev/Library/Android/sdk`. `.gitignore`: `.gradle/`, `build/`, `local.properties`, `.superpowers/`, `.idea/`, `*.iml`. Compose enabled via the `org.jetbrains.kotlin.plugin.compose` plugin. Manifest: one launcher activity, portrait, `android:label="@string/app_name"`. `strings.xml`: `app_name` = `Абетка`. MainActivity shows a centered `Text(stringResource(R.string.app_name))`.

- [ ] **Step 3: Generate wrapper and build**

Run: `gradle wrapper --gradle-version 8.9 && ./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`, file `app/build/outputs/apk/debug/app-debug.apk` exists.

- [ ] **Step 4: Commit**

```bash
git init && git add -A && git commit -m "chore: scaffold Android project"
```

---

### Task 2: Language data model and Ukrainian alphabet

**Files:**
- Create: `app/src/main/java/app/learnalphabet/alphabet/Language.kt`
- Create: `app/src/main/java/app/learnalphabet/alphabet/Ukrainian.kt`
- Create: `app/src/main/java/app/learnalphabet/alphabet/Languages.kt`
- Test: `app/src/test/java/app/learnalphabet/alphabet/UkrainianDataTest.kt`

**Interfaces:**
- Produces:
  ```kotlin
  data class LetterCard(
      val upper: String, val lower: String,
      val spokenName: String,          // TTS text for the letter name, e.g. "бе"
      val word: String,                // lowercase, e.g. "банан"
      val spokenWord: String? = null,  // TTS override when the word needs it
      val highlightIndex: Int = 0,     // index in word of the letter to highlight
      val imageName: String,           // base file name without extension, e.g. "02-banan"
      val placeholder: String,         // emoji shown until art exists
  )
  data class Language(
      val code: String, val displayName: String, val ttsLocale: java.util.Locale,
      val available: Boolean, val letters: List<LetterCard>,
  )
  object Languages { val all: List<Language>; val default: Language; fun byCode(code: String): Language? }
  val Ukrainian: Language  // code "uk", "Українська", Locale("uk","UA"), available = true
  ```
  `Languages.all` = `[Ukrainian, English]` where English is `Language("en", "English", Locale.ENGLISH, available = false, letters = emptyList())`.

- [ ] **Step 1: Write the failing tests**

```kotlin
@Test fun ukrainian_has33Letters_inAlphabetOrder() {
    assertEquals("АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯ", Ukrainian.letters.joinToString("") { it.upper })
}
@Test fun everyHighlight_pointsAtTheCardsLetter() {
    Ukrainian.letters.forEach { assertEquals(it.upper, it.lower, it.word[it.highlightIndex].toString(), it.lower) }
}
@Test fun specialLetters_useInnerHighlight() {
    val byUpper = Ukrainian.letters.associateBy { it.upper }
    assertEquals("кит" to 1, byUpper.getValue("И").let { it.word to it.highlightIndex })
    assertEquals("кінь" to 3, byUpper.getValue("Ь").let { it.word to it.highlightIndex })
    assertEquals("рюкзак" to 1, byUpper.getValue("Ю").let { it.word to it.highlightIndex })
}
@Test fun imageNames_areUnique_andNumbered() {
    val names = Ukrainian.letters.map { it.imageName }
    assertEquals(33, names.toSet().size)
    names.forEachIndexed { i, n -> assertTrue(n, n.startsWith("%02d-".format(i + 1))) }
}
@Test fun spokenNames_matchSpec() {
    assertEquals(listOf("а","бе","ве","ге","ґе","де","е","є","же","зе","и","і","ї","йот","ка","ел","ем","ен","о","пе","ер","ес","те","у","еф","ха","це","че","ша","ща","м'який знак","ю","я"),
        Ukrainian.letters.map { it.spokenName })
}
@Test fun languages_defaultIsUkrainian_englishIsComingSoon() {
    assertEquals("uk", Languages.default.code)
    assertFalse(Languages.byCode("en")!!.available)
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew testDebugUnitTest --tests '*UkrainianDataTest*'`
Expected: FAIL (compilation: `Ukrainian` unresolved).

- [ ] **Step 3: Implement the model and data**

Words, image names and spoken names come verbatim from the spec table (`Слово`, `Файл` without `.png`, `Як вимовити`); highlight index = spec column `Виділити` minus 1. Placeholder emoji: pick a matching one per word where Unicode has it (🚌 🍌 🐻 🍐 🦕 🦝 🦒 🦓 🐋 🦃 🦔 🐱 🦁 ⚽ 🦏 🥒 🐧 🐟 ☀️ 🚜 😊 🦩 ☁️ 🍬 🐢 🧢 🪥 🐴 🎒 🍎 …); otherwise use `""` and the UI shows a letter tile.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew testDebugUnitTest --tests '*UkrainianDataTest*'`
Expected: PASS (6 tests).

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: language model and Ukrainian alphabet data"
```

---

### Task 3: Card order navigators

**Files:**
- Create: `app/src/main/java/app/learnalphabet/order/CardNavigator.kt`
- Test: `app/src/test/java/app/learnalphabet/order/CardNavigatorTest.kt`

**Interfaces:**
- Produces:
  ```kotlin
  interface CardNavigator { val current: Int; fun next(): Int; fun previous(): Int }
  class AlphabetNavigator(size: Int, start: Int = 0) : CardNavigator      // wraps both ways
  class ShuffleNavigator(size: Int, start: Int = 0, random: kotlin.random.Random = Random.Default) : CardNavigator
  enum class OrderMode { ALPHABET, SHUFFLE }
  fun navigatorFor(mode: OrderMode, size: Int, start: Int): CardNavigator
  ```

- [ ] **Step 1: Write the failing tests**

```kotlin
@Test fun alphabet_startsAt_givenIndex() = assertEquals(5, AlphabetNavigator(33, 5).current)
@Test fun alphabet_wrapsForward() { val n = AlphabetNavigator(33, 32); assertEquals(0, n.next()) }
@Test fun alphabet_wrapsBackward() { val n = AlphabetNavigator(33, 0); assertEquals(32, n.previous()) }
@Test fun shuffle_firstCard_isGivenStart() = assertEquals(7, ShuffleNavigator(33, 7, Random(1)).current)
@Test fun shuffle_eachRound_showsEveryCardOnce() {
    val n = ShuffleNavigator(33, 0, Random(42))
    val round = listOf(n.current) + List(32) { n.next() }
    assertEquals((0 until 33).toSet(), round.toSet())
}
@Test fun shuffle_newRound_neverRepeatsLastCard() {
    repeat(200) { seed ->
        val n = ShuffleNavigator(5, 0, Random(seed))
        var last = n.current
        repeat(50) { val x = n.next(); assertNotEquals(last, x); last = x }
    }
}
@Test fun shuffle_previous_walksBackThroughHistory() {
    val n = ShuffleNavigator(33, 0, Random(3)); val a = n.current; val b = n.next(); n.next()
    assertEquals(b, n.previous()); assertEquals(a, n.previous())
}
@Test fun shuffle_previous_atStart_staysPut() { val n = ShuffleNavigator(33, 4, Random(3)); assertEquals(4, n.previous()) }
@Test fun shuffle_nextAfterPrevious_replaysHistory() {
    val n = ShuffleNavigator(33, 0, Random(9)); val b = n.next(); n.previous(); assertEquals(b, n.next())
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew testDebugUnitTest --tests '*CardNavigatorTest*'`
Expected: FAIL (unresolved references).

- [ ] **Step 3: Implement the navigators**

`ShuffleNavigator` keeps `history: MutableList<Int>` and `pos`. `next()` moves forward in history if possible; otherwise draws from the current round's remaining deck. The first round's deck is a shuffle of all indices except `start`. When the deck is empty, a new round shuffles all indices and, if its first element equals the last shown card, swaps it with another position. With `size == 1` repeats are unavoidable; allow them.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew testDebugUnitTest --tests '*CardNavigatorTest*'`
Expected: PASS (9 tests).

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: alphabetical and shuffled card navigators"
```

---

### Task 4: Speech behind `CardSpeaker`

**Files:**
- Create: `app/src/main/java/app/learnalphabet/speech/CardSpeaker.kt` (interface, state, pure helpers)
- Create: `app/src/main/java/app/learnalphabet/speech/TtsCardSpeaker.kt`
- Test: `app/src/test/java/app/learnalphabet/speech/SpeechLogicTest.kt`

**Interfaces:**
- Consumes: `LetterCard` (Task 2).
- Produces:
  ```kotlin
  enum class SpeakerState { INITIALIZING, READY, MISSING_VOICE, UNAVAILABLE }
  interface CardSpeaker { val state: StateFlow<SpeakerState>; fun speak(card: LetterCard); fun stop(); fun release() }
  const val GOOGLE_TTS = "com.google.android.tts"
  fun chooseEngine(installed: List<String>, default: String?): String?   // GOOGLE_TTS if installed, else default
  fun stateFor(languageResult: Int): SpeakerState   // maps TextToSpeech.setLanguage/isLanguageAvailable result
  data class Utterance(val text: String, val flush: Boolean, val pauseAfterMs: Long = 0)
  fun utterancesFor(card: LetterCard): List<Utterance>
  class TtsCardSpeaker(context: Context, locale: Locale) : CardSpeaker
  ```
  Also in `CardSpeaker.kt`: `fun ttsSettingsIntent(): Intent` = `Intent("com.android.settings.TTS_SETTINGS")`.

- [ ] **Step 1: Write the failing tests**

```kotlin
@Test fun chooseEngine_prefersGoogle() = assertEquals(GOOGLE_TTS, chooseEngine(listOf("com.samsung.SMT", GOOGLE_TTS), "com.samsung.SMT"))
@Test fun chooseEngine_fallsBackToDefault() = assertEquals("x.tts", chooseEngine(listOf("x.tts"), "x.tts"))
@Test fun stateFor_available_isReady() = assertEquals(SpeakerState.READY, stateFor(TextToSpeech.LANG_COUNTRY_AVAILABLE))
@Test fun stateFor_missingData_isMissingVoice() = assertEquals(SpeakerState.MISSING_VOICE, stateFor(TextToSpeech.LANG_MISSING_DATA))
@Test fun stateFor_notSupported_isMissingVoice() = assertEquals(SpeakerState.MISSING_VOICE, stateFor(TextToSpeech.LANG_NOT_SUPPORTED))
@Test fun utterances_letterNameThenWord() {
    val card = Ukrainian.letters[1]
    assertEquals(listOf("бе", "банан"), utterancesFor(card).map { it.text })
}
@Test fun utterances_firstUsesFlush() {
    val u = utterancesFor(Ukrainian.letters[0]); assertTrue(u[0].flush); assertFalse(u[1].flush)
    assertTrue(u[0].pauseAfterMs in 300L..700L)
}
@Test fun utterances_useSpokenWordOverride() {
    val card = Ukrainian.letters[0].copy(spokenWord = "авто́бус")
    assertEquals("авто́бус", utterancesFor(card)[1].text)
}
```
(`TextToSpeech.LANG_*` are compile-time constants, usable in JVM tests; add `testOptions.unitTests.isReturnDefaultValues = true`.)

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew testDebugUnitTest --tests '*SpeechLogicTest*'`
Expected: FAIL (unresolved references).

- [ ] **Step 3: Implement helpers and `TtsCardSpeaker`**

`stateFor`: `>= LANG_AVAILABLE` → READY, `LANG_MISSING_DATA` / `LANG_NOT_SUPPORTED` → MISSING_VOICE, anything else → UNAVAILABLE. `TtsCardSpeaker`: list engines with a throwaway `TextToSpeech(context) {}` instance's `engines` + `defaultEngine`, pick with `chooseEngine`, then create the real `TextToSpeech(context, listener, engine)`. On init failure → UNAVAILABLE. On success, `setLanguage(locale)` → `stateFor`. `speak()` is a no-op unless READY; it issues `speak(text, QUEUE_FLUSH/QUEUE_ADD, null, id)` and `playSilentUtterance(pauseAfterMs, QUEUE_ADD, id)`. Speech rate 0.85f (slower for a child).

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew testDebugUnitTest --tests '*SpeechLogicTest*'`
Expected: PASS (8 tests).

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: text-to-speech card speaker preferring Google engine"
```

---

### Task 5: Card screen, settings, images

**Files:**
- Create: `app/src/main/java/app/learnalphabet/ui/CardViewModel.kt`
- Create: `app/src/main/java/app/learnalphabet/ui/CardScreen.kt`
- Create: `app/src/main/java/app/learnalphabet/ui/LanguageDialog.kt`
- Create: `app/src/main/java/app/learnalphabet/ui/CardImage.kt`
- Create: `app/src/main/java/app/learnalphabet/ui/WordText.kt`
- Create: `app/src/main/java/app/learnalphabet/ui/theme/Theme.kt`
- Modify: `app/src/main/java/app/learnalphabet/MainActivity.kt`, `app/src/main/res/values/strings.xml`
- Test: `app/src/test/java/app/learnalphabet/ui/UiLogicTest.kt`

**Interfaces:**
- Consumes: `Languages`, `LetterCard` (Task 2); `OrderMode`, `navigatorFor` (Task 3); `CardSpeaker`, `SpeakerState`, `TtsCardSpeaker`, `ttsSettingsIntent()` (Task 4).
- Produces:
  ```kotlin
  data class CardUiState(val language: Language, val mode: OrderMode, val index: Int) { val card: LetterCard }
  class CardViewModel : ViewModel() {
      val ui: StateFlow<CardUiState>
      fun next(); fun previous(); fun setMode(mode: OrderMode); fun setLanguage(code: String)
  }
  fun highlightRange(card: LetterCard): IntRange           // single-char range at highlightIndex
  fun imageAssetPath(language: String, card: LetterCard, exists: (String) -> Boolean): String?  // "images/uk/02-banan.webp" or null
  ```

- [ ] **Step 1: Write the failing tests**

```kotlin
@Test fun highlightRange_isSingleCharAtIndex() = assertEquals(1..1, highlightRange(Ukrainian.letters.first { it.upper == "И" }))
@Test fun imageAssetPath_existingFile() =
    assertEquals("images/uk/02-banan.webp", imageAssetPath("uk", Ukrainian.letters[1]) { true })
@Test fun imageAssetPath_missingFile_returnsNull() = assertNull(imageAssetPath("uk", Ukrainian.letters[1]) { false })
@Test fun viewModel_setMode_keepsCurrentCard() {
    val vm = CardViewModel(); vm.next(); vm.next(); val before = vm.ui.value.index
    vm.setMode(OrderMode.SHUFFLE); assertEquals(before, vm.ui.value.index)
}
@Test fun viewModel_startsOnFirstUkrainianLetter() = assertEquals("А", CardViewModel().ui.value.card.upper)
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew testDebugUnitTest --tests '*UiLogicTest*'`
Expected: FAIL (unresolved references).

- [ ] **Step 3: Implement view model, helpers and screens**

- `CardViewModel` holds a `CardNavigator`; `setMode` builds `navigatorFor(mode, size, current)`. `setLanguage` ignores unavailable languages and resets to index 0.
- `MainActivity` owns the `TtsCardSpeaker` (created in `onCreate` with the current language's `ttsLocale`, `stop()` in `onStop`, `release()` in `onDestroy`) and passes it to `CardScreen`.
- `CardScreen` follows the Global Constraints layout. Card changes animate with `AnimatedContent` slide left/right. Horizontal swipe (`detectHorizontalDragGestures`, threshold ~80 dp) calls next/previous. Tapping the picture and 🔊 both call `speaker.speak(card)`. Next/previous also stop current speech.
- When `speaker.state` is `MISSING_VOICE` or `UNAVAILABLE`, tapping 🔊 shows an `AlertDialog`: text `@string/voice_missing` («Щоб чути літери, потрібен український голос. Відкрийте налаштування синтезу мовлення та завантажте українську мову для «Синтезатор мовлення Google».») with buttons «Відкрити налаштування» (`ttsSettingsIntent()`, wrapped in try/catch for `ActivityNotFoundException`) and «Закрити».
- Mode switch: two-segment control at the top with `@string/mode_alphabet` «Абетка» and `@string/mode_shuffle` «Випадково».
- `LanguageDialog` lists `Languages.all`; unavailable ones disabled with suffix `@string/coming_soon` «скоро».
- `CardImage` decodes the asset (`BitmapFactory`, off the main thread via `produceState`) if `imageAssetPath` finds it, else shows `placeholder` emoji at large size, or, when empty, a rounded tile with the letter.
- `WordText` renders `card.word` with `highlightRange` in the letter color, bold.

- [ ] **Step 4: Run all unit tests**

Run: `./gradlew testDebugUnitTest`
Expected: PASS (all tests from Tasks 2–5).

- [ ] **Step 5: Run on the emulator and check by eye**

Run: `$ANDROID_SDK/emulator/emulator -avd Medium_Phone_API_35 &`, then `./gradlew installDebug` and start the app with `adb shell am start -n app.learnalphabet/.MainActivity`.
Check with screenshots (`adb exec-out screencap -p`): card «А а / 🚌 / автобус» matches mockup A; ▶ ×33 returns to «А»; ◀ from «А» shows «Я»; swipe works; «Випадково» keeps the current card then walks a different order; ⚙️ shows Українська + English (скоро). Press Home while speaking: speech stops.

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "feat: card screen, modes, language picker and image loading"
```

---

### Task 6: Pronunciation tuning, art import, APK hand-off

**Files:**
- Create: `tools/import-art.sh`
- Modify: `app/src/main/java/app/learnalphabet/alphabet/Ukrainian.kt` (only `spokenName` / `spokenWord` values, if tuning needs it)
- Create: `README.md` (how to build, add art, install on phone)

**Interfaces:**
- Consumes: everything above.
- Produces: `app/build/outputs/apk/debug/app-debug.apk`, copied to `dist/abetka-debug.apk`.

- [ ] **Step 1: Write `tools/import-art.sh <lang>`**

For every `art/<lang>/*.png`: `sips -Z 512` into a temp file, then `cwebp -q 82` to `app/src/main/assets/images/<lang>/<same-base-name>.webp`. Prints each converted file and warns about PNGs whose base name matches no `imageName` (grep the names out of the language's Kotlin file). Exits 0 when `art/<lang>/` is empty.

- [ ] **Step 2: Check the script**

Run: create a test PNG (`sips -s format png -z 1024 1024` on any image, or `python3 -c` writing a solid PNG) as `art/uk/02-banan.png`, run `tools/import-art.sh uk`.
Expected: `app/src/main/assets/images/uk/02-banan.webp` exists, under 60 KB; the app shows it on the «Б» card. Remove the test files afterwards.

- [ ] **Step 3: Tune pronunciation on the emulator**

With Google TTS and the Ukrainian voice installed on the emulator, press 🔊 on all 33 cards and record which letter names sound wrong (watch especially «бе», «ґе», «и», «йот», «м'який знак», «ю», «я»). Adjust `spokenName` (spelling tweaks like «бе-е», a stress mark, or a trailing period) until each sounds right. Re-run `./gradlew testDebugUnitTest`; update `spokenNames_matchSpec` only for values changed here.

- [ ] **Step 4: Build the hand-off APK**

Run: `./gradlew assembleDebug && mkdir -p dist && cp app/build/outputs/apk/debug/app-debug.apk dist/abetka-debug.apk`
Expected: `dist/abetka-debug.apk` exists (target under 15 MB with full art). `dist/` is git-ignored.

- [ ] **Step 5: Write README and commit**

README in Ukrainian: build command, how to add pictures (`art/uk/`, file names from the spec, then `tools/import-art.sh uk`), how to install on Xiaomi (send the APK file, open it, allow «Встановлення з невідомих джерел» for that app; if HyperOS shows a security scan, choose to install anyway), how to install the Ukrainian voice (Settings → Додаткові налаштування → Мова та введення → Синтез мовлення → Google → Встановити голосові дані → Українська).

```bash
git add -A && git commit -m "feat: art import script, pronunciation tuning, README"
```

- [ ] **Step 6: Owner check on both phones (owner does this)**

Owner installs `dist/abetka-debug.apk` on Redmi Note 9 and 14T Pro, listens to all 33 cards, reports any letter that sounds wrong. Fixes go back into `spokenName` and a new APK.
