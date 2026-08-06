# The Contract

An Android TV app for two consenting adult men. The television is the shared display and hosts a
small server on the local network; each player scans one QR code and plays from the browser on
his own phone. Everything runs offline — there is no cloud backend, no analytics, no external
API, no CDN and no runtime download of any kind.

> Adults only. Both players are assumed to be consenting adults who have discussed what they
> are doing before starting.

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
