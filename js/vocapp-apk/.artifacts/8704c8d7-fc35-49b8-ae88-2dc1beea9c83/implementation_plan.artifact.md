# Implementation Plan - Puzzle Tab

Add a "Puzzle" tab to the application featuring a matching pairs game as seen in the design.

## User Review Required

> [!IMPORTANT]
> The implementation assumes a dark theme consistent with the provided screenshot. I will define custom colors to match the design.

## Proposed Changes

### Dependencies

#### [MODIFY] [libs.versions.toml](file:///Users/freeelectron/labs/projects/vocapp/js/vocapp-apk/gradle/libs.versions.toml)
- Add `androidx-navigation-compose` to the `libraries` section.
- Add `navigationCompose = "2.7.7"` (or latest stable) to the `versions` section.

#### [MODIFY] [build.gradle.kts](file:///Users/freeelectron/labs/projects/vocapp/js/vocapp-apk/app/build.gradle.kts)
- Add the navigation dependency to the `dependencies` block.

---

### UI and Navigation

#### [MODIFY] [Color.kt](file:///Users/freeelectron/labs/projects/vocapp/js/vocapp-apk/app/src/main/java/com/example/vocapp_apk/ui/theme/Color.kt)
- Define colors for the puzzle screen: `PuzzleBackground`, `PuzzleCardBorder`, `PuzzleProgressPrimary`, etc.

#### [NEW] [PuzzleScreen.kt](file:///Users/freeelectron/labs/projects/vocapp/js/vocapp-apk/app/src/main/java/com/example/vocapp_apk/ui/PuzzleScreen.kt)
- Implement the `PuzzleScreen` Composable.
- Top bar with Close icon, Progress bar, and Level indicator.
- Grid of 10 word cards (matching pairs).
- Game logic: handling selection, matching pairs, and state updates.

#### [MODIFY] [MainActivity.kt](file:///Users/freeelectron/labs/projects/vocapp/js/vocapp-apk/app/src/main/java/com/example/vocapp_apk/MainActivity.kt)
- Set up `NavController` and `NavHost`.
- Add a `Scaffold` with a `NavigationBar` to switch between "Home" (placeholder) and "Puzzle".

## Verification Plan

### Manual Verification
- Deploy the app to a device/emulator.
- Navigate to the "Puzzle" tab.
- Verify the UI matches the provided screenshot.
- Play the matching game to ensure the logic works correctly (selecting pairs, feedback for matches).
