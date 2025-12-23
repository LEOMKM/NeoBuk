# NeoBuk - Jetpack Compose Android App

A modern Android application built with Jetpack Compose.

## 📱 Features

- 100% Jetpack Compose UI
- Material Design 3
- Modern Android architecture
- Kotlin-based

## 🛠️ Tech Stack

- **Kotlin** - Programming language
- **Jetpack Compose** - Modern UI toolkit
- **Material 3** - Design system
- **Navigation Compose** - Navigation component
- **Gradle 8.2** - Build system

## 📋 Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK 34
- Minimum SDK: 24 (Android 7.0)

## 🚀 Getting Started

### Build the project

```bash
./gradlew build
```

### Run on emulator or device

```bash
./gradlew installDebug
```

Or open the project in Android Studio and click Run.

## 📂 Project Structure

```
NeoBuk/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/neobuk/app/
│   │       │   ├── MainActivity.kt
│   │       │   └── ui/theme/
│   │       │       ├── Color.kt
│   │       │       ├── Theme.kt
│   │       │       └── Type.kt
│   │       ├── res/
│   │       │   └── values/
│   │       │       ├── strings.xml
│   │       │       └── themes.xml
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## 🎨 Customization

### Colors

Edit `app/src/main/java/com/neobuk/app/ui/theme/Color.kt` to customize your color palette.

### Typography

Edit `app/src/main/java/com/neobuk/app/ui/theme/Type.kt` to customize fonts and text styles.

### Theme

Edit `app/src/main/java/com/neobuk/app/ui/theme/Theme.kt` to customize the overall theme.

## 📝 License

This project is created for development purposes.

## 👨‍💻 Development

To start developing:

1. Open Android Studio
2. Click "Open" and select this project directory
3. Wait for Gradle sync to complete
4. Run the app on an emulator or physical device

Happy coding! 🎉
