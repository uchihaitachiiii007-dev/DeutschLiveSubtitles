# Deutsch Live Subtitles (prototype)

This is a starter Android project for German-to-English live subtitles.

## Important prototype limitations
- Uses Android SpeechRecognizer microphone input (not direct internal audio capture).
- Recognition may pause after a phrase; this prototype attempts to restart after final results.
- Speech recognition depends on an installed Android speech recognition provider and language support.
- Overlay permission must be granted in Android settings.
- Translation uses ML Kit on-device German-English model, which downloads once.
- This prototype has not been compiled or device-tested here.

## Build using GitHub Actions (phone-only route)
1. Create a GitHub account and a new repository.
2. Upload the project files from this ZIP to the repository (keep the folders).
3. Open the Actions tab, select "Build APK", and run workflow (or push to main).
4. Download the workflow artifact named `DeutschLiveSubtitles-debug`.
5. Extract the APK and install it. Android may ask to allow installing from that browser/file manager.
6. Open app; grant microphone permission. Tap Enable floating subtitles and grant overlay permission, then return and tap it again. Tap Start listening.

For the first attempt, play the series on another device or use the phone speaker at a reasonable volume so the mic can hear it. Direct capture of audio from the same phone is a planned improvement.
