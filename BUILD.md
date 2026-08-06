# Building and sideloading The Contract

## Requirements

| | Version |
| --- | --- |
| JDK | 17 or newer (the build targets Java 17 bytecode) |
| Android SDK | Platform 35, Build-Tools 35.0.0, Platform-Tools |
| Gradle | Supplied by the wrapper (8.14.3) — do not install it separately |
| Network | Maven Central **and** Google Maven (`dl.google.com`) must be reachable |

Every dependency version is pinned in `gradle/libs.versions.toml`. Nothing resolves dynamically,
so two machines produce the same dependency graph.

> **One version pair must stay aligned:** KSP is released per Kotlin version, so the `ksp`
> entry in the catalogue (`2.2.21-2.0.4`) must match the `kotlin` entry (`2.2.21`). If Gradle
> reports that the KSP version cannot be found, look up the current KSP release for that exact
> Kotlin version and update the `ksp` entry only.

---

## 1. Verify the environment

```bash
java -version          # 17+
echo "$ANDROID_HOME"   # or set sdk.dir in local.properties
```

`:app` is only added to the Gradle build when an SDK is found. Point the build at one either by
exporting `ANDROID_HOME` / `ANDROID_SDK_ROOT`, or by creating `local.properties`:

```properties
sdk.dir=/path/to/Android/sdk
```

Confirm it worked — the configuration banner about a missing SDK should disappear:

```bash
./gradlew projects
```

If you only want to run the logic and content tests, skip this step entirely; `:core` needs no
Android SDK.

---

## 2. Install the SDK pieces

Using the command-line tools:

```bash
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
```

Or in Android Studio: **SDK Manager → SDK Platforms → Android 15 (API 35)** and
**SDK Tools → Android SDK Build-Tools 35, Android SDK Platform-Tools**.

---

## 3. Run the test suite first

```bash
./gradlew :core:test
```

This runs content validation, the three specified end-to-end multiplayer games, the protocol and
network tests (which stand up the real HTTP/WebSocket server on real sockets) and the
persistence and restart tests. It takes well under a minute and needs no device.

An HTML report is written to `core/build/reports/tests/test/index.html`.

---

## 4. Build the debug APK

```bash
./gradlew :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`
(application id `com.thecontract.tv.debug`, so it can sit alongside a release build).

---

## 5. Build the signed release APK

1. Generate a keystore once:

   ```bash
   keytool -genkeypair -v \
     -keystore the-contract-release.jks \
     -alias the-contract \
     -keyalg RSA -keysize 4096 -validity 10000
   ```

   Answer the prompts and choose strong passwords. **Back this file up.** Without it you cannot
   ship an update that the TV will accept as the same app.

2. Create `keystore.properties` in the project root (git-ignored):

   ```properties
   storeFile=the-contract-release.jks
   storePassword=…
   keyAlias=the-contract
   keyPassword=…
   ```

   `storeFile` is resolved relative to the project root.

3. Build:

   ```bash
   ./gradlew clean :app:assembleRelease
   ```

   Output: `app/build/outputs/apk/release/app-release.apk`

4. Verify the signature:

   ```bash
   "$ANDROID_HOME/build-tools/35.0.0/apksigner" verify --print-certs \
       app/build/outputs/apk/release/app-release.apk
   ```

If `keystore.properties` is missing the release variant still builds, but the output is
`app-release-unsigned.apk` and cannot be installed until you sign it.

---

## 6. Sideloading onto an Nvidia Shield TV Pro

### Enable developer mode on the Shield

1. **Settings → Device Preferences → About**
2. Click **Build** seven times until it says you are a developer.
3. **Settings → Device Preferences → Developer options** → enable **USB debugging**, and
   **Network debugging** if you want to install over Wi-Fi.

### Install over the network (easiest)

The Shield's IP address is under **Settings → Device Preferences → About → Status**.

```bash
adb connect 192.168.1.x:5555          # accept the prompt on the TV
adb install -r app/build/outputs/apk/release/app-release.apk
```

### Install over USB

```bash
adb devices                            # accept the prompt on the TV
adb install -r app/build/outputs/apk/release/app-release.apk
```

### Reinstalling over a different signature

If you previously installed a build signed with a different key, uninstall first:

```bash
adb uninstall com.thecontract.tv
adb install app/build/outputs/apk/release/app-release.apk
```

### Launching

The app appears in the Shield's **Apps** row with its banner. It can also be started directly:

```bash
adb shell am start -n com.thecontract.tv/.ui.MainActivity
```

---

## 7. First run

1. Put the TV and both phones on the same network. Ethernet on the Shield is preferred and is
   selected automatically when present.
2. The TV shows a QR code, the join URL, and which interface it is advertising. If it picked the
   wrong one, open **Network and pairing** in the remote panel and choose another.
3. Scan with the first phone's camera and open the link — that phone becomes Player 1 and runs
   the shared setup.
4. Scan with the second phone — it becomes Player 2 and waits until setup is finished.
5. Once both phones are connected the QR code disappears from the TV.

A third device that scans the code is told the session is full and receives no game state.

---

## 8. Troubleshooting

**The phone cannot open the link.** Client isolation ("AP isolation" / "guest network") on the
router blocks device-to-device traffic. Move both phones and the TV onto the same normal SSID.

**The TV shows the wrong address.** Some Shields report both Ethernet and Wi-Fi. Pick the
interface you want in the remote panel; the QR code regenerates immediately.

**The port is different from 8765.** Something else on the TV was already using it. The app
falls back through 8766–8769 and always advertises the port it actually bound.

**A phone lost its slot.** Reconnect from the same phone first — its stored token restores its
own slot. If the phone is gone for good, use **Release slot** in the remote panel, then have the
replacement scan the code.

**The app was killed while a timer was running.** The timer comes back paused at its last saved
value. That is intentional; restart it when you are ready.

**A build fails resolving `com.android.application`.** Google Maven (`dl.google.com`) is not
reachable from your network. The Android Gradle Plugin, the Android SDK and every AndroidX
artifact are served from that host and have no mirror on Maven Central.
