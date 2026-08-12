# The Contract

An Android TV game for two adult men in a room together. The television is the shared surface;
each man brings a phone, which is private. Over an evening the pair negotiate a contract of
sexual terms, pay for each one with a "consideration", and then perform the whole contract in
order as a final scene.

Everything runs on the local network. The TV hosts an HTTP/WebSocket server, the phones connect
to it in a browser, and nothing about a session ever leaves the box.

---

## 1. How a session runs

| Phase | What happens |
|---|---|
| Pairing | TV shows a QR code and a join URL. Two phones join and take PLAYER_1 / PLAYER_2. |
| Shared setup | Player 1 configures names, roles, anal roles, session length, explicitness register, finale format, stop word, boundaries, equipment. |
| Private profiles | Each man answers the preference questions on his own phone. Nobody else ever sees the answers. |
| Negotiation | The engine proposes a term. Both men answer independently: sign, reject, counteroffer, or bundle. |
| Consideration | Before a term is signed, the man who gains less picks a consideration from a list; it is performed and receipted. |
| Closing terms | Two mandatory climax terms, one guaranteed to each man. |
| Final scene | Every signed term performed in order, one step at a time, with timers. The closing terms are always last. |

**Key structural fact:** the final scene performs the terms that were *signed during
negotiation*, in a running order. A term that cannot be proposed can never reach the finale.
Anything that must happen only at the very end therefore has to be a **closing term**, not a
regular one.

---

## 2. Repo layout

```
core/    Pure Kotlin. Game engine, content library, view builder, server, persistence.
         No Android dependencies — this is where almost all logic and all content lives.
app/     Android TV app. Compose UI, foreground service, on-device narration (sherpa-onnx).
model/   The TTS voice model (large binaries, fetched at first run if absent).
```

Files worth knowing:

| Path | What it is |
|---|---|
| `core/.../content/TermsLevel1-5.kt` | The regular term library, one file per intensity level. |
| `core/.../content/ClimaxTerms.kt` | The closing terms. The only place intercourse and orgasm belong. |
| `core/.../content/Considerations*.kt` | Consideration actions (mutual / non-sexual / sexual). |
| `core/.../content/TermDsl.kt` | The `t()`, `c()` and `tm()` constructors. |
| `core/.../style/Lexicon.kt` | Verb and adverb tokens, four registers each. |
| `core/.../model/Preferences.kt` | The private-profile questions. |
| `core/.../engine/Selectors.kt` | Which term is proposed next, and the alternation rule. |
| `core/.../engine/Eligibility.kt` | Boundary, profile and role filtering. |
| `core/.../engine/FinaleOrdering.kt` | The running order of the final scene. |
| `core/.../validation/ContentValidator.kt` | Every content rule that can be automated. |
| `app/.../ui/TvCanvas.kt` | The TV design canvas and the fit-to-height guard. |
| `app/proguard-rules.pro` | R8 keep rules. See §8 — this file has broken the app twice. |

### Commands

```bash
./gradlew :core:test                 # engine + content rules (fast, run this constantly)
./gradlew :app:testReleaseUnitTest   # app-side unit tests
./gradlew :app:assembleRelease       # APK -> app/build/outputs/apk/release/
./gradlew :core:test --tests "*PlaythroughReadout*" --rerun-tasks
                                     # writes core/build/playthrough-readout.txt
```

Gradle works offline (`--offline`). Release builds are signed if `keystore.properties` exists.

---

## 3. The content model

A **term** is one negotiated item. A **consideration** is what the man who gains less performs to
pay for it. Both are written once and rendered four ways by the explicitness register the couple
chose, so every instruction is authored as a `base` (measured) and an `explicit` (coarse)
template, with lexicon tokens filling in the register-specific verbs.

```kotlin
t(
    id = "l3_example_thing", level = 3, cats = setOf(ORAL),
    title = "Two minutes each way",
    base = "{G} [v_suck] {R} slowly for two minutes, then hard for two more.",
    explicit = "{G} [v_suck] {R+} cock slowly for two minutes, then as hard as he can for two more.",
    benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
    acts = setOf("oral", "harder_oral"),
    timers = listOf(tm("Slow", 120), tm("Hard", 120))
)
```

**Placeholders:** `{G}` giver, `{R}` receiver, `{DOM}` / `{SUB}` by role; a trailing `+` makes it
possessive (`{R+}` → "Chris's"). `[v_xxx]` / `[adv_xxx]` are lexicon tokens. `#equipment#` names a
piece of kit.

**`acts`** lists activity ids. Each must have a `<activity>_give` and `<activity>_receive`
preference in `PreferenceLibrary`, because eligibility looks both sides up by id. Never invent an
activity id without adding the preference pair.

---

## 4. Content rules — terms and consideration actions

These are the standing criteria. Most are enforced by `ContentValidator` and `RenderSweepTest`;
the ones marked *(read)* can only be caught by reading the rendered output.

### 4.1 Be specific — an instruction must be performable without interpretation

* **Never tell a man to improvise.** No "carries on with something else", "as he likes", "as he
  wants", "of his choosing", "and then some". Name the act.
* **Say how many, where, and for how long.** "Asks before he touches him" is not an instruction;
  "puts a hand on him five times — chest, stomach, the back of the neck, thigh, then his cock —
  asking before each and holding a slow count of ten" is.
* **Endpoints must be countable.** No "until it stops stinging", "long enough", "for a while",
  "until he is ready". Use a timer, a count, or a named number of repetitions.
* **A term that only forbids something is not a term.** If the instruction is "he keeps his hands
  off himself", it must also say what the pair are *doing* for those three minutes.
* **Both men must be named at least once** in each template. A template that only ever says "him"
  is ambiguous on screen.
* **Mutual terms take plural subjects.** Lexicon verbs render singular ("works his mouth over"),
  so "X and Y [v_suck] each other" comes out ungrammatical. Write mutual instructions in plain
  words.

### 4.2 What is reserved for the end of the final scene

* **Intercourse — a man putting his cock in the other man's arse — only ever appears in closing
  terms** (`ClimaxTerms.kt`, `climax = true`). No regular term may carry the `topping` or
  `bottoming` activity. Fingers, tongues, toys and fisting are *not* covered by this rule; it is
  about intercourse specifically.
* **Nobody comes before the end of the final scene.** No regular term may make an orgasm its
  endpoint. Where a term would have ended in one, it stops at "and he does not come."
  A term that merely *rules* he must ask permission before finishing is fine — that rule is what
  the closing term later honours.

`FinaleOnlyPenetrationTest` holds both of these.

### 4.3 Suggestion lists

* A term may carry `says` / `saysExplicit` (things to say) or `positions`. If it carries any, it
  must carry **exactly five**, distinct, each ending in sentence punctuation.
* The instruction must actually ask for the thing in **both** registers — a list of lines to say
  on a term that never tells anybody to talk is a list with no instruction attached.
* **The heading is fixed** at "Things he could say — suggestions only" and attaches to the man the
  instruction leads with. If the list belongs to the *other* man, rewrite the instruction so he is
  the subject. *(read)*
* Where a term is about answering questions, give five example questions.

### 4.4 Naming what is happening

* **Oral content must name the mouth** — mouth, lips, tongue, throat, sucks, swallows — in every
  register.
* **Penetration must name what goes in** — cock, fingers, hand, fist, dildo, plug, toy, tongue.
* **No female anatomy or gendered wording anywhere.** Both players are men.
* **Forbidden entirely:** choking, strangling, asphyxiation or any breath restriction; anything
  suggesting unconsciousness, intoxication or minors; leaving a restrained man alone; anything in
  public or in front of others.
* **Never write an instruction that removes a man's ability to stop.** "Without pulling off once"
  during hard oral is the failure case — the stop word exists, and the text should not be telling
  him to override it. Say he comes off to breathe. *(read)*
* **Physical plausibility.** Full bodyweight belongs on a back or shoulders, never a neck.
  *(read)*

### 4.5 Benefit and fairness

* `benefit` names which side gains: `GIVER`, `RECEIVER`, or `MUTUAL`. `type` is the specific
  benefit kind.
* **Penetration credits the man taking it.** Declare `acts = setOf("topping")` only —
  adding `"bottoming"` alongside it flips the credit onto the wrong man. `BenefitRuleTest`
  catches this.
* Closing terms must have `benefit = RECEIVER` (the receiver is the man guaranteed to finish) and
  must contain an explicit continuation if the timer runs out ("if the timer ends first, he keeps
  going until he is done").

### 4.6 Lexicon tokens carry their own grammar

Check the entry in `Lexicon.kt` before using a verb — several already include their object or
preposition, and the validator will not catch the result.

| Token | Renders as | Correct use |
|---|---|---|
| `[v_edge]` | "edges him" | `[v_edge] three times` — **not** `[v_edge] {R}` |
| `[v_finger]` | "works a finger into" | `[v_finger] {R}` — **not** `... with one finger` |
| `[v_insert]` | "slides in" | `[v_insert] #anal_plug#` — **not** `... into {R}` |
| `[v_use_toy]` | "uses" / "drives" | `[v_use_toy] a toy in {R}` |
| `[v_suck]` | "puts his mouth on" | `[v_suck] {R+} cock` |
| `[adv_kiss_deep]` | "...as deep as he can" | singular subject — unusable in mutual terms |

### 4.7 Library size floors

`ContentValidator` requires at least 202 regular terms, 120 considerations, 132 preferences, and
per level: **L1 42, L2-L5 40 each.**

Current: **L1 50, L2 48, L3 40, L4 47, L5 41; 40 closing terms; 170 considerations.**

> **Level 3 sits exactly on its floor.** Removing any level-3 term requires adding a replacement
> in the same commit.

---

## 5. The safety model

Not a bolt-on — most of the architecture.

* **13 hard boundaries** (no anal, no pain, no marks, no restraints, no degradation, …). Which
  boundaries block a term is **derived** from its categories, activities and equipment rather than
  tagged per term, so a new term cannot accidentally slip past one.
* **151 private preferences**, 95 of which are asked (§6). Directional: yes to giving is a
  separate answer from yes to receiving. **A single No from either man removes the term.**
* **Filtering is silent.** A No or missing equipment removes content without ever showing why —
  nobody learns what the other man declined. Boundary conflicts are the exception and may be
  named.
* **Maybe is a negotiated yes**, not a soft no: it attaches a condition that rewrites the
  instruction (gentler, under two minutes, roles reversed, stop before orgasm, …).
* **Role limits with memory.** `receptiveAnalLimit` counts across the whole night — a top's
  allowance is zero, a vers top's is exactly one.
* **Unilateral veto.** Either man alone rejects a term; it leaves the pool and costs no progress.
* **A mandatory stop word**, plus a global pause wired to the remote's media keys that bypasses
  version checks and pauses every timer. Resuming needs both phones.
* **Privacy is structural.** The TV view never contains a profile answer or an unsubmitted
  selection — not hidden, simply absent from the object the TV is handed.

Known gaps, if asked: there is no aftercare stage and no periodic check-in; the stop word is
displayed but not modelled in software (the pause button is the enforceable mechanism).

---

## 6. Engine invariants worth not breaking

* **Alternation.** The man who came out ahead on the last one-sided term is not the man the next
  is bound to favour. It moves on a **signature**, not a proposal — a rejected term leaves the
  debt where it was. Balanced terms name nobody and pass no turn. It is a *preference* the
  selector drops if nothing eligible survives with it. `BenefitAlternationTest` plays real games.
* **A trade is one contract item and two execution steps.** The two halves routinely have giver
  and receiver reversed, so each step carries its own instruction, clocks, controller and
  completer. They stay adjacent in the running order. `BundledTermExecutionTest`.
* **Assumed preferences.** 56 of the 151 preferences are no longer asked and answer `YES`
  regardless of what is stored — a stale `NO` from an older profile must not survive. The records
  stay in the library because terms reference those activities by id. Two sections (Language,
  Orgasm control) are empty and are not shown. `AssumedPreferenceTest`.
* **Persistence tolerates old saves.** `StoreJson` uses `ignoreUnknownKeys`, and a failed decode
  drops the session silently. When changing a persisted field, **rename rather than retype** so the
  old key is skipped instead of failing to parse, and provide a rebuild path.

---

## 7. Android TV specifics

* **Design canvas is 1280 x 720.** `TvCanvas` ignores the set's reported density and derives one
  that makes the window come out at that size, so 720p, 1080p and 4K all lay out identically.
  Changing `DESIGN_WIDTH` / `DESIGN_HEIGHT` rescales the whole app.
* **Safe area is 5% of each axis**, derived from the canvas (`TvLayout.safeAreaX/Y`) — not one
  figure for both.
* **Nothing in the content column can take focus**, so a remote cannot scroll it. Anything that
  overflows is *gone*. `FitToHeight` shrinks the column by lowering the density rather than
  clipping. `TvLayoutBudgetTest` guards the arithmetic.
* The accessibility text scale is deliberately not carried through.

---

## 8. Traps that have already cost real debugging

**R8 strips what only native code uses.** Both of these were process aborts, not exceptions, so no
`try/catch` anywhere could have caught them, and both only appeared in release builds:

1. `sherpa-onnx` reads its config object's fields from C++ by name. R8 renamed them →
   `JNI DETECTED ERROR: fid == null`. Fixed by keeping `com.k2fsa.sherpa.onnx.**`.
2. The audio callback was a Kotlin lambda. Lambdas compile through `invokedynamic`, and the
   desugared class carries only the erased `invoke(Object)Object` — the specialised
   `invoke([F)Ljava/lang/Integer;` the native side looks up **was never in the APK at all**, so no
   keep rule could preserve it. Fixed by declaring `ChunkSink` as a real class.

> When touching narration or ProGuard rules, verify against the **built APK's method table**, not
> the rules file. A keep rule that looks right proves nothing.

**Narration performance.** Four threads, `SAMPLING_STEPS = 3`, and a short lead-in segment so
speech starts before the whole passage is generated. `SAMPLING_STEPS` is the quality/speed dial.

---

## 9. How to verify content changes

1. `./gradlew :core:test` — the validator, the render sweep, the benefit rules, the playthroughs.
2. Regenerate and **read** the readout:
   `./gradlew :core:test --tests "*PlaythroughReadout*" --rerun-tasks`
   → `core/build/playthrough-readout.txt`

The second step is not optional for content work. The suite catches everything expressible as a
rule; it cannot tell you whether an instruction can be carried out by two men in a room. Every
issue marked *(read)* above was found by reading rendered output and would have shipped otherwise.

When adding terms, render them at the mildest and coarsest registers and read both — the coarse
register substitutes different verbs and is where grammar breaks.
