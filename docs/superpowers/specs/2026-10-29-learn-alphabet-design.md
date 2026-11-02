# Abetka: first version design (MVP)

Date: 2026-10-29

## Why

A father (the project owner) wants to teach his child (4 years old) the Ukrainian letters. How it is used: an adult sits next to the child, they flip through the cards together and name the letter and the object in the picture. It is a shared activity, not a game the child plays alone.

Some day the app may go public (Google Play) and get other languages and reading lessons. That is not the goal now, but the structure should not get in the way of it.

## Goals

- A card for each of the 33 letters of the Ukrainian alphabet: letter, picture, word.
- Flipping forward and back.
- Two orders: alphabetical and shuffled.
- A sound button: the letter, then the word.
- A ready `.apk` that the owner installs on his phone by hand.
- A language is a separate data set. A new language is added without changing screen code.

## Non-goals (for now)

- Reading lessons, syllables, games, tests, points, rewards.
- Languages other than Ukrainian (only a visible "coming soon" item in the language picker).
- Google Play publishing, release signing, a developer account.
- A recorded human voice (but sound is a separate module so one can be added).
- Analytics, ads, network, accounts.

## Anti-goals (a failure even if it "works")

- A letter pronounced wrong. The child must learn the correct letter names from the start.
- A word a four-year-old does not recognise from the picture.
- Pictures taken from other sources, or well-known characters (copyright, if the app goes public).
- The app crashes when the phone has no Ukrainian voice.

## Owner's decisions

- **Card look (mockup option A chosen):** a big letter on top in both cases («А а»), the picture in the middle, below it the word with the letter highlighted («**А**втобус»). At the bottom ◀ 🔊 ▶ buttons, **a bit smaller** than in the mockup (mockup: arrows 56 dp, sound 72 dp, so roughly 48 and 60 dp). At the top the mode switch and a settings gear.
- **Modes:** «Абетка» (Alphabet, in order) and «Випадково» (Shuffle).
- **Speech:** the phone's built-in speech synthesis (Android TextToSpeech, Ukrainian). A letter is spoken by **its name** ("бе", not the sound "б"), then the word: "бе… банан".
- **Pictures:** cartoon-like but realistic-looking, no named characters. The owner generates them in ChatGPT from the template below. Until there are pictures, the app shows placeholders.
- **Languages:** a language picker in settings; Ukrainian is available, no others yet ("coming soon" item).

## My decisions (the owner approved the overall description)

- Flipping also works by swipe, not only by arrows.
- Tapping the picture also starts speech.
- Portrait orientation only.
- Shuffle mixes all letters and shows each exactly once; after the last one it reshuffles (and does not start the new round with the letter that was just shown). In alphabetical order «Я» is followed by «А» again, and going back from «А» gives «Я».
- If there is no Ukrainian voice or synthesis is unavailable, a short clear hint is shown (how to install the voice in phone settings, with a button that opens the system speech settings). The app does not crash, cards keep working.
- Settings contain only the language picker. The "coming soon" item in the language list: "English".
- In Shuffle, ◀ goes back through the history of cards already shown (within the current launch), not to the "previous one alphabetically". At the very start of the history ◀ does nothing.
- The word on the card is written in lowercase («**а**втобус», «к**и**т»), and the highlighted letter is the same lowercase letter as in the word. That way highlighting looks the same on every card.
- Synthesis: the app looks for a Ukrainian voice in any installed engine and prefers Google (`com.google.android.tts`), because others (e.g. Samsung) often lack Ukrainian. Letter-name pronunciation is tuned for the Google engine.
- Listening check: the owner listens to all 33 cards on his phone; the list of names that need fixing goes back into the data.
- Stack: Kotlin + Jetpack Compose, a single `app` module, minimum Android 8.0 (API 26).
- App name on the phone: «Абетка». Package id: `app.learnalphabet` (can be changed before the first publication).
- Build: a debug APK signed with the standard debug key. That is enough to install on his own phone.
- **Letters И and Ь, and also Ю:** everyday Ukrainian has almost no words starting with И, none at all with Ь, and I found no word starting with Ю that a child would recognise. So for these the word contains the letter in the middle, and that letter is highlighted: к**и**т, кін**ь**, р**ю**кзак. The data model supports this (see below).

## Data model (the gist)

Every language is a data set, not code:

- language code (`uk`), menu name («Українська»), locale for synthesis (`uk-UA`), available or "coming soon";
- an ordered list of letters. For each:
  - upper and lower case («А», «а»);
  - how to pronounce the letter name (text for synthesis, e.g. "бе"). A separate field, because synthesis does not always guess the name of a single letter, and it has to be tuned;
  - the word ("банан") and, when needed, the word text for synthesis, if stress or pronunciation needs fixing;
  - which letter in the word to highlight (position), because for И, Ь, Ю it is not the first one;
  - the picture file name;
  - a placeholder until the picture exists (an emoji or a coloured tile with the letter).

UI texts (mode names, hints) go through regular Android string resources, so they can be translated too.

Sound sits behind a small "speak a card" interface, so synthesis can later be replaced with recorded files.

## Word list (Ukrainian)

Letter names for synthesis are starting values; they have to be checked by ear on the emulator and fixed if synthesis sounds wrong.

| # | Letter | Spoken as | Word | Highlight | Prompt object (English) | File |
|---|---|---|---|---|---|---|
| 1 | А а | а | автобус | 1st | a yellow city bus | 01-avtobus.png |
| 2 | Б б | бе | банан | 1st | a ripe yellow banana | 02-banan.png |
| 3 | В в | ве | ведмідь | 1st | a friendly brown bear | 03-vedmid.png |
| 4 | Г г | ге | груша | 1st | a green-yellow pear | 04-hrusha.png |
| 5 | Ґ ґ | ґе | ґудзик | 1st | a big round sewing button with four holes | 05-gudzyk.png |
| 6 | Д д | де | динозавр | 1st | a friendly green dinosaur | 06-dynozavr.png |
| 7 | Е е | е | екскаватор | 1st | a yellow excavator (digger) | 07-ekskavator.png |
| 8 | Є є | є | єнот | 1st | a raccoon | 08-yenot.png |
| 9 | Ж ж | же | жираф | 1st | a giraffe | 09-zhyraf.png |
| 10 | З з | зе | зебра | 1st | a zebra | 10-zebra.png |
| 11 | И и | и | кит | 2nd | a blue whale with a water spout | 11-kyt.png |
| 12 | І і | і | індик | 1st | a turkey (the bird) | 12-indyk.png |
| 13 | Ї ї | ї | їжак | 1st | a hedgehog | 13-yizhak.png |
| 14 | Й й | йот | йогурт | 1st | a cup of yogurt with a spoon | 14-yohurt.png |
| 15 | К к | ка | кіт | 1st | a cat | 15-kit.png |
| 16 | Л л | ел | лев | 1st | a lion | 16-lev.png |
| 17 | М м | ем | м'яч | 1st | a colorful ball | 17-miach.png |
| 18 | Н н | ен | носоріг | 1st | a rhinoceros | 18-nosorih.png |
| 19 | О о | о | огірок | 1st | a green cucumber | 19-ohirok.png |
| 20 | П п | пе | пінгвін | 1st | a penguin | 20-pinhvin.png |
| 21 | Р р | ер | риба | 1st | a colorful fish | 21-ryba.png |
| 22 | С с | ес | сонце | 1st | a smiling sun | 22-sontse.png |
| 23 | Т т | те | трактор | 1st | a red tractor | 23-traktor.png |
| 24 | У у | у | усмішка | 1st | a big happy smile (smiling face) | 24-usmishka.png |
| 25 | Ф ф | еф | фламінго | 1st | a pink flamingo | 25-flamingo.png |
| 26 | Х х | ха | хмарка | 1st | a fluffy white cloud | 26-khmarka.png |
| 27 | Ц ц | це | цукерка | 1st | a wrapped candy | 27-tsukerka.png |
| 28 | Ч ч | че | черепаха | 1st | a turtle | 28-cherepakha.png |
| 29 | Ш ш | ша | шапка | 1st | a warm knitted winter hat | 29-shapka.png |
| 30 | Щ щ | ща | щітка | 1st | a toothbrush | 30-shchitka.png |
| 31 | Ь ь | м'який знак | кінь | 4th | a horse | 31-kin.png |
| 32 | Ю ю | ю | рюкзак | 2nd | a child's backpack | 32-riukzak.png |
| 33 | Я я | я | яблуко | 1st | a red apple | 33-yabluko.png |

Weak spots worth checking with the child: «ґудзик» (button), «індик» (turkey), «носоріг» (rhinoceros), «усмішка» (smile), «єнот» (raccoon). If a word is unfamiliar to him, the owner can replace it (it is just one line of data and one picture).

## Pictures: ChatGPT prompt template

One style for all 33 pictures. It works best in a single chat: paste the template first, then ask for "next: <object>".

```
Create a children's picture-book illustration of [OBJECT].

Style: soft, friendly cartoon style that still looks fairly realistic and
instantly recognizable to a 4-year-old. Gentle shading, rounded shapes,
bright warm colors. Not a famous or branded character.

Composition: exactly one object, centered, fully visible with some empty
space around it, facing slightly toward the viewer. Plain solid light-cream
background (#FFF8EC). No text, no letters, no numbers, no shadows on the
ground, no other objects, no frame.

Format: square, 1024x1024 PNG.
```

`[OBJECT]` is the value from the "Prompt object" column. The owner names the finished files after the "File" column and puts them in the project folder `art/uk/`. Converting them for the app (downscaling, WebP) is the build's job.

## Constraints

- Target phones: **Xiaomi Redmi Note 9** (the child's; MIUI, Android 10–12) and **Xiaomi 14T Pro** (the owner's; HyperOS, Android 14+). The app must install and sound the same on both. On global Xiaomi firmware the default voice is usually Google, but the Ukrainian voice may need to be downloaded; the in-app hint must cover that. The weaker Redmi Note 9 is the performance baseline (smooth flipping, pictures not too large).
- Listening check on both phones.

- Built on the owner's Mac. The Android SDK is already there (`~/Library/Android/sdk`, platform android-34, build-tools 34/35). The installed Java 8 is too old; JDK 17 is needed (install via Homebrew).
- No network, no permissions beyond the standard ones.
- Keep the APK small (compress pictures).

## Left to the builder

- The exact look: colours, font, spacing, flip animation (guide: mockup A, cream background, warm colours, orange-red letter).
- How language data is stored (Kotlin objects, JSON in assets, etc.), as long as the model above holds.
- How to tune the pronunciation of individual letter names so synthesis sounds right.
- Whether to remember the chosen mode and current card between launches.
- How the placeholders look.
- Tests: at least the order logic (alphabetical, shuffled round without repeats) and data integrity (33 letters, each has a word, the highlight position points at that letter in the word).
