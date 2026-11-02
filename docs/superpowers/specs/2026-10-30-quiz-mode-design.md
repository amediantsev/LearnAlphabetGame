# Abetka: the Find mode («Знайди», letter knowledge check)

Date: 2026-10-30

Extends the [first version design](2026-10-29-learn-alphabet-design.md). Everything said there about languages, speech, pictures and target phones applies here too.

## Why

The child has already learnt some letters from the cards. Now the knowledge needs to be consolidated: not just looking at a letter and hearing its name, but finding the right letter among others on his own. As before, an adult sits next to the child, and it is a shared activity.

The exercise: the child sees a picture, hears "А… автобус" (exactly as on the cards) and looks for the letter he heard among four. So he recognises a letter's **shape** from its **name**. The picture supports and holds attention but does not give the answer away.

## Goals

- A new mode «Знайди» (Find) next to «Абетка» (Alphabet) and «Випадково» (Shuffle).
- A question: a picture, a 🔊 button and four big letter tiles, one of them correct.
- The question is spoken automatically once, when a new question appears.
- You can make mistakes but cannot lose: each wrong try only removes an option, and the child always reaches the right letter.
- The right answer is clearly visible (green colour, a checkmark), and the word with the highlighted letter is shown.

## Non-goals (for now, but noted for the future)

- **Choosing the set of letters to check.** For now all 33 letters are asked, and wrong options are also drawn from all 33. Later the adult should be able to pick a subset, for example only the letters the child has already learnt. The code should not get in the way: the set of letters for questions and for options is a separate parameter, not hard-wired to "the whole language".
- **Separate "learning" and "checking" sections.** For now the mode is a third button in the same switch. The owner plans to separate these parts more radically later. Keep the quiz logic apart from the card logic, so such a split only changes the UI.
- **Sound effects** (a chime for a right answer, etc.). No sounds except the spoken question. The owner will probably add them later.
- Points, score, statistics, timers, difficulty levels.

## Anti-goals (a failure even if it "works")

- The child can "lose", get stuck or be punished for a mistake. Every round ends with the right answer.
- A mistake looks scary: a harsh sound, vibration, a big red screen. A mistake should be noticeable but calm.
- The answer is visible before the child answers (for example, the word «**а**втобус» under the picture).
- Speech in this mode sounds different from the cards.
- Speech starts by itself again (on every try, on returning to the app, etc.). It starts by itself only once per question.
- Automatic speech brings up the "No Ukrainian voice" dialog on its own, without a tap.

## Owner's decisions

- The question is spoken the same way as on the cards: letter name, pause, word ("а… автобус").
- Speech starts automatically once when a new question appears. It can be repeated with 🔊 or by tapping the picture.
- **4 options, a 2×2 grid** (mockup option A chosen; 3 options in a row rejected).
- Wrong answer: the tile turns red (pale, with ✕) and can no longer be tapped. The rest stay active.
- Right answer: green colour and a checkmark; the exact look the owner left to the builder (see below, approved on the mockup).
- No sounds in any scenario, except speech.
- All letters of the language are asked, no subset.
- Mode switch: «Абетка · Випадково · Знайди» (approved on the mockup, temporary).

## My decisions (approved on the mockup)

Mockup: `.superpowers/brainstorm/65269-1793371405/content/quiz-layout.html` (not in git), first row: three moments of a round.

### Question screen

- At the top, the same mode switch and ⚙️.
- The picture is smaller than on the cards (about half the width or less), so the tiles are the main thing.
- Under the picture, space for the word. **It is empty until the right answer**, but the space is reserved so nothing jumps when the word appears.
- Four square tiles in a 2×2 grid: white background, thin border, a big dark **uppercase** letter («А», without the lowercase «а»). Tiles as big as possible, so a child's finger does not miss.
- At the bottom: 🔊 in the centre and ▶ on the right. There is no ◀ in this mode (the slot stays empty so 🔊 stays centred).

### Wrong try

- The tile shakes briefly (about 0.3 s), then stays pale red with ✕ in the corner and no longer reacts to touch.
- Nothing else changes: no speech, no messages.

### Right answer

- The tile turns green (`#4C9F70`, like the ▶ buttons) with a white letter, gets ✓ in the corner and grows a little (~6%).
- The other tiles fade (both wrong and not yet tapped) and can no longer be tapped.
- The word with the highlighted letter appears under the picture, as on the cards («**а**втобус», «к**и**т»).
- **No automatic move on.** The ▶ button turns green, bigger and "glows" (a soft ring around it). The child presses "next" himself, and the adult has time to praise and talk.
- Speech does not start again (but 🔊 and tapping the picture work).

### Moving on and skipping

- ▶ or a swipe left: the next question. A new question slides in the same way as cards and starts speech by itself.
- Before the right answer ▶ is grey but works: this skips a letter the child does not know yet. A skipped letter does not come back in this round.
- A swipe right does nothing in this mode: there is no going back to the previous question.

### Question order and options

- Letters are asked in random order in rounds: each letter exactly once per round, and a new round does not start with the letter that was just asked (as in Shuffle).
- The three wrong options are random distinct letters of the same language, other than the right one. Look-alike letters (Ш/Щ, І/Ї, Е/Є, Г/Ґ, И/Й) are neither filtered out nor picked on purpose.
- The right tile's place in the grid is random.
- A question's options are fixed when it appears and do not change until moving on.

### Switching modes and language

- Turning on Find: a new question right away (random letter), speech starts by itself.
- Going back from Find to Alphabet or Shuffle: the card of the letter of the last question opens.
- Turning Find on again starts a new question; the state of the previous one (mistakes, solved) is not kept. The round continues though: letters already asked in this round are not repeated.
- The first question after entering the mode cannot be the letter whose card was just on screen: the child has just seen the answer.
- Changing the language in Find: a new round and a new question in the new language. (Only Ukrainian is available for now.)
- Nothing is remembered between app launches.

### Automatic speech: exact rules

- It starts once for every new question: on entering the mode and on every move on.
- The rule is easiest to picture as a "waiting for auto-speech" flag. A new question sets it. When the flag is set and the voice is ready, speech starts and the flag is cleared. A failed try (the voice broke mid-speech) also counts as used.
- If the voice is still getting ready (the app just started), the flag waits, and speech starts as soon as the voice is ready.
- When the app goes to the background, the flag is cleared. So returning to the app does not start speech, even if the voice became ready just then (for example, the adult just installed the Ukrainian voice in settings). After returning, speech is started with 🔊.
- If there is no Ukrainian voice or synthesis is unavailable, automatic speech simply stays silent. The "No Ukrainian voice" hint appears, as before, only after tapping 🔊 or the picture.
- Tapping tiles does not interrupt speech.

## Constraints

- The same target phones: Xiaomi Redmi Note 9 (≈393×851 dp) and Xiaomi 14T Pro. The screen must fit without scrolling or clipping on both, including with system font ×1.3.
- No new permissions, no network, no new dependencies if it can be avoided.
- Alphabet and Shuffle work as before.
- Work goes through PRs; GitHub Actions builds an APK for each PR automatically.

## Left to the builder

- Exact sizes, spacing, duration and shape of animations (within what is described above).
- How the letter size on a tile depends on the tile size and the system font (the main thing is not to clip).
- Code structure: a separate question state model, a separate view model or an extension of the existing one, as long as the quiz logic (options, tries, "solved", question order, the auto-speech rule) is covered by unit tests without Android.
- Accessibility texts (contentDescription) for tiles and buttons.
