# GlassPlayer — Rokid prototype

Android receiver designed for Rokid AI Glasses with a monochrome display, with a Safari remote for iPhone. Version 0.1 is source code, not a hardware-verified release.

## Install using an iPhone

Follow the [iPhone installation and first-use guide](docs/INSTALL_IPHONE.md) to download the APK, check Hi Rokid’s upload option, install on the glasses, and connect the Safari remote. Includes troubleshooting and prototype limitations.

## Included
- Fullscreen Media3 player for HTTP(S) media, HLS and DASH (subject to device codecs).
- Experimental YouTube playback using the official IFrame API and the installed Android WebView.
- Local Wi-Fi receiver on port 8080; iPhone sends a URL and control commands through Safari.
- App-generated pairing token, POST-only controls, request-size limits; no account or cloud backend.
- Tap and horizontal swipe for direct media; D-pad / gamepad A toggles, left/right seeks 10 seconds, up/down adjusts volume. YouTube retains its own touch controls, with remote and gamepad commands.
- Player fetches media on the glasses, independently of the iPhone after the command is sent. The app must stay open and glasses must retain internet access.

## Not yet implemented
- Discovery in the official iPhone YouTube Cast menu, DIAL/YouTube Lounge pairing or TV codes.
- Offline downloads/cache, DRM playback, background service or guaranteed screen-off playback.
- Verified Rokid touchpad event mapping, device-specific display sizing and installation.

This is a first development milestone toward the screenshot, not feature parity. An iPhone personal hotspot may affect reachability and independence; begin testing with both devices on a normal Wi-Fi router without client isolation.

## Build
Open this directory in Android Studio, install Android SDK 35 and Build Tools 35.0.0, and select JDK 17 and Gradle 8.11.1. The Gradle wrapper is bundled.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

The GitHub Actions workflow builds, tests and uploads a debug APK on push or manual dispatch. Production signing is not configured.

## First device test (when ready)
1. Install the debug APK on an Android test device or the glasses using an authorized installation method.
2. Open GlassPlayer. It displays local pairing addresses. On iPhone, join the same Wi-Fi and enter one complete address in Safari, including its `#` token.
3. Choose **Try sample video**. Confirm picture/audio, pause, seek, volume and stop.
4. Close Safari or turn the iPhone off while keeping the glasses online; direct media should continue.
5. Send a public YouTube video link that allows embedding. Check startup, errors, controls and playback after phone disconnects. YouTube may require tapping its Play button. Errors 101/150 indicate embedding restrictions; 153 indicates player identity/configuration failure. WebView, Google services and firmware compatibility require device testing.
6. Test the touchpad and gamepad. Record firmware version, Android version, resolution and actual key events before adding Rokid-specific mappings.
7. Reopen the app and confirm the old token no longer controls it. Remote control is plain HTTP on the local network; use trusted Wi-Fi. Token pairing prevents unauthenticated commands but does not encrypt traffic.

## Architecture
`MainActivity` owns playback and marshals remote requests to the UI thread. `RemoteServer` validates/authenticates commands. `VideoSource` validates URLs and extracts YouTube IDs. Bundled `remote.html` is served to the iPhone. `youtube.html` embeds the official YouTube player with app identity as its base URL. No media extraction or YouTube download service is used.

## Validation status
Built locally on November 5, 2026 with a temporary ARM JDK 17, Gradle 8.11.1 and Android SDK 35. The debug APK compiles successfully. Seven JUnit tests pass (URL validation and live HTTP receiver command/authentication tests). Both HTML assets pass JavaScript syntax checks. Android lint completes with no errors; prototype warnings include English-only UI strings, fixed landscape orientation, enabled JavaScript for YouTube, backup rules and newer available Media3 versions. Playback, iPhone Safari behavior and Rokid hardware input have not been tested on a device.

Temporary tools live under `/tmp/rokid-build`; future builds should use an installed Android Studio toolchain. The download/build did not change system Java or install apps on the glasses.

References: https://developer.android.com/media/media3/exoplayer/hello-world ; https://developers.google.com/youtube/iframe_api_reference ; https://developers.google.com/cast/docs/android_tv_receiver/
