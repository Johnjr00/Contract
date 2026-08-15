# The Contract

A sexual Android TV app/game for two consenting adult gay men. The television is the shared display and hosts a
small server on the local network; each player scans one QR code and plays from the browser on
his own phone. Everything runs offline — there is no cloud backend, no analytics, no external
API, no CDN and no runtime download of any kind.

> Adults only. Both players are assumed to be consenting adults who have discussed what they
> are doing before starting.

---

## How the game works

You start on Player 1's phone, setting up together: names, roles, anal roles, length, filth level of wording, hard limits, stop word. Then you separate and each answer your private profile questions — these are your own preferences — yes, maybe or no, giving and taking asked separately. Neither man ever sees the other's answers; anything either of you declines just quietly stops appearing in the game.

Then the TV proposes terms — specific sexual acts, written out in full with timers attached. You each answer privately: sign, amend, trade or reject it. When a term is agreed upon, it gets added to the final "contract" sceen to be performed at the end. Whoever gains more from a term has to buy the other man's signature first, performing a consideration action on the spot before the term is officially signed — a smaller act, picked for him from a private list by the man who gains less from the term. Finish adding terms to fill the contract, then add two closing terms (a guaranteed climax for each player), and the final contract scene runs the terms you agreed on in order, closing terms last.

Why "consideration"? In real contract law a promise alone isn't binding — each side has to give something of value in exchange. That's consideration, and it's the difference between a contract and a wish. Same here: no signature comes free, and the man with the most to gain buys it with his mouth or his hands first. Pay now, fuck later.

## What you need

- A TV running Android TV or Google TV with the app open, and **a phone each** (iPhone or Android, doesn't matter), on the same Wi-Fi.
- Lube, towels, water, and whatever kit you actually own. Tick the equipment list honestly — the game silently never mentions anything you haven't got, so you won't be handed an instruction involving a spreader bar that exists only in your browser history.

Scan the QR code on the TV. First phone in is Player 1, second is Player 2. That's the whole pairing ritual.

## Step 1 — Setup

Player 1's phone inputs the game settings:

- **Names**, and who's **Dominant** and who's **submissive**.
- **Anal role** each — top, vers-top, vers, vers-bottom, bottom, or none. Be truthful. "Top" means nothing goes near his ass all night; "vers-top" means exactly one thing does, once, in the entire game.
- **How filthy the writing is** — four registers, from politely erotic up to the sort of language you'd normally only use with a hand already down someone's shorts. This changes the wording, not the acts.
- **How long** — 10, 15, 20 or 25 terms, plus two closing ones. Ten is an evening. Twenty-five is a hostage situation you've consented to.
- **A stop word.** Something you'd never shout by accident. "Red" is the default. 
- **Hard boundaries** — thirteen checkboxes. Anything you check never appears in any form for that game.
- Whether the **TV reads it aloud**. It will use Google Speech Synthesis or whatever TTS provider you have set up in your TV's accessibility settings.

## Step 2 — Your private profile

You each answer about seventy questions on your own phone: **Yes / Maybe / No**, and what you'll give is a separate question from what you'll take.

1. **Nobody ever sees your answers.** Not the TV, not his phone, not at the end. They're never sent anywhere.
2. **One No kills it.** Either man's No removes that thing from the whole game, and the other man is never told why it didn't come up. No conversation, no negotiation, no face.
3. **Maybe isn't a shrug** — it's "yes, but." The game takes your condition (gentler, under two minutes, roles reversed) and rewrites the instruction to honour it.

## Step 3 — Negotiation (the long bit)

The TV proposes a term. You each answer privately. **Neither answer shows until both are in**.

| Button | What it does |
|---|---|
| **Sign it** | Yes. Goes to consideration, then into the contract. |
| **Counteroffer** | Yes, but amended — gentler, shorter, roles reversed, no toys, no restraint, massage first, save it for the finale. |
| **Trade** | Bundle it with a second term as one package. Two slots, and it costs you more. |
| **Reject** | Gone. Instantly, unilaterally, no reason, no cost. |

**Reject is free.** One man alone kills a term, it leaves the pool for the game, and it doesn't cost you a slot or an inch of progress. 

If you both counteroffer and want different changes, you vote between the two. Fail to agree and the term dies where it stands.

**A signed term is not performed now.** It goes onto the contract, and the contract is performed in full at the end. You are negotiating in explicit detail for a sexual scene you've both agreed to have later.

## Step 4 — Consideration: happening right now, before he signs

This is the one part of negotiation that is not a promise. It's live.

- The game names the man who gains more from the term.
- The man who gains **less** picks the payment from a private list. The TV shows nothing while he chooses.
- The man who gains **more** performs it **immediately**.
- Then the man who's owed presses **"Signature earned"** — or **"again."**

Only once the consideration action is finished does the term get signed onto the contract. 

The game also keeps score. Sign something that favors one of you and the next proposal leans the other way. Rejecting doesn't move that needle — only signing does.

## Step 5 — Closing terms

Once all terms are signed, you each pick **how you finish** from a private list, and the other man votes it in. And yes, you pay a consideration for that too, on the spot.

## Step 6 — The final scene

Now the contract is performed. Everything you signed, in order, one step at a time, with the TV running the clock and reading your contract back to you.

You'll pick a running order first: as signed, gentlest-to-hardest, or shuffled. Disagree and the Dominant decides — perks of management.

The two closing terms are **always last**, and nothing can move them earlier. That's the whole design: your first signed term was probably a shoulder rub, and by the end you'll have talked yourself, in writing, into something you'd have been too shy to ask for out loud two hours and one hard-on ago.

## Five things people get wrong

1. **Rushing the profile.** See above. Do it properly.
2. **Overstating the anal role.** "Vers" written in optimism is a rough way to discover you meant "vers-bottom."
3. **Treating Maybe as No.** 
4. **Forgetting it's cumulative.** By term fifteen you've signed a contract, not floated an idea.
5. **Lube out of reach.** The app thought of everything except your bedside table.

The negotiation is the game. The final scene is just the contract being honored — and it's the only one either of you will sign where reading the small print carefully makes the ending better and considerably wetter.



---

## 1. Project architecture

Two Gradle modules:

| Module  | What it is | Depends on |
| ------- | ---------- | ---------- |
| `:core` | Pure-JVM Kotlin. The content library, style engine, rules engine, server-authoritative state machine, wire protocol, timer engine, embedded HTTP + WebSocket server and the phone-controller web assets. | Maven Central only |
| `:app`  | The Android TV application. Compose UI, Room persistence, Keystore encryption, the foreground service that hosts `:core`'s server, network-interface detection, QR rendering and the boot receiver. | Android SDK + Google Maven |

Almost all behaviour lives in `:core`, which has no Android dependency at all. That is a
deliberate choice: it means the whole system — including three complete end-to-end games with
two simulated phones over real sockets — is exercised by an ordinary JVM test suite, and the
Android module is left as a thin, mostly declarative layer.

`:app` is only wired into the Gradle build when an Android SDK is actually present
(`settings.gradle.kts` checks `local.properties` / `ANDROID_HOME` / `ANDROID_SDK_ROOT`), so
`./gradlew :core:test` works on a bare JDK.

```
core/src/main/kotlin/com/thecontract/core/
├── model/         domain types: terms, preferences, equipment, boundaries, session state
├── content/       the authored library (202 terms, 28 closing terms, 124 consideration actions)
├── style/         explicitness lexicon and the token renderer
├── engine/        eligibility, benefit analysis, selection, rendering, timers, state machine, views
├── protocol/      wire messages and the server-driven view model
├── server/        session manager and the NanoWSD HTTP/WebSocket server
├── persistence/   StateStore interface + JSON-file and in-memory implementations
└── validation/    the content validation suite
core/src/main/resources/web/    controller.html / .css / .js — the phone controller

app/src/main/kotlin/com/thecontract/tv/
├── data/          Room database, Keystore crypto, RoomStateStore
├── net/           ConnectivityManager-based interface monitoring
├── service/       foreground service hosting the server, boot receiver
└── ui/            Compose television surface and remote-control panel
```

### Content

| | Count |
| --- | ---: |
| Proposed contract terms | **202** (level 1–5: 42 / 40 / 40 / 40 / 40) |
| Closing (climax) term options | **28** |
| Consideration actions | **124** |
| Private preferences | **147** |
| Equipment options | **28** |
| Shared hard boundaries | **13** |
| Explicitness lexicon entries | **73**, each with four registers |

Explicitness is not a word swap. Every term and consideration action is authored twice — a
measured register and a coarse register with different sentence structure and command framing —
and a 73-entry lexicon then rewrites verbs, adverbs, commands, dominance phrasing, oral, anal,
rough-sex, orgasm and ownership language differently at each of the four levels. Extremely
filthy additionally appends an authored tail clause. Anatomy is written literally in the
templates and is deliberately *not* the mechanism of differentiation.

---

## 2. Local-server architecture

The TV runs a [NanoHTTPD/NanoWSD](https://github.com/NanoHttpd/nanohttpd) server inside a
foreground service.

```
        Android TV (server, authoritative)
        ┌──────────────────────────────────────────────┐
        │ ContractService  (foreground, specialUse)    │
        │   ├── ContractServer   :8765 (fallback 8766…)│
        │   │     GET  /join/<token>   controller page │
        │   │     GET  /assets/*       bundled JS/CSS  │
        │   │     GET  /api/health                     │
        │   │     WS   /ws?token=<token>  protocol     │
        │   ├── SessionManager   slots, tokens, tick   │
        │   ├── GameEngine       state machine         │
        │   └── RoomStateStore   encrypted persistence │
        │ MainActivity → renders the published TV view │
        └──────────────────────────────────────────────┘
              ▲                              ▲
              │ ws + http (LAN, plain)       │
     ┌────────┴────────┐            ┌────────┴────────┐
     │ Player 1 phone  │            │ Player 2 phone  │
     │ browser page    │            │ browser page    │
     └─────────────────┘            └─────────────────┘
```

* **Binding.** The server binds every local interface for reliability, but the QR code only ever
  advertises the interface the TV user selected.
* **Port.** 8765 by default, falling back through 8766–8769. The advertised URL always carries
  the port actually bound.
* **Interfaces.** Loopback, down interfaces and IPv6-only interfaces are ignored. Private
  addresses outrank link-local (169.254/16), Ethernet outranks Wi-Fi, and VPN interfaces rank
  last so they are never selected automatically — the TV user can still pick one. Address
  changes arrive through `ConnectivityManager` callbacks, which re-rank the list, regenerate the
  join URL and QR code, and leave the game state untouched.
* **No outbound traffic.** The app makes no network request of any kind. `INTERNET` is held only
  to open a listening socket. The controller page references nothing but `/assets/*`, which is
  asserted by a test.
* **Honest about transport.** The LAN connection is plain HTTP. The app says so on screen rather
  than implying encryption. The unguessable 256-bit join token is the access mechanism.

### Phone controller

`controller.html` / `.css` / `.js`, bundled in the artifact and served from it. An ordinary
browser page: no service worker, no web manifest, no install prompt, no native shell. The server
sends a finished description of the screen (heading, body, cards, choices, timers) and the page
renders it — the client never infers state, which is what makes the privacy guarantees
structural rather than conventional.

---

## 3. Multiplayer state model

The TV is the single source of truth. Phones send actions; they never mutate anything.

```
phone ──PLAYER_ACTION{actionId, expectedVersion, action}──▶ server
                                                             │ validate phase
                                                             │ check version
                                                             │ dedupe by actionId
                                                             │ mutate + version++
                                                             │ persist
      ◀──ACTION_ACCEPTED / ACTION_REJECTED──────────────────┤
      ◀──STATE_CHANGED{view}  (to both phones, redacted)────┤
      ◀──TV view published to the activity ─────────────────┘
```

Message types: `HELLO`, `CLAIM_SLOT`, `RECONNECT`, `PLAYER_ACTION`, `PING` inbound;
`HELLO_OK`, `SESSION_FULL`, `RECLAIM_PENDING`, `STATE_SNAPSHOT`, `STATE_CHANGED`,
`TIMER_UPDATE`, `ACTION_ACCEPTED`, `ACTION_REJECTED`, `GLOBAL_PAUSE`, `ERROR`, `PONG` outbound.

**Every mutation** carries a unique client action id and the client's expected state version.
Stale versions are rejected; replayed ids are no-ops that do not even bump the version. That is
what prevents double signatures from a repeated tap, duplicate terms, duplicate consideration
receipts, two screens being skipped by one double input, timer races and one phone overwriting
the other's action.

The state machine covers the phases the specification names — `PAIRING`, `PLAYER_1_SETUP`,
`WAITING_FOR_PLAYER_2`, `PRIVATE_PROFILES`, `PROPOSAL`, `COUNTEROFFER_*`, `BUNDLE_*`,
`CONSIDERATION_*`, `WAITING_FOR_SIGNATURE_CONFIRMATION`, `TERM_SIGNED`, `CLOSING_TERM_*`,
`FINAL_CONTRACT_REVIEW`, `FINALE_ORDER_SELECTION`, `PRIVATE_DOMINANT_FINALE_SELECTION`,
`FINAL_EXECUTION`, `PAUSED`, `COMPLETED` — with explicit, validated transitions.

### Privacy

`ViewBuilder` is the only place a view is produced, and it builds a separate projection per
audience. A profile answer, an unsubmitted private selection or a private vote is simply never
serialised into the TV projection or into the other player's. Tests assert that no preference id
or label ever appears in any TV view across a whole game, and that the Dominant's private finale
choice never reaches the screen before submission.

### Back navigation

Back is a snapshot stack: each step pushes the previous authoritative state, and Back pops it.
That makes it structurally impossible for Back to duplicate a term, a vote or a receipt, or to
consume progress. The stack is truncated at every signature, so a signed term can never be
reopened, and timers are reset when leaving a timed screen.

### Timers

The TV server owns every clock. `remainingMs` is only rewritten at a transition; while a timer
runs, remaining time is derived from the server's start anchor, so a phone whose clock is
minutes out cannot change the result. Each timed segment gets its own timer — "shoulders 60s,
lower back 45s, each thigh 30s" is four timers, not one — and multi-timer screens offer Stop All.

---

## 4. Phone reconnection behaviour

Each phone stores a device id and a 256-bit resume token in `localStorage`. `HELLO` carries
both.

1. **Valid resume token** → straight back to that phone's own slot.
2. **Same device id, no token** (storage cleared, token lost) → back to its slot if no live
   client holds it.
3. **A free slot** → the first phone becomes Player 1, the second becomes Player 2.
4. **Both slots taken, one offline, unknown browser** → a `RECLAIM_PENDING` response. The slot is
   *reserved*, not reassigned; only a confirmation on the TV remote hands it over. No PIN.
5. **Both slots taken and live** → `SESSION_FULL`, and that device receives no game state at all.

A reconnecting phone can never become the other player. Refreshing, locking the phone, closing
and reopening the browser, losing Wi-Fi briefly or dropping the WebSocket all recover
automatically: the page reconnects with exponential backoff, on `visibilitychange`, on `online`
and on `pageshow`, and receives a full state snapshot each time. Actions are idempotent, so an
action in flight when the socket died is safe to retry.

If the TV's address changes, the QR code and join URL are regenerated, both slots are preserved,
and a phone that has to scan the new code reclaims its previous slot through the same flow.

From the remote the TV can release a disconnected slot, confirm a reclaim, restart pairing
without deleting the contract, and act for a disconnected player during a *public* step only.

---

## 5. Persistence behaviour

`StateStore` is a small interface implemented on Android by Room, with the payload encrypted
using AES-256-GCM under a key generated inside the Android Keystore and never extractable.
Cloud backup and device-to-device transfer are disabled; saved contracts stay on the TV.

The authoritative state is written through after **every** meaningful mutation, so an unfinished
game survives activity recreation, process death, a server restart, an app relaunch, a temporary
network loss and a TV reboot. Persisted material includes the active session, names, roles,
reconnect tokens, shared setup, both private profiles, boundaries, equipment, seen/declined
terms, signed terms, amendments, bundle trades, consideration receipts, closing terms, the
current negotiation state, the Back stack, the finale order, the current execution step, timer
definitions and timer state.

Timer countdowns are deliberately **not** written every second. Timers are persisted when
started, paused, reset or completed, when leaving a timed screen, when the app is backgrounded
and when the service is shutting down. After a restart a running timer comes back **paused** at
the last safely persisted value — a sexual-action timer must never keep counting while the app
is unavailable.

On launch, if an unfinished session exists the TV offers Resume or Start New. It is never
silently destroyed; replacing it is an explicit choice. A corrupt session file is ignored rather
than crashing the launch, which is covered by a test.

---

## 6. Build commands

```bash
# Everything that does not need the Android SDK — content validation, three end-to-end
# multiplayer games, protocol and network tests over real sockets, persistence tests.
./gradlew :core:test

# The content validation suite on its own.
./gradlew validateContent

# The Android TV app (requires the Android SDK and access to Google Maven).
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

Full build and sideloading instructions, including the release-signing procedure, are in
[BUILD.md](BUILD.md). A record of what was verified and what was not is in
[IMPLEMENTATION_REPORT.md](IMPLEMENTATION_REPORT.md).

---

## 7. Release-signing procedure

1. Generate a keystore (once — keep it safe; losing it means you cannot update the app):

   ```bash
   keytool -genkeypair -v \
     -keystore the-contract-release.jks \
     -alias the-contract \
     -keyalg RSA -keysize 4096 -validity 10000
   ```

2. Create `keystore.properties` in the project root:

   ```properties
   storeFile=the-contract-release.jks
   storePassword=<store password>
   keyAlias=the-contract
   keyPassword=<key password>
   ```

3. Build:

   ```bash
   ./gradlew :app:assembleRelease
   # → app/build/outputs/apk/release/app-release.apk
   ```

`keystore.properties` and `*.jks` / `*.keystore` are git-ignored and must never be committed. If
`keystore.properties` is absent the release variant still assembles, unsigned, so the build never
depends on a secret being present.

---

## 8. What the app deliberately does not do

No spectator mode, no third player, no audience mode, no joining over the internet. No cloud
storage, analytics, advertising, crash reporting or telemetry. No PWA install, service worker or
app-install banner on the phone side. No remote fonts, scripts or images. Content generation
excludes choking, breath restriction, neck compression, unconsciousness, head strikes,
intoxication-based consent, unattended restraint, activities requiring injury, non-consensual
conduct, minors, and exposure involving anyone who is not playing — enforced by the automated
content validation suite, not just by convention.
