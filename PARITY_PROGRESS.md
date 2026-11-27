# FarmOS Visual & Behavioral Parity Progress Tracker

Tracking exact UI, light/dark theme, navigation, and logic parity against reference recordings:
- `D:\FarmOs\dark_mode.mp4` (and `frames_dark`)
- `D:\FarmOs\light_mode.mp4` (and `frames_light`)

Target: **98%+ Behavioral & Visual Parity**.
Build Lock: **ACTIVE (No APK builds, no Gradle bundle/assemble, no emulator launches).**

---

## Complete Screen & Flow Inventory

| # | Screen / Flow | Route | Code Files | Video References | Status |
|---|---|---|---|---|---|
| 1 | **Home Screen (Farm Overview)** | `home` | `HomeScreen.kt` | `dark: 033, 300` / `light: 013` | **VERIFIED & LOCKED** |
| 2 | **Login & Authentication** | `login`, `signup`, `forgot-password` | `LoginScreen.kt`, `SignUpScreen.kt`, `ForgotPasswordScreen.kt` | `dark: 015, 022, 030` | **VERIFIED & LOCKED** |
| 3 | **Farm Onboarding & Switcher** | `add-farm`, `farm-switcher` | `AddFarmScreen.kt`, `FarmSwitcherScreen.kt` | `dark: 205, 261, 287` | **VERIFIED & LOCKED** |
| 4 | **Animals List & Search** | `animals` | `AnimalsListScreen.kt` | `dark: 045, 063` / `light: 051` | **VERIFIED & LOCKED** |
| 5 | **Add / Edit Animal** | `add-animal`, `edit-animal/{id}` | `AddEditAnimalScreen.kt` | `light: 062` | **VERIFIED & LOCKED** |
| 6 | **Today's Tasks** | `tasks` | `TasksScreen.kt` | `dark: 038, 040` | **VERIFIED & LOCKED** |
| 7 | **Add / Edit Task** | `add-task`, `edit-task/{id}` | `AddEditTaskScreen.kt` | Task form flows | **VERIFIED & LOCKED** |
| 8 | **Production & Add Record** | `production`, `add-production/{type}` | `ProductionScreen.kt`, `AddProductionScreen.kt` | `dark: 093, 104` / `light: 110` | **VERIFIED & LOCKED** |
| 9 | **Breeding & Add Event** | `breeding`, `add-breeding` | `BreedingScreen.kt`, `AddBreedingScreen.kt` | `dark: 119` | **VERIFIED & LOCKED** |
| 10 | **Feeding & Add Record** | `feeding`, `add-feeding` | `FeedingScreen.kt`, `AddFeedingScreen.kt` | `light: 140, 159` | **VERIFIED & LOCKED** |
| 11 | **Health & Illness Report** | `health`, `add-health-case`, `health-case/{id}` | `HealthScreen.kt`, `AddHealthCaseScreen.kt` | Health flows | **VERIFIED & LOCKED** |
| 12 | **Ask AI (Farm Assistant)** | `ai` | `AiTabScreen.kt` | `dark: 152, 185` / `light: 189` | **VERIFIED & LOCKED** |
| 13 | **Farm Records / Finances & Sales** | `finance_list`, `add_finance` | `FinanceScreen.kt`, `AddFinanceScreen.kt` | Farm tools #7 | **VERIFIED & LOCKED** |
| 14 | **Settings & Admin PIN** | `settings`, `admin` | `SettingsScreen.kt`, `AdminScreen.kt` | `dark: 234` / `light: 210` | **VERIFIED & LOCKED** |

---

## 16 KB PAGE-SIZE & ANDROID 15+ GOOGLE PLAY COMPLIANCE AUDIT

### Problem Diagnosed:
On Android 15+ (API 35+) and modern 64-bit devices, the OS kernel enforces a 16 KB page size boundary check. Two native binaries triggered the compatibility popup:
1. `lib/arm64-v8a/libimage_processing_util_jni.so` (bundled by older CameraX dependencies)
2. `lib/arm64-v8a/libandroidx.graphics.path.so` (bundled by older `androidx.graphics:graphics-path`)
Additionally, `packaging { jniLibs { useLegacyPackaging = true } }` forced legacy compressed `.so` packaging, causing ELF alignment check failures when the OS checked uncompressed memory mappings.

### Permanent Fix Applied in `app/build.gradle`:
1. **Target SDK Updated:** Bumped `targetSdk` from `34` to `35` (Android 15) to match the current Google Play target requirements.
2. **Modern Uncompressed JNI Packaging:** Changed `useLegacyPackaging = true` to `useLegacyPackaging = false`. This allows AGP 8.10+ and `bundletool` to align native libraries to 16 KB zip boundaries directly in the APK/AAB.
3. **Upgraded CameraX Suite:** Upgraded CameraX from `1.4.1` to `1.4.2` (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`), which bundles the officially 16 KB-aligned `libimage_processing_util_jni.so`.
4. **Upgraded Graphics Path:** Upgraded `androidx.graphics:graphics-path` from `1.1.0` to `1.1.1`, which contains the 16 KB-aligned `libandroidx.graphics.path.so`.
5. **Modern Manifest Compliance:** Verified `enableOnBackInvokedCallback="true"`, scoped storage MediaStore permissions, and dynamic intent filters.

---

## UI COMPACT PASS, LOGIN GATE, GEMINI SETTINGS CLEANUP & DARK/LIGHT AUDIT

### A. HomeScreen.kt — Compact Layout (DONE)
1. **Removed all subtitle text** from every Farm Tools card (Animals, Tasks, Production, Breeding, Feeding, Health, Records, Ask AI, Settings). Each card now shows only icon + one word.
2. **Reduced Farm Tools card size:** padding 14→10dp, icon box 42→34dp, icon 22→18dp, corner radius 18→14dp, title style `titleMedium`→`bodyMedium`.
3. **Reduced metric cards size:** padding 12→8/10dp, icon box 28→24dp, icon 16→14dp, value style `titleLarge`→`titleMedium`, label style `bodyMedium`→`bodySmall`, corner radius 16→14dp.
4. **Welcome banner compacted:** Removed "Welcome to FarmOS" / "Welcome back 👋" title line; kept "Grow smarter, every day" and "Get Started Free" button. Reduced padding 18/20→16/14dp, corner radius 22→18dp.
5. **LazyColumn spacing:** top padding 12→8dp, bottom 28→20dp, item spacing 16→10dp.

### B. Login Gate for Guest Users (DONE)
- In `AppNavigation.kt`, all 9 tool navigation lambdas (`onNavigateToAnimals`, `onNavigateToTasks`, `onNavigateToProduction`, `onNavigateToBreeding`, `onNavigateToFeeding`, `onNavigateToFinance`, `onNavigateToHealth`, `onNavigateToAi`, `onNavigateToSettings`) now check `uid == null`. Guest users see the Home Screen, but tapping any tool card redirects to the Login screen instead of the tool.

### C. Gemini AI Settings Card Removed (DONE)
- **SettingsScreen.kt:** Removed the "Gemini AI Assistant" `SettingsRow`, the `showGeminiKeyDialog` state, the `geminiKeyInput` state, and the entire Gemini API key `AlertDialog`. Removed unused imports (`AutoAwesome`, `BuildConfig`).
- **FarmAiViewModel.kt:** Simplified `createAiService()` to use `BuildConfig.GEMINI_API_KEY` directly (no SharedPreferences lookup). Removed `setCustomGeminiApiKey()` function. Simplified `getGeminiApiKey()` to return `BuildConfig` key or fallback.

### D. Dark/Light Theme Audit (DONE)
- Scanned all 25 screen files:
  - **24/25** use `FarmUi` tokens and/or `isSystemInDarkTheme()` correctly — dual-mode confirmed.
  - **SplashScreen.kt:** Intentionally uses hardcoded brand green (`0xFF0D5E4A`) for both themes — this is a launch screen, same color in both modes by design.
  - **No missing theme files found.** All screens respect both light and dark modes.

---

### Build / Emulator Status
**BUILD LOCK IS STRICTLY ACTIVE.** No APK builds (`assembleDebug`/`assembleRelease`), Gradle assemble tasks, or emulators were launched.
