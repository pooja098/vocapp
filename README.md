# vocapp

An Android app for practicing German vocabulary through two timed puzzles: **Match** and **Write**. Built with Kotlin and Jetpack Compose.

## Run on an Android phone

### Requirements

- An Android phone running Android 7.0 (API 24) or newer.
- Android Studio with support for the project's Android Gradle plugin and Gradle wrapper.
- Android SDK Platform 37, as configured in `app/build.gradle.kts`, and SDK Platform-Tools. Install missing components through Android Studio's SDK Manager.
- A USB cable that supports data transfer.

### Open and install

1. Open the **repository root (`vocapp`)** in Android Studio—the directory containing `settings.gradle.kts` and `gradlew`. The project no longer lives under `js/vocapp-apk`.
2. Let Gradle sync finish and install any requested SDK components. Ensure `local.properties` points to your own Android SDK location; a path from another computer will not work. Use a compatible Gradle JDK in Android Studio's Gradle settings.
3. On your phone, enable **Developer options** by tapping **Build number** seven times in Settings (its location varies by manufacturer). Enable **USB debugging** in Developer options.
4. Connect and unlock the phone, then accept its USB debugging authorization prompt.
5. Select the **app** run configuration and your phone in Android Studio, then click **Run ▶**. Android Studio builds, installs, and launches the app.
6. After installation, open **vocapp** from your phone's launcher to play without Android Studio connected.

For device connection troubleshooting, including platform-specific drivers, see the official [Android device setup guide](https://developer.android.com/codelabs/basic-android-kotlin-compose-connect-device).

### Optional: install from the terminal

With the Android SDK configured, a compatible JDK available through `JAVA_HOME`, and the phone connected with USB debugging authorized, run from the repository root:

```sh
./gradlew :app:installDebug
```

On Windows, use `gradlew.bat :app:installDebug`. Open **vocapp** on the phone after installation.

To build an APK without installing it:

```sh
./gradlew :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

If Gradle reports **Unable to locate a Java Runtime**, configure a compatible JDK for the terminal or run the project using Android Studio's configured Gradle JDK. If the phone is missing from the device list, check the cable, USB debugging, and the authorization prompt.

## Puzzle settings

Open **Settings** before starting a run. **Write** and **Match** have independent settings:

| Setting | Meaning | Default |
| --- | --- | --- |
| words | Total word pairs to solve in a run | 10 |
| timer (seconds) | Time allowed for the whole run | 5 |
| page-size | Number of rows shown in each column, capped by the target word count | 7 |

Use **+** and **−** to adjust values. Changes save when you leave Settings, survive app restarts, and apply to new runs. Increase the timer for more relaxed practice, especially while typing.

Both puzzles start the countdown immediately. The top progress bar shows time remaining. Solve all configured pairs **before** the timer expires to win; otherwise you lose. A result popup appears for one second before returning to Home. Time continues while the app is in the background.

## Play Match

1. Tap **Match** on Home.
2. Tap a German word in the left column and its English meaning in the right column, in either order. English cards are shuffled, so the matching word may be on a different row.
3. Selected cards have an emphasized border. A correct pair counts toward your target and is replaced with a new pair while there are more pairs to load.
4. An incorrect match briefly holds the selection, then clears it so you can try again.
5. Once the configured total has been loaded, correct matches leave empty spaces. Clear the remaining pairs before time runs out.

Pairs may repeat from the active word bank during a run.

## Play Write

1. Tap **Write** on Home.
2. Each row shows an English meaning on the right and an empty German input on the left. Tap the German cell to type its translation.
3. Submit by tapping another input cell, tapping an English card, or pressing the keyboard's **Done** action. Typing alone does not submit the answer.
4. A correct answer counts toward your target. The pair is replaced immediately while more pairs remain to be loaded; the replacement German input starts empty.
5. An incorrect answer stays visible with a soft red border. Edit it and submit again. Empty answers are ignored.
6. Once the configured total has been loaded, solved pairs disappear. Finish all remaining pairs before the deadline, including submitting your final answer.

Answers ignore capitalization and spaces at the beginning or end, but otherwise must match the stored German text. Include articles and special characters when present: for example, `das Haus` for `house`, rather than just `Haus`.

## Top performances

Home shows separate **Match** and **Write** tables, each containing up to five wins. Results are ranked by **words matched ÷ time taken in seconds**, highest first. The tables show Words, Time, and Words/s, and persist across app restarts. Losses are not recorded in these tables.

## Manage vocabulary

- In **Words**, enter both German and English text in the final empty row and tap **+** to add a pair.
- Long press a row to start selecting. Tap other rows to select them, or tap selected rows to deselect them.
- Tap **Archive** to move selected pairs out of the active bank.
- Tap **View archive** to open archived words. Select rows to **Unarchive** them or **Delete** them permanently. The back arrow returns to Words.
- New puzzles use the active bank. If it is empty, add or unarchive a pair to enable Match and Write.

The bundled files `app/src/main/assets/words.json` and `app/src/main/assets/archive.json` seed the word lists. Both use a two-column JSON array with no header row:

```json
[
  ["das Haus", "house"],
  ["die Katze", "cat"]
]
```

Android assets are read-only at runtime. Edits are saved to app-private `words.json` and `archive.json` files and survive restarts; they do not rewrite the repository assets. Editing the bundled assets does not replace an already initialized word bank on a phone. Clearing app storage resets local data, including vocabulary, settings, and performance history.
