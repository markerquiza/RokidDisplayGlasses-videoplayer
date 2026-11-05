# Install GlassPlayer on Rokid AI Glasses using an iPhone

This guide is for **Rokid AI Glasses with the green display** and an iPhone. The APK installs on the glasses; Safari on the iPhone becomes the remote.

**GlassPlayer v0.1 is a debug prototype.** It has passed compilation and automated tests, but installation, playback and controls have not been tested on Rokid hardware. YouTube playback is experimental. Native YouTube Cast-button pairing and offline caching are not included.

## Before you start

- Have the official **Hi Rokid** iPhone app installed and your glasses paired normally. Use [Rokid’s official app download page](https://global.rokid.com/pages/app-download).
- Update Hi Rokid through the App Store. Keep both devices adequately charged.
- Use a trusted Wi-Fi network for the first playback test. Both devices must be able to communicate with each other.
- This procedure does not require rooting the glasses, replacing firmware or jailbreaking the iPhone.

Community developers document **Hi Rokid → Home → Toolbox → Glasses app management** as an APK installation route. The feature and exact labels can vary with the iPhone app and glasses firmware. These are conditional instructions, not a claim that every Hi Rokid version supports APK upload. See the [community developer installation reference](https://github.com/xnohat/rokid-glass-browser#readme).

## 1. Download the APK on your iPhone

1. Open [this successful build](https://github.com/markerquiza/RokidDisplayGlasses-videoplayer/actions/runs/41960626464) in Safari.
2. Sign in to GitHub if necessary.
3. Scroll to **Artifacts** and select **GlassPlayer-debug-apk**.
4. Save the downloaded ZIP to Files.
5. Open **Files → Browse → Downloads**, or the folder selected when saving.
6. Tap the ZIP to extract it.
7. Locate **app-debug.apk** in the extracted folder.

Download the APK from this repository’s build artifacts. The repository’s **Code → Download ZIP** button downloads source code, not the installable app.

Build artifacts expire according to GitHub retention settings. If this download has expired, open [Actions](https://github.com/markerquiza/RokidDisplayGlasses-videoplayer/actions), select a newer successful **Build Android APK** run, or use **Run workflow** to generate another build.

## 2. Transfer and install through Hi Rokid

1. Open **Hi Rokid** and confirm your glasses are connected.
2. Open **Home → Toolbox**.
3. Look for **Glasses app management**, **Install app on glasses**, or similar wording.
4. **If an Upload APK or local APK selection option is available**, tap it.
5. In the file picker, select the extracted **app-debug.apk**.
6. Follow the app’s transfer and installation prompts. Keep the devices nearby and powered on. If Hi Rokid requests joining the glasses’ Wi-Fi during the transfer, follow its instructions.
7. Confirm installation if prompted on the glasses.
8. Launch **GlassPlayer** through the app-management screen’s **Open** option if available, or through the glasses’ app menu.

**If no APK-upload option appears, stop at this point.** Record your Hi Rokid version and glasses firmware version, and share a screenshot of the Toolbox screen so the installation route can be checked. Do not assume an unrelated uploader or an iPhone configuration profile is required.

## 3. Connect your iPhone as the remote

1. After installation, connect the glasses and iPhone to the same trusted Wi-Fi network.
2. Open **GlassPlayer** on the glasses.
3. Read the address in **Pair your iPhone**. You can reopen it using **Pair iPhone**.
4. Enter the complete address in Safari, including port **8080** and everything after the **#**.
5. Keep GlassPlayer open on the glasses.
6. On the Safari remote, tap **Try sample video**.
7. Check picture, sound, play/pause, seek, volume and stop.
8. Once the sample works, paste a public YouTube video link and tap **Play on glasses**.

The glasses fetch media themselves. Closing Safari should not stop direct media playback while GlassPlayer stays open and the glasses remain online. If the glasses use your iPhone’s hotspot, turning off the iPhone can remove their internet connection. Begin with an ordinary Wi-Fi router.

The pairing code changes when the app restarts. Reopen the new complete address if the old remote no longer works.

## Troubleshooting

| Problem | What to check |
| --- | --- |
| Downloaded file is a ZIP | Extract it in Files and select the APK inside. |
| No APK-upload option in Hi Rokid | Capture the Toolbox screen and note app/firmware versions; the route is not confirmed for that version. |
| Safari cannot connect | Keep GlassPlayer open, check the displayed address and port, and ensure both devices are on the same Wi-Fi. Guest networks may block communication between devices. |
| Pairing or authorization fails | Open the latest complete pairing address, including its # code. |
| Sample works but YouTube does not | Try a different video that permits embedding. YouTube may require tapping its Play button on the glasses. |
| YouTube error 101 or 150 | The video does not permit embedded playback. |
| YouTube error 153 | Player identity/configuration failed; report it for investigation. |
| App crashes or glasses controls do not work | Stop the test and record the exact behavior, firmware version and app version. Hardware compatibility is still unverified. |
| A later APK reports a signature mismatch | Debug APKs produced by different build machines can use different signing keys. Do not disable signature verification. Use a build signed with the original key, or uninstall the old GlassPlayer through supported app management before installing the replacement; uninstalling removes its app data. |

## Safety and limitations

- The Safari remote uses **unencrypted local HTTP**. Use trusted Wi-Fi, keep the pairing address private, and do not expose port 8080 to the internet.
- Test while seated. Close the app if the glasses become uncomfortable or unusually warm.
- Installation success does not establish playback or touchpad compatibility.
- If removing the prototype, use Hi Rokid’s supported uninstall option if available, or the glasses’ app settings. Do not factory-reset the glasses merely to remove this app.

## References

- [Official Hi Rokid download page](https://global.rokid.com/pages/app-download)
- [Rokid Help Center](https://global.rokid.com/pages/faq)
- [Community developer reference for installation through Hi Rokid Toolbox](https://github.com/xnohat/rokid-glass-browser#readme)
- [Project README and current limitations](../README.md)

Last updated: November 5, 2026.
