# Implementation report

Generated from the repository at the tip of `claude/contract-android-tv-app-1ogltr`.

---

## 1. Headline: the APKs were not produced, and why

**No debug APK and no release APK exist in this repository.** They could not be built in the
environment this work was done in, and I am not going to claim otherwise.

The blocker is a single host. `dl.google.com` is refused by this environment's egress policy:

```
$ curl -sS -o /dev/null -w "%{http_code}" https://dl.google.com/android/repository/repository2-3.xml
curl: (56) CONNECT tunnel failed, response 403

$ curl -sS "$HTTPS_PROXY/__agentproxy/status"
  "recentRelayFailures": [
    { "kind": "connect_rejected",
      "detail": "gateway answered 403 to CONNECT (policy denial or upstream failure)",
      "host": "dl.google.com:443" } ]
```

That one host serves **all three** things an Android build needs:

1. the Android SDK (platforms, build-tools, platform-tools),
2. the Android Gradle Plugin, and
3. every AndroidX artifact — Compose, Room, Lifecycle, Core-KTX.

`maven.google.com` is reachable but 301-redirects straight to `dl.google.com`. Maven Central
carries the Android Gradle Plugin only up to **2.3.0 (2017)**, which cannot build a Kotlin 2.x
Compose project. Ubuntu's `universe` repository offers `android-sdk-platform-23` and
`android-sdk-build-tools 29`, but API 23 predates `startForegroundService`, notification
channels and the whole AndroidX line — building against it would have produced something that
does not match the specification and would not run correctly on a Shield.

The exact failure, reproduced against the finished project:

```
$ ./gradlew :app:assembleDebug
* What went wrong:
Plugin [id: 'com.android.application', version: '8.7.3'] was not found in any of the following sources:
  … could not resolve plugin artifact 'com.android.application:com.android.application.gradle.plugin:8.7.3'
  Searched in the following repositories: Google, MavenRepo, Gradle Central Plugin Repository
```

On any machine with the Android SDK and normal access to Google Maven, `./gradlew
:app:assembleDebug` and `./gradlew :app:assembleRelease` are the only commands needed;
`BUILD.md` covers both, plus signing and sideloading to a Shield.

### What I did about it

I put as much of the system as possible into a **pure-JVM `:core` module** that has no Android
dependency: the content library, the style engine, the rules engine, the server-authoritative
state machine, the wire protocol, the timer engine and the embedded HTTP/WebSocket server. That
module builds and its full test suite runs here — including three complete end-to-end games with
two simulated phone clients, and network tests that stand up the real server on real sockets.

So the parts that were verifiable were verified properly. The parts that were not are named
plainly in section 6.

---

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
| Tested on device | **None.** No APK was produced, so nothing was installed or run on any Android version, emulator or Shield. |
| Tested on JVM | JDK 21 (Ubuntu), producing Java 17 bytecode — the full `:core` suite |

Runtime-behaviour claims about Android specifically (foreground service, Keystore, Room, boot
receiver, Compose focus handling) are **written, reviewed and conventional, but unexecuted**.

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

---

## 5. Requirement coverage

| Area | Status |
| --- | --- |
| Native Kotlin Android TV app, Compose, leanback launcher intent, touchscreen not required | Written; not compiled |
| Embedded HTTP + WebSocket server (NanoHTTPD/NanoWSD), pinned versions | Written and tested over real sockets |
| Room persistence, Keystore encryption, no release logging | Written; not compiled |
| Offline QR generation (ZXing core, bundled) | Written and tested |
| Phone controllers as plain bundled HTML/CSS/JS | Written and tested (no CDN, no PWA) |
| Interface detection, selection, address change, port fallback | Written and tested |
| Exactly two slots, third device refused | Written and tested |
| Secure tokens, reconnection, TV-confirmed reclaim, rate limiting | Written and tested |
| Server-authoritative state, versioning, idempotency | Written and tested |
| Persistence across restart and reboot, paused timer restore | Written and tested (reboot path via the boot receiver is written, not executed) |
| Saved games and contracts | Written and tested |
| All 24 game phases, negotiation, consideration, bundles, closing terms, finale | Written and tested |
| Escalation across five acts | Written and tested |
| Explicitness registers | Written and tested |
| Boundaries, equipment gating, erection filtering, Maybe conditions | Written and tested |
| Back navigation | Written and tested |
| Timers, synchronisation, Stop All | Written and tested |
| Global pause from either phone and from the remote | Written and tested |
| TV remote backup controls | Written; the UI itself is not compiled |
| Privacy: nothing private on TV or the other phone | Written and tested |
| Forbidden content excluded | Written and tested |
| Debug APK | **Not produced** |
| Release APK | **Not produced** |

---

## 6. Remaining limitations

1. **No APKs.** See section 1. Everything needed to produce them is in the repository; the
   environment could not reach `dl.google.com`.

2. **The `:app` module has never been compiled.** Roughly 1,500 lines of Android code — the
   foreground service, Room database, Keystore crypto, network monitor, boot receiver, Compose
   surface and remote panel — are written to conventional, current APIs and reviewed, but not
   compiled, not linted and not run. Expect to fix small things (an import, a Compose signature,
   a Room annotation) on the first real build. The `:core` module it sits on is fully tested.

3. **Nothing has been run on an Nvidia Shield**, an emulator, or any Android version.
   Device-specific behaviour — Shield Ethernet naming, foreground-service restrictions on the
   installed OS version, D-pad focus traversal in the actual Compose runtime — is unverified.

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
./gradlew :core:test          # 53 tests, all passing
./gradlew validateContent     # the content validation suite
```

Neither needs an Android SDK or a network connection beyond the initial dependency download from
Maven Central.
