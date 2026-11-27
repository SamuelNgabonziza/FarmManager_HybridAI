# FarmOS Hybrid AI

**Smart Livestock Management Powered by Firebase AI Logic (On-Device + Cloud)**  
Built for **#BuildHybridAI 2026** (Android).

---

## Value Proposition

Smallholder and commercial livestock farmers frequently operate in rural areas using affordable Android smartphones with unreliable internet connectivity. **FarmOS Hybrid AI** is a complete Android livestock operations platform paired with a hybrid AI advisor powered by **Firebase AI Logic**:

- **On-Device First (`InferenceMode.PREFER_ON_DEVICE`):** On AICore / Gemini Nano–supported Android devices, farm queries run locally on-device with zero mobile data or cloud token cost.
- **Seamless Cloud Fallback (`gemini-3.5-flash-lite` / `gemini-3.8-flash`):** On entry-level and mid-range phones without Gemini Nano hardware (such as the Samsung Galaxy A16), FarmOS automatically detects unavailability and routes requests to Cloud Gemini via Firebase AI Logic—never leaving farmers behind.
- **Grounded in Real Farm Records:** Unlike a generic chatbot, FarmOS injects live Firestore herd, milk yield, health, feeding, breeding, and finance records into the advisor context (`FarmAiContextRepository`) and lets farmers turn AI recommendations into scheduled farm tasks with one tap.

---

## Firebase AI Logic Hybrid Architecture

- **Initialization & App Check (`FarmOsApplication.kt`):**
  - Calls `Firebase.initialize(context = this)` and `Firebase.appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())` before any Firebase calls.
- **Runtime Hybrid Inference (`HybridFarmAdvisorService.kt`):**
  - Uses `Firebase.ai(backend = GenerativeBackend.googleAI())` with `OnDeviceConfig(mode = InferenceMode.PREFER_ON_DEVICE)` and `gemini-3.5-flash-lite` (plus `gemini-3.8-flash` secondary fallback).
  - Reads `InferenceSource.ON_DEVICE` vs. `InferenceSource.IN_CLOUD` directly from `GenerateContentResponse` chunks and surfaces the active route (`ON-DEVICE`, `IN CLOUD`, `CLOUD FALLBACK`, or `OFFLINE LOCAL`) in `HybridInferenceStatusBar` inside `AiTabScreen.kt`.
  - Uses an 8-second initial timeout + per-session unavailability cache so unsupported devices fall back to Cloud Gemini in 1–2 seconds, and handles offline/quota/network failures gracefully (`"Hey you went offline, retry when you get connected!"`).
- **Supporting Firebase Products:**
  - **Firebase Authentication** (Email/Password + Google Sign-In via Credential Manager)
  - **Cloud Firestore** (`users/{uid}/farms/{farmId}/...` scoped real-time collections)
  - **Firebase App Check** (Debug provider for hackathon evaluation)

---

## Core Features

- **Farm Overview Dashboard (`HomeScreen`):** Live herd count, open tasks, today's milk yield, compact tool grid, and a draggable Gemini-style **`FloatingHybridAiOrb`** launcher.
- **FarmOS Hybrid AI (`AiTabScreen`):** Streaming Google-style unboxed responses, live `HybridInferenceStatusBar`, 4 one-tap quick prompts (*Diagnose Livestock Symptom*, *Boost Daily Milk Yield*, *Feed Ration & Cost Check*, *Heat Stress & Water Plan*), camera/gallery photo symptom input, **Save as Task**, **Copy**, **Edit**, and saved chat history.
- **Complete Livestock Operations:**
  - **Animals:** Tag-searchable herd registry (Cattle, Goats, Sheep) with status tracking.
  - **Today's Tasks:** Active, Overdue, and Completed task management with WorkManager reminders.
  - **Production & Yields:** Daily Milk & Animal Weight logs + interactive **Heat Stress Radar (THI)** gauge.
  - **Breeding:** Event logging with gestation progress ring and due-date countdowns.
  - **Feeding:** Daily feed & cost logs + **Feed Ration Calculator**.
  - **Health:** Active/resolved case tracking + symptom diagnostic view.
  - **Finances & Admin:** Monthly revenue/expense/net profit summary, **Profit Simulator**, and PIN-protected Admin access with Security Question recovery.

---

## Existing-App Disclosure & Hackathon Summary (Nov 11–30, 2026)

Per **#BuildHybridAI 2026** Section 6 rules:

- **Pre-existing Foundation:** FarmOS began as an Android Jetpack Compose + Firebase Auth/Firestore livestock record-keeping prototype (basic herd, task, feeding, breeding, production, health, and finance screens).
- **Built & Upgraded During the Submission Period (Nov 11–30, 2026):**
  - Integrated **Firebase AI Logic** (`firebase-bom:35.2.0`, `firebase-ai`, `firebase-ai-ondevice:16.0.0-beta08`, `firebase-appcheck-debug`, `minSdk 26`) and removed legacy direct API-key HTTP clients.
  - Built `HybridFarmAdvisorService` with `InferenceMode.PREFER_ON_DEVICE`, `InferenceSource` tracking (`ON_DEVICE` / `IN_CLOUD`), fast fallback timeout, and offline/quota error mapping.
  - Built `FloatingHybridAiOrb` (draggable 4-point sparkle launcher), `HybridInferenceStatusBar`, unboxed Google-style `AiTabScreen`, 4 one-tap livestock quick prompts, photo symptom attachment preview, **Save as Task**, and Copy/Edit message actions.
  - Updated `FarmOsApplication` with `Firebase.initialize` + `DebugAppCheckProviderFactory`, fixed Firestore collection path alignment, added Admin PIN security-question recovery, and polished Light/Dark Material 3 UI consistency across all screens.

---

## Build & Testing Instructions (For Judges)

### Prerequisites
- Android Studio (JDK 17+ / JBR)
- Android device or emulator running **Android 8.0+ (API 26+)**

### Local Setup
1. Clone the repository.
2. Add your Firebase project's `google-services.json` to `app/google-services.json` (excluded from Git for security).
3. Build or compile:
   ```bash
   ./gradlew :app:compileDebugKotlin --console=plain
   ./gradlew :app:assembleDebug --console=plain
   ```
4. Install the debug APK on an Android device (or use the prebuilt test APK link provided on the Devpost submission).

### How to Test Hybrid AI
1. Launch **FarmOS** and sign in with **Google** or **Email/Password** (or tap **Log in** from the guest Home screen).
2. Tap the **FarmOS Hybrid AI** card or drag/tap the floating **`AI`** orb.
3. Observe the **`HybridInferenceStatusBar`** at the top of the screen:
   - On an **AICore / Gemini Nano–supported device**, inference runs **`ON-DEVICE`** (even offline).
   - On a **standard Android device**, it automatically transitions to **`CLOUD FALLBACK`** (`gemini-3.5-flash-lite`) within seconds.
4. Try one of the 4 quick-prompt cards or attach a livestock photo, then tap **Save as Task** on the AI response to verify it saves directly to **Today's Tasks**.

---

## License

Licensed under the **Apache License, Version 2.0**  see [LICENSE](LICENSE).
