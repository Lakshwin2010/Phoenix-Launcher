# Phoenix Launcher (iOS Theme × KWGT Engine for Samsung)

**Phoenix Launcher** is a custom Android home launcher engineered for **Samsung Galaxy devices (One UI 5/6/7)**, merging the refined **iOS Frosted Glass & Squircle UX** with the deep modularity of **Kustom Widget (KWGT)** designs.

---

## 🌟 Key Features

### 1. 🎨 iOS Aesthetic & One UI Polish
- **Continuous Curvature Squircles**: Smooth superellipse app icons (22.5% radius) matching iOS HIG and Samsung AMOLED display curves.
- **Frosted Glass Blur (Glassmorphism)**: Translucent, elevated cards with subtle ambient glow and border reflections (`backdrop-filter` style rendering).
- **iOS App Library**: Automatic smart grouping into 2x2 folder widgets (*Social & Chat*, *Creativity & Media*, *Utilities*, *Productivity*, *Games*).
- **Frosted Floating Dock**: 4 quick-access slots for dialer, messaging, web, and camera.
- **Spotlight Search**: Swipe-down or tap search bar with instant fuzzy index for all installed apps.
- **Dynamic Island Pill**: Floating top activity pill showing media playback and charging telemetry with interactive expansion.

### 2. 🧩 Kustom Widget (KWGT) Inspired Widgets
- **Clock & Dual Weather Widget**: Bold KWGT typography, dynamic date, real-time temperature, condition emoji, and next calendar sync agenda.
- **Hardware Telemetry Rings**: SnapDragon/Exynos monitor displaying live Battery %, Storage usage, and RAM meter using circular canvas arcs.
- **Now Playing Music Player**: Dynamic album art background, waveform progress bar, and play/pause/skip touch targets.
- **Quick Hub (Control Center)**: Modular toggle tiles for Wi-Fi, Bluetooth, Flashlight, and Focus Mode.
- **Smart Stack**: Horizontal pager with smooth spring physics and iOS-style pagination pill indicators.

---

## 📁 Project Architecture

```
Phoenix/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # Configured with ACTION_MAIN, CATEGORY_HOME, QUERY_ALL_PACKAGES
│   │   ├── java/com/phoenix/launcher/
│   │   │   ├── MainActivity.kt          # Edge-to-edge, wallpaper transparency, back press handling
│   │   │   ├── LauncherViewModel.kt     # App states, live filtering, and screen routing
│   │   │   ├── data/
│   │   │   │   └── AppRepository.kt     # LauncherApps / PackageManager query and categorization
│   │   │   ├── model/
│   │   │   │   └── AppInfo.kt           # App model & category definitions
│   │   │   ├── ui/
│   │   │   │   ├── components/
│   │   │   │   │   ├── FrostedGlassCard.kt
│   │   │   │   │   ├── SquircleIcon.kt
│   │   │   │   │   ├── IosDock.kt
│   │   │   │   │   ├── DynamicIslandPill.kt
│   │   │   │   │   └── SpotlightSearchBar.kt
│   │   │   │   ├── screens/
│   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   ├── AppLibraryScreen.kt
│   │   │   │   │   └── SpotlightScreen.kt
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Theme.kt
│   │   │   │   │   └── Type.kt
│   │   │   │   └── widgets/
│   │   │   │       ├── KwgtSmartStack.kt
│   │   │   │       ├── ClockWeatherWidget.kt
│   │   │   │       ├── DeviceHardwareWidget.kt
│   │   │   │       ├── NowPlayingMediaWidget.kt
│   │   │   │       └── QuickHubWidget.kt
│   │   │   └── res/
│   │   │       ├── values/ (themes.xml, colors.xml, strings.xml)
│   │   │       └── drawable/ (Adaptive launcher icons)
│   │   └── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🚀 How to Run & Test

### Option A: Using Android Studio
1. Open **Android Studio** located at `E:\LakshwinAnand\Android\android-studio\bin\studio64.exe`.
2. Choose **Open** and select `E:\LakshwinAnand\App\Phoenix`.
3. Allow Gradle to sync dependencies.
4. Select your connected Samsung phone (or the pre-configured `Pixel_10_Pro_XL` AVD).
5. Click **Run ▶**.

### Option B: Setting as Default Launcher on Samsung Phone
1. Once installed on your phone, go to **Settings > Apps > Choose default apps**.
2. Tap **Home app**.
3. Select **Phoenix Launcher**.
4. Press Home or swipe up to experience the iOS x KWGT launcher with live wallpaper transparency.
