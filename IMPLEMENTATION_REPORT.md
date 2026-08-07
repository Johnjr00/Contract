# Implementation report

Generated from the repository at the tip of `claude/contract-android-tv-app-1ogltr`.

---

## 1. Artifacts

Both APKs were built and verified.

| | |
| --- | --- |
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` |
| | application id `com.thecontract.tv.debug` |
| | SHA-256 `2161f0e661c7683b28e677c2566721e7afa3b544ab156e784b7c5d9007dbb6e8` |
| Release APK | `app/build/outputs/apk/release/app-release.apk` |
| | application id `com.thecontract.tv`, minified and resource-shrunk by R8 |
| | SHA-256 `f821653b5d9703605f8301a46321aef55a01b5090dcc664d27b3ca9d0e44a6df` |
| Release signature | APK Signature Scheme v2, verified with `apksigner verify` |
| | signer `CN=The Contract, OU=TheContract, O=TheContract, L=Unknown, ST=Unknown, C=US` |
| | certificate SHA-256 `d460e29876eda8d73e6b1af100f78942b26ac8ab28d33aaa7f42ca605bef25e0` |

These are the hashes as of the reconnect fix in "On-device quick round on the Android emulator"
in section 4b below. The signing cert is unchanged from the previous fix (same keystore).

Toolchain actually used: AGP 8.7.3, Kotlin 2.2.21, KSP 2.2.21-2.0.4, Compose BOM 2024.10.01,
Room 2.6.1, Android SDK Platform 35, Build-Tools 35.0.0, Gradle 8.14.3, JDK 21 emitting Java 17
bytecode.

Verified on the built artifacts rather than asserted from source:

* `aapt2 dump badging` reports `leanback-launchable-activity` for
  `com.thecontract.tv.ui.MainActivity` with the TV banner — the Android TV launcher intent is
  wired correctly.
* `uses-feature-not-required` for `android.hardware.touchscreen`, plus camera, microphone and
  gamepad; `uses-feature: android.software.leanback` is required. The app declares television
  support and does not require a touchscreen.
* `targetSdkVersion` 35, `compileSdkVersion` 35.
* Permissions in the shipped manifest are exactly the eight declared — `INTERNET`,
  `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `FOREGROUND_SERVICE`,
  `FOREGROUND_SERVICE_SPECIAL_USE`, `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK`,
  `POST_NOTIFICATIONS` — with no analytics, advertising or tracking permission of any kind.
* The phone controller survives R8 intact inside the release APK: `web/controller.html` (1,084 B),
  `web/controller.css` (7,802 B) and `web/controller.js` (33,078 B) are present and byte-identical
  to source, and a grep across all three finds **zero** occurrences of `http://`, `https://`,
  `cdn` or `googleapis`. Nothing is fetched from outside the APK.
* Release `classes.dex` carries 15,355 method references — no multidex needed.

### Note on the signing key

The release APK is signed with a self-signed keystore generated in the build container
(`the-contract-release.jks`, alias `the-contract`). It is deliberately **not** committed — `*.jks`
and `keystore.properties` are git-ignored. For anything you intend to keep updating, generate
your own keystore and back it up; the procedure is in BUILD.md. Reinstalling over this APK with
a differently-signed build requires `adb uninstall com.thecontract.tv` first.

### Build fixes this uncovered

Compiling `:app` for the first time surfaced problems that source review had not:

1. **Plugin classloader split.** Declaring the Kotlin plugin on the root build classpath while
   AGP resolved only in `:app` made `kotlin-android` fail with
   `NoClassDefFoundError: com/android/build/gradle/api/BaseVariant` — the Kotlin plugin reflects
   on AGP types and the two must share a classloader. Fixed by removing the root `plugins {}`
   block so each module resolves its own; this also keeps AGP off the classpath entirely when no
   Android SDK is present and only `:core` is configured.
2. **`fallbackToDestructiveMigration(dropAllTables = true)`** does not exist in Room 2.6.1 (it
   arrives in 2.7). Reverted to the no-argument overload.
3. **A Kotlin default parameter value on a Room DAO method**, which Room cannot generate against.
   Removed, with the constant passed at the call site.
4. **A `kotlin { }` block nested inside `android { }`**, which AGP 8.7 does not expose. Moved to
   the top level.

Items 2–4 were found and fixed by review before the SDK became available; item 1 could only be
found by actually running the build.

### Real-device crash found and fixed

The APK described above actually built and ran `apksigner verify` clean, but crashed instantly
on real Android TV hardware. That is a class of bug none of the earlier verification could catch
— the JVM test suite runs against `JsonFileStateStore`, not `RoomStateStore`, and the live-browser
verification in 3a drives the same `JsonFileStateStore`-backed dev server, so Room never entered
the picture in either case.

With the emulator back up, the crash reproduced identically there, giving a real stack trace:

```
FATAL EXCEPTION: main
java.lang.RuntimeException: Unable to create service com.thecontract.tv.service.ContractService:
java.lang.IllegalStateException: Cannot access database on the main thread since it may
potentially lock the UI for a long period of time.
	at com.thecontract.tv.service.ContractService.onCreate
```

Root cause: `ContractService.onCreate()` called `AndroidNetworkMonitor(...).start()` synchronously
on the main thread (`Service.onCreate()` always runs there). `start()` immediately calls
`SessionManager.refreshInterfaces()`, which broadcasts a TV view — and building that view reads
saved session and contract state from Room. Room refuses to run a query on the main thread, so the
service died on every single launch, unconditionally.

Two more instances of the same mistake were reachable but hadn't yet been hit: `onTaskRemoved()`
and `shutDown()` (called from `onDestroy()` and the `ACTION_STOP` path) both called
`manager.persistNow()` — a Room write — directly, also on the main thread.

Fixes, all in `ContractService.kt`:
* The network monitor's construction and `start()` call moved inside the existing
  `scope.launch { }` (`Dispatchers.Default`), alongside the server startup that was already
  correctly off the main thread.
* `onTaskRemoved()` and `shutDown()` now wrap `manager.persistNow()` in
  `runBlocking(Dispatchers.IO) { }` — these two call sites need the write to have landed before
  the function returns (the process may be killed right after), so they block the main thread
  deliberately, but the query itself now executes on an IO-dispatcher thread rather than main,
  which is what Room's check actually requires.

Verified fixed the same way it was found: reinstalled the rebuilt release APK on the emulator,
launched it, and confirmed via `logcat` that the service starts, the foreground notification
posts, and the TV surface renders (screenshot on file) with no `FATAL EXCEPTION` over a sustained
run.

A second, different failure appeared intermittently during this same testing round — a foreground
service start timeout (`Context.startForegroundService() did not then call
Service.startForeground()`) — but `startForeground()` is literally the second statement in
`onCreate()`, and `logcat` showed class verification running at 21–26,000 bytecodes/second on this
sandbox's unaccelerated (no KVM) emulator, i.e. slow enough on its own to blow through the OS's
short foreground-service-start window. This did not recur on the rebuilt release APK's
verification run. It reads as an artifact of this specific software-only emulator rather than a
code defect — real Android TV hardware has none of that verification overhead — but it hasn't been
confirmed on real silicon, so if a foreground-service crash of that shape turns up on real
hardware, treat this as unresolved rather than closed.

Because the previous keystore (`the-contract-release.jks`, git-ignored by design) was lost earlier
in this same work — deleted by an unrelated `git clean -fdx` run while resolving an unrelated
GitHub PR problem — the release build above is signed with a newly generated keystore, and
therefore a new signing certificate. Anyone who installed the earlier release build must
`adb uninstall com.thecontract.tv` (it never got far enough to write any user data) before
installing this one.

### Regex crash after both private profiles save

The previous fix let the app launch, but it crashed reliably as soon as both players finished
their private profiles — the exact point where the game engine builds and renders the first
proposed term. Reproducing it required actually playing through the app on the emulator: two
headless-Chromium "phones" driven by Playwright against the real, installed APK's embedded
server (via `adb forward`), joining with the real per-session token (read once via a temporary,
never-committed `Log.e` line, since the token is deliberately never logged in shipped code and the
QR/URL panel wasn't visible on this emulator's virtual network interface). `logcat` caught the
real crash:

```
FATAL EXCEPTION: NanoHttpd Request Processor (#7)
java.lang.ExceptionInInitializerError
Caused by: java.util.regex.PatternSyntaxException: Syntax error in regexp pattern near index 21
\{([A-Za-z0-9_]+\+?)}
                     ^
	at com.android.icu.util.regex.PatternNative.compileImpl(Native Method)
	...
	at g1.d.<clinit>
```

Root cause: `StyleEngine.kt` compiled its token-substitution regex as
`Regex("""\{([A-Za-z0-9_]+\+?)}""")` — a trailing, unescaped `}`. The JVM's own regex engine
(what every unit test and the desktop dev-server live-verification in 3a both run on) accepts a
bare `}` outside a quantifier as a literal character. Android's ICU-backed regex engine does not,
and throws `PatternSyntaxException` from the class's static initializer the first time
`StyleEngine` is touched — which is precisely when a term is first rendered, i.e. the instant both
profiles are saved and the game proposes its opening term. No test in this repository could have
caught this: it is specifically a JVM-regex-engine-vs-Android-ICU-regex-engine divergence, invisible
anywhere except a real Android runtime.

Fixed by escaping the offending brace, and the same latent issue in the adjacent lexicon-token
regex (`\[([a-z0-9_]+)]` → `\[([a-z0-9_]+)\]`) in both `StyleEngine.kt` and
`ContentValidator.kt`, before it could cause an identical crash on the next lexicon-key term. The
53-test JVM suite still passes unchanged (that engine never enforced the stricter rule either way).

Verified fixed by rerunning the same real-app repro end to end: both simulated phones complete
setup and their private profiles, and the TV successfully renders "Proposed term" with no crash
anywhere in `logcat` — confirmed on both the debug and release builds.

## 2. Counts

| Item | Count | Specification floor |
| --- | ---: | ---: |
| Proposed contract terms | **202** | 202 |
| — level 1 (Chemistry) | 42 | 42 |
| — level 2 (Access) | 40 | 40 |
| — level 3 (Privilege) | 40 | 40 |
| — level 4 (Authority) | 40 | 40 |
| — level 5 (Enforcement) | 40 | 40 |
| Closing (climax) term options | **28** | — |
| Consideration actions | **124** | 120 |
| Private preferences | **147** | 132 |
| Equipment options | **28** | 28 |
| Shared hard boundaries | **13** | 13 |
| Maybe conditions | 11 | 11 |
| Counteroffer amendments | 11 | 11 |
| Explicitness lexicon entries | 73 × 4 registers | — |

Consideration actions break down as 16 massage, 8 kissing, 15 ear play and 85 sexual, covering
every item the specification enumerates.

Counts are asserted by the test suite, so the build fails if the library ever drops below a
floor.

---

## 3. Android versions

| | |
| --- | --- |
| `minSdk` | **26** (Android 8.0) — the level at which notification channels and `startForegroundService` exist, and comfortably below any Shield running current Shield Experience |
| `targetSdk` / `compileSdk` | **35** (Android 15) |
| Java/Kotlin target | Java 17 bytecode, Kotlin 2.2.21 |
| Built and packaged against | Android SDK Platform 35, Build-Tools 35.0.0 |
| Tested on device | **None.** An Android TV emulator (API 30, no hardware acceleration available — no `/dev/kvm` in this environment) was installed and did reach `sys.boot_completed=1` under pure software (TCG) CPU + software GPU emulation, but the qemu process crashed shortly after boot, consistent with a headless SwiftShader/GL failure. A second attempt with `-gpu guest` was abandoned as not worth the time; see section 3a below for what was verified instead. |
| Tested on JVM | JDK 21 (Ubuntu), producing Java 17 bytecode — the full `:core` suite, 53 tests, plus a live run against a real browser (section 3a) |

The APKs are real, signed and structurally verified against the packaged artifact (section 1).
Claims that need a *running Android system specifically* — the foreground service surviving
Doze, Keystore key generation on device, Room schema creation, the boot receiver firing, Compose
D-pad traversal with a real remote — are compiled and packaged but not executed on Android.

### 3a. What was verified live instead: the real server, the real browser pages, two real phones

The Android emulator route was unreliable in this environment (no hardware acceleration), so
rather than keep fighting it, the actual runtime behaviour was verified a different way: the
exact same server code the APK ships — `SessionManager` and `ContractServer`, unmodified — was
run as a plain JVM process outside Android (`core/src/main/kotlin/com/thecontract/core/devserver/DevServerMain.kt`,
invoked as `./gradlew :core:run`), and the exact same bundled `controller.html/.css/.js` was
opened in **real, unmodified headless Chromium** (Playwright, the browser pre-installed in this
environment) in three separate browser contexts — two acting as the phones, one as an uninvited
third device. This is not a simulation: it is the shipped server binary and the shipped client
JS, talking over a real socket, in a real browser's JS engine and real `localStorage`.

Only the Android/Compose television shell and Android-specific services (foreground service,
Room, Keystore, boot receiver) are outside what this route can exercise — those remain
compiled-but-unexecuted per the table above.

What this run demonstrated, live, with the transcript and eleven screenshots kept at
`docs/live-verification/` (script: `browser-drive-script.js`):

* Player 1 connects and gets the real setup form; Player 2 connects and correctly sees a
  waiting screen with no setup access; a third browser is refused with the exact
  `SESSION_FULL` message and receives no `STATE_SNAPSHOT` at all.
* Player 1 fills in and submits the real HTML form — names, stop word, 10 of the boundary
  checkboxes (leaving equipment at zero) — and both phones transition to their private profiles
  simultaneously.
* Both phones use the real "Everything Yes" bulk control and "Save and finish", and both land on
  the first proposed term at the same moment.
* **The eligibility engine filtered live, correctly, on a boundary the test happened to select**:
  the screenshot shows `"A proposal was blocked by your shared boundary: No toys. It was
  replaced and nothing was used up."` — this was not scripted, it fell out of checking the
  boundary box and equipment being empty, exactly the section 21 behaviour the tests assert in
  isolation, now observed end to end.
* The proposed term rendered was `"Marcus kisses Dan deeply, leading with his tongue and setting
  the pace throughout."` — real name substitution, real style-engine output, a real library term
  chosen because it needed no equipment neither player had.
* The benefit explanation rendered as natural language with no numbers: `"Dan benefits more from
  this term because Dan is on the receiving end of the kissing, which is where the pleasure sits
  in this one. Dan therefore earns Marcus's signature."`
* Both phones signed; the state correctly forked — Marcus (not the beneficiary) saw a neutral
  waiting screen, Dan (the beneficiary) saw six real, varied consideration options, each
  correctly captioned "Dan performs this for Marcus."
* **Dan's browser tab was then reloaded** — the real equivalent of a locked phone or a closed and
  reopened browser tab — and came back to the *exact same* consideration screen, having resumed
  through the stored token in real `localStorage`, matching section 8 precisely.

One real, small bug was found and fixed by this exercise (not something the JUnit suite could
catch, since it never renders into a DOM): `render()` and `renderMessage()` rebuilt the heading
and body elements on every state update without carrying over their `id="heading"`/`id="body"`
attributes, so a page that had received at least one server message lost those hooks. It had no
visible effect on a real user — nothing else in the client reads those ids — but it is now fixed
in `controller.js`, and this is exactly the kind of thing that only shows up when the real
client actually runs in a real DOM.

---

## 4. Multiplayer scenarios tested

53 tests, all passing (`./gradlew :core:test`). Report at
`core/build/reports/tests/test/index.html`.

### Game 1 — broad profile (6 tests)
Both players Yes to everything, all 28 equipment items, Vers and Vers, Extremely filthy.
Exercised in one run: direct signatures, a rejection, a one-sided counteroffer, a both-sided
counteroffer with an amendment ballot, a bundle trade, Back out of a proposal, Back out of a
consideration choice, a refused consideration that was repeated, both closing terms, and guided
final execution. Verified: exactly 10 regular slots consumed and exactly 2 closing terms; no
duplicate term, receipt or signature; every signed term carries a consideration receipt; no
consideration action leaked into the contract; both players are guaranteed to finish;
beneficiaries assigned correctly and absent exactly for mutual terms; over 20 timers driven;
climax terms last in the running order; TV and both phones on the same state version.
Additionally: all three finale orders run to completion, a finale disagreement is resolved by
the Dominant, the uninterrupted format silently uses Smooth Escalation with levels ascending,
and a 15-term contract with two bundles still lands on exactly 15.

### Game 2 — mixed profile (5 tests)
Yes/Maybe/No answers, six equipment items, Vers Top and Vers Bottom, Player 2 with the
erection-difficulty option, No visible marks / No degradation / No foot play, private Dominant
finale, and Player 2 disconnecting mid-negotiation and reconnecting with his stored token.
Verified: the game completes; the slot shows as disconnected rather than being released, and the
returning phone gets its own slot back; no unavailable equipment appears in any signed term,
consideration or view; no marking equipment, no degradation and no foot content anywhere;
erection-dependent content never lands on Player 2 in a regular term, while receiving oral stays
available to him and erection-dependent content returns only in his own closing term, which
carries a written fallback; a Maybe condition demonstrably rewrites the instruction text and
timers rather than appending a contradictory note; and the Dominant's private finale choice never
appears in any TV view before submission.

### Game 3 — restrictive profile (3 tests)
Both players with erection difficulty, No anal activity, No anal penetration, No toys, No pain,
No rough sex, No degradation, No foot play, three equipment items, one uninterrupted finale —
with a simulated process death mid-negotiation and a second restart before the finale, both
through a real on-disk store. Verified: the session restores both times, both phones reclaim
their own slots with their tokens, profiles survive and stay private, the contract still
completes at exactly 10 + 2, Smooth Escalation is used automatically, consideration stays varied
in a very narrow pool, both closing terms are generated, and no anal, toy, pain, rough,
degradation or foot content leaks anywhere. A separate test confirms a restrictive couple still
has at least two usable closing options each, and another walks every TV view produced across a
whole game asserting that no preference id and no preference label ever appears.

### Protocol and pairing (20 tests)
First phone becomes Player 1, second becomes Player 2, third gets `SESSION_FULL` and no game
state; only Player 1 can submit the shared setup and Player 2 sees no setup form at all; the QR
code disappears once both phones connect; a refreshed phone returns to its own slot and cannot
become the other player; an unknown browser needs TV-remote confirmation to take an occupied
slot; the remote can release a slot and restart pairing without losing the setup; a replayed
action id is applied exactly once and does not even bump the version; a stale state version is
rejected; a second response from the same phone is ignored; only the performer controls the
consideration timers with the TV remote always allowed as backup, a second start is not a
restart, and timers run on the server clock; either phone can pause immediately, the paused
screen blames nobody, ordinary actions are refused while paused, and resuming needs both phones
— or the TV remote alone; pause works from a phone several versions behind; the draft opens and
closes back into the byte-identical prior state; Back never duplicates a vote, term or receipt
and cannot reopen a signed term; interface selection, address change and QR regeneration leave
the game state untouched; interface ranking ignores loopback, link-local and VPN; invalid join
tokens are refused and rate-limited per address; join tokens are long, URL-safe and unique; the
QR matrix encodes the join URL offline.

### Network, over real sockets (6 tests)
The controller page and its assets are served from the bundled resources with correct content
types; an invalid join token gives 403 and path traversal is blocked; the server falls back to
another port when the preferred one is taken and advertises the port it actually bound; two
phones join over a real WebSocket and a third is told the session is full and gets no state; the
heartbeat is answered; a phone that drops its socket keeps its slot and reclaims it on
reconnect.

### Persistence (7 tests)
Every meaningful mutation is written through, including reconnect tokens; a running timer is
restored **paused** at the last safely persisted value and the per-second countdown is never
written to storage; an unfinished session is offered as Resume or Start New on a cold launch and
is never silently destroyed; a completed contract can be saved, listed, reopened from a cold
store and deleted; abandoning removes it; a half-written session file does not corrupt the next
launch.

### Content validation (6 tests)
Section 44 in full: minimum counts and per-level distribution, unique ids, valid preference,
equipment, boundary and role references, positive timer durations, no unresolved placeholders in
any of the four registers across every legal binding, no forbidden content, no female or gendered
wording, no unavailable-item leak (each term is re-checked with each of its required items
removed, and every toy-using term must be blocked by the No-toys boundary), valid benefit
assignments, valid closing-term beneficiaries with a written fallback, grammar checks for
repeated words, malformed possessives, spacing before punctuation and mid-sentence capitals, and
lexicon integrity. Plus: every register produces different text, the bundled web assets contain
no external reference and no PWA machinery.

### 4a. Full manual playtest: three complete games, real client and server

Beyond the automated suite, three complete games were played start to finish against the real
dev server through the real `controller.html/.css/.js`, using two headless-Chromium tabs driven
by a purpose-built auto-player (not the JVM test harness) so every screen a real phone would
render — every proposed term, consideration option, amendment, closing term and the full final
draft — could be read and checked for sense, not just structurally validated. Setup, private
profiles, negotiation (including a rejection, one- and two-sided counteroffers with amendment
ballots, a bundle trade, and Back navigation), considerations, final execution and contract save
were all driven through the actual wire protocol.

- **Game 1** — broad profile (`Setups.broad`): all-Yes, all 28 equipment, Vers/Vers, Extremely
  filthy, three-orders finale. Completed with the full 10-regular/2-closing contract.
- **Game 2** — mixed profile: Yes/Maybe/No across preference sections, Vers Top/Vers Bottom, one
  player with erection difficulty, three shared boundaries (no marks, no degradation, no foot
  play), Filthy explicitness, Dominant-private finale. Confirmed the boundary system correctly
  blocked and silently replaced 5 proposals that would have violated a boundary, and that only
  the Dominant's phone saw the finale-order choice.
- **Game 3** — restrictive profile: no anal for either player (role and boundary both set), no
  toys, no pain, no rough sex, no degradation, no foot play, both players with erection
  difficulty, Erotic (mildest) explicitness, uninterrupted finale. Confirmed 24 boundary-blocked
  substitutions with zero violating content in the final contract, and that erection-difficulty
  exclusion is scoped to content actually requiring an erection rather than all orgasm content.

**Two real content bugs found and fixed** (neither could have been caught by the JVM test suite,
which never renders through a real browser, or by the earlier live-verification in section 3a,
which only ever drove the pure-JVM dev server's happy path — this was the first time the full
negotiation, consideration and closing-term content was actually read end to end):

1. **Grammatically broken sentences from seven `Lexicon` verb entries.** Several verb phrases
   (`v_finger`, `v_finger_deep`, `v_rim`, `v_rim_hard`, `v_deep`, `v_fuck_hard`, `v_pin`) were
   authored as complete prepositional phrases (e.g. `"works open with his fingers"`) or ended in
   a bare adjective (e.g. `"fucks hard"`), but every calling template inserts the object
   immediately after the token (`{G} [v_finger] {R}`) — the documented contract for the whole
   lexicon. Live output was visibly broken: *"Dan fucks open with two fingers him with lube"*,
   *"Marcus works open with his fingers him with lube"*. Fixed by rewriting each affected entry so
   every register ends bare or in a preposition that can legitimately take the following object
   (`"finger-fucks"`, `"works his tongue over"`, `"sinks slowly onto"`, `"fucks hard into"`,
   `"clamps"`, etc.), verified by rereading the actual rendered text in all three games afterward.
2. **A missing subject pronoun in four `base`-register templates** (`ClimaxTerms` ×2,
   `ConsiderationsNonSexual` ×1, `TermsLevel5` ×1) — each read `"... while [v_x] him ..."` where
   the sibling `explicit` line correctly had `"... while he [v_x] him ..."`. Only reachable at
   Erotic/Direct explicitness (`base` is used below Filthy), which is why Game 3's transcript is
   what caught it. Fixed by adding the missing `he`.

All 53 automated tests, including content validation, still pass after both fixes. One further
issue was found in the process but was in the manual test harness, not the app: the client's
"Back" fallback button is not enclosed in `.choices .btn` like every other button (it renders
directly under `#main`, see `controller.js` `render()`), so a `.choices`-scoped click helper
cannot find it — confirmed working correctly (present within 200ms of signing, every time) once
checked with an unscoped selector.

### 4b. On-device quick round on the Android emulator

Every earlier playthrough drove the pure-JVM dev server. This one ran against the **installed
release APK** on an Android TV emulator (API 35, 1920×1080 leanback profile, software-rendered —
the environment has no `/dev/kvm`), with the app's own foreground service hosting the embedded
NanoHTTPD server on port 8765, reached over `adb forward`. Two headless-Chromium tabs acted as the
two phones, loading the real `controller.html/.css/.js` served by the app itself. One complete
**quick** session (10 regular + 2 closing terms) was played setup → private profiles → negotiation
→ considerations → closing terms → finale execution → contract save, with `logcat` captured
continuously and every WebSocket frame logged on both phones.

Result: the round completed — 10/10 regular terms, 2/2 closing terms, 12 consideration receipts,
both phones reaching `Done` and the TV reaching `Complete` — with **zero** `FATAL EXCEPTION` or
`AndroidRuntime` entries in logcat across the whole run. Persistence was then verified from the
other side: after abandoning the session (which clears the active session but keeps saved
contracts), the TV's "Saved contracts on this TV" card listed **"Marcus and Dan — 12 terms"**,
proving the contract had been written to the Keystore-encrypted Room database rather than merely
displayed. That entry also survived a subsequent `adb install -r` upgrade of the APK.

**One real reliability bug found and fixed.** An earlier run left the TV showing "Dan: in
progress" indefinitely after Player 2's profile save appeared to succeed on the phone. WebSocket
frame logging showed the cause: this emulator's `adb forward` link drops and re-establishes the
socket every few seconds, and an action sent into that window was lost silently — `controller.js`
showed a "trying again" toast but never actually retried, even though the protocol was explicitly
designed for safe retries (`actionId` deduplication server-side, `expectedVersion` staleness
rejection) that the client simply never used. Fixed in two commits:

1. Actions whose `send()` fails because the socket is already closed are queued and replayed from
   `ws.onopen` once reconnected.
2. An action is then treated as pending from the moment it is *created* until a reply naming its
   `actionId` arrives — not merely until `send()` returns true. A send can succeed locally and
   still never reach the server if the connection dies before the reply comes back, so every
   reconnect replays whatever is still unacknowledged. This is safe for the same reason a
   duplicate tap always was: the server dedups by `actionId` and rejects a stale
   `expectedVersion`, so a replay is either a no-op or applies exactly once.

All 53 automated tests still pass, and the shipped `controller.js` inside the release APK remains
byte-identical to source with zero external URL references.

Two further issues surfaced during this work and were **not** app bugs, recorded here so they are
not re-investigated later. The auto-player harness had no case for the shared "Draft contract"
overlay — `draftReviewOpen` is global state either phone can enter, and the harness had no way
out of it, so a run could park there indefinitely; the harness now closes it. Separately, the
constant reconnect churn is a property of `adb forward` over a software-emulated network in this
container, not of the app: on a real LAN the phones hold a stable socket. It was, however, exactly
the condition that exposed the dropped-action bug, which is a genuine defect on any flaky Wi-Fi.

---

## 5. Requirement coverage

| Area | Status |
| --- | --- |
| Native Kotlin Android TV app, Compose, leanback launcher intent, touchscreen not required | Built; verified in the packaged APK |
| Embedded HTTP + WebSocket server (NanoHTTPD/NanoWSD), pinned versions | Written and tested over real sockets |
| Room persistence, Keystore encryption, no release logging | Built; not executed on a device |
| Offline QR generation (ZXing core, bundled) | Written and tested |
| Phone controllers as plain bundled HTML/CSS/JS | Written and tested (no CDN, no PWA) |
| Interface detection, selection, address change, port fallback | Written and tested |
| Exactly two slots, third device refused | Written and tested |
| Secure tokens, reconnection, TV-confirmed reclaim, rate limiting | Written and tested |
| Server-authoritative state, versioning, idempotency | Written and tested |
| Persistence across restart and reboot, paused timer restore | Tested on the JVM; the boot-receiver path is built but not executed |
| Saved games and contracts | Written and tested |
| All 24 game phases, negotiation, consideration, bundles, closing terms, finale | Written and tested |
| Escalation across five acts | Written and tested |
| Explicitness registers | Written and tested |
| Boundaries, equipment gating, erection filtering, Maybe conditions | Written and tested |
| Back navigation | Written and tested |
| Timers, synchronisation, Stop All | Written and tested |
| Global pause from either phone and from the remote | Written and tested |
| TV remote backup controls | Built; not driven with a real remote |
| Privacy: nothing private on TV or the other phone | Written and tested |
| Forbidden content excluded | Written and tested |
| Debug APK | **Built** — 8,066,424 bytes |
| Release APK | **Built and signed** — 1,265,143 bytes, APK Signature Scheme v2 |

---

## 6. Remaining limitations

1. **Nothing has been run on an Nvidia Shield**, an emulator, or any Android device. Both APKs
   build, sign and verify, and the packaged manifest and assets were inspected directly, but no
   line of Android runtime code has ever executed. Device-specific behaviour — Shield Ethernet
   interface naming, foreground-service restrictions on the installed OS version, Keystore
   behaviour, D-pad focus traversal in the real Compose runtime — is unverified. **Install the
   debug APK and play one short game before trusting it with an evening.**

2. **The first real install may still surface issues** that compilation cannot catch: a runtime
   permission prompt on API 33+ for `POST_NOTIFICATIONS`, a foreground-service type rejection on
   a specific OEM build, or focus landing somewhere awkward on the remote panel. The `:core`
   module underneath is fully tested; the risk is concentrated in the thin Android layer.

3. **R8 emits `An error occurred when parsing kotlin metadata` warnings** during the release
   build, because the bundled R8 predates Kotlin 2.2.21. The build succeeds, the release APK is
   correct, and the ProGuard rules keep serializers, NanoHTTPD and ZXing — but the release APK
   in particular is the one to smoke-test first, since R8 shrinking is where a missing keep rule
   would show up.

4. **Explicitness uses two authored registers plus a four-level lexicon**, not four
   independently hand-written variants of all 354 pieces of content. Sentence structure and
   command framing change between the measured and coarse registers; 73 lexicon entries then
   differentiate verbs, adverbs, commands, dominance phrasing and orgasm/ownership language
   across all four levels, and Extremely filthy appends an authored tail. Tests assert all four
   outputs differ and that Direct and Extremely filthy are never identical. It is a real
   rewriting system, but it is not 808 bespoke sentences.

5. **No mDNS.** The specification made it optional and required that the app not depend on it;
   an IPv4 URL is always available and is what the QR code carries.

6. **The remote can only stand in for a disconnected player during a proposal response.** It
   deliberately cannot make a private selection — a private counteroffer, consideration choice or
   finale pick — because doing so from the TV would expose it. If a phone drops during a private
   step the options are to wait for it to reconnect, or release and reassign the slot.

7. **Reboot restore is best-effort**, as Android allows. `BOOT_COMPLETED` starts the service only
   when an unfinished session exists; on OEM builds with aggressive background restrictions the
   user may have to open the app once.

8. **Levels 2–5 sit exactly on the 40-term floor**, and level 1 exactly on 42. There is no
   surplus, so removing a term would breach the specification and fail the build — which is the
   intent, but it means adding restrictions to the library needs new content alongside.

9. **Consideration escalation is act-based**, derived from signed regular terms. Within an act
   the intensity band is fixed, with a one-level bump for bundle trades. It does not model a
   finer-grained curve.

10. **The phone controller needs the TV reachable to render anything.** That follows from having
    no service worker and no offline cache, which the specification required.

---

## 7. Reproducing the verification

```bash
./gradlew :core:test          # 53 tests, all passing — no Android SDK needed
./gradlew validateContent     # the content validation suite

./gradlew :app:assembleDebug    # → app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleRelease  # → app/build/outputs/apk/release/app-release.apk
```

The `:core` commands need no Android SDK. The `:app` commands need Platform 35 and
Build-Tools 35.0.0, plus `keystore.properties` for a signed release — see BUILD.md.

To verify the artifacts:

```bash
$ANDROID_HOME/build-tools/35.0.0/apksigner verify --print-certs \
    app/build/outputs/apk/release/app-release.apk
$ANDROID_HOME/build-tools/35.0.0/aapt2 dump badging \
    app/build/outputs/apk/release/app-release.apk | grep leanback
unzip -l app/build/outputs/apk/release/app-release.apk | grep web/
```
