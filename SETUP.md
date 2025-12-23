# NeoBuk Setup Guide

## ✅ Project Created Successfully!

Your Jetpack Compose Android project has been set up with the following structure:

### 📁 Project Structure

```
NeoBuk/
├── app/
│   ├── src/main/
│   │   ├── java/com/neobuk/app/
│   │   │   ├── MainActivity.kt          # Main activity with Compose
│   │   │   └── ui/theme/                # Theme configuration
│   │   │       ├── Color.kt             # Color definitions
│   │   │       ├── Theme.kt             # App theme
│   │   │       └── Type.kt              # Typography
│   │   ├── res/
│   │   │   └── values/
│   │   │       ├── strings.xml          # String resources
│   │   │       └── themes.xml           # XML themes
│   │   └── AndroidManifest.xml          # App manifest
│   ├── build.gradle.kts                 # App-level build config
│   └── proguard-rules.pro               # ProGuard rules
├── gradle/                              # Gradle wrapper
├── build.gradle.kts                     # Project-level build config
├── settings.gradle.kts                  # Project settings
├── gradle.properties                    # Gradle properties
├── gradlew                              # Gradle wrapper (Unix)
└── gradlew.bat                          # Gradle wrapper (Windows)
```

### 🎯 What's Included

✅ **Jetpack Compose** - Modern declarative UI
✅ **Material Design 3** - Latest Material components
✅ **Navigation Compose** - Navigation library
✅ **Custom Theme** - NeoBuk branded colors
✅ **Sample Screen** - Working counter example

### 🚀 Next Steps

#### 1. Open in Android Studio

```bash
# Navigate to the project directory
cd /Users/leonard.mutugi/CodzureGroup/NeoBuk

# Open in Android Studio (if you have the command line tools)
open -a "Android Studio" .
```

Or manually: **File → Open** → Select NeoBuk folder

#### 2. Sync Gradle

Android Studio should automatically sync Gradle dependencies. If not:
- Click **File → Sync Project with Gradle Files**

#### 3. Run the App

- Connect an Android device or start an emulator
- Click the **Run** button (Green Play icon)
- Or use: `./gradlew installDebug`

### 🎨 Customization Guide

#### Change App Colors

Edit `app/src/main/java/com/neobuk/app/ui/theme/Color.kt`:

```kotlin
val NeoBukPrimary = Color(0xFF1E88E5)  // Change this
val NeoBukSecondary = Color(0xFF26A69A) // And this
```

#### Modify Main Screen

Edit `app/src/main/java/com/neobuk/app/MainActivity.kt`:

```kotlin
@Composable
fun MainScreen() {
    // Your composables here
}
```

#### Add New Screens

Create new Kotlin files in `app/src/main/java/com/neobuk/app/`:

```kotlin
@Composable
fun MyNewScreen() {
    // Your UI here
}
```

### 📦 Dependencies Included

- **androidx.core:core-ktx:1.12.0**
- **androidx.lifecycle:lifecycle-runtime-ktx:2.6.2**
- **androidx.activity:activity-compose:1.8.1**
- **androidx.compose:compose-bom:2023.10.01**
- **androidx.compose.material3:material3**
- **androidx.navigation:navigation-compose:2.7.5**

### 🛠️ Useful Commands

```bash
# Build the project
./gradlew build

# Clean build
./gradlew clean

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test

# Check for dependency updates
./gradlew dependencyUpdates
```

### 📱 Minimum Requirements

- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34

### 🐛 Troubleshooting

#### Gradle Sync Failed

1. Check your internet connection
2. Clear Gradle cache: `./gradlew clean`
3. Invalidate caches: **File → Invalidate Caches / Restart**

#### Build Errors

- Ensure you're using Android Studio Hedgehog or later
- Check Java/Kotlin versions match requirements
- Update Android SDK if needed

### 📚 Learning Resources

- [Jetpack Compose Basics](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Compose Samples](https://github.com/android/compose-samples)

---

**Happy Coding! 🎉**

Your NeoBuk app is ready to build amazing Android experiences with Jetpack Compose!
