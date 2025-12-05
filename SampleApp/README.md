# PayPipes SDK Sample App

This is a complete, standalone Android project demonstrating PayPipes SDK integration.

## Quick Start

1. Open this directory in Android Studio
2. Android Studio should automatically detect it as a Gradle project
3. Wait for Gradle sync to complete
4. Update credentials in `app/src/main/java/com/punext/paypipes/example/MainActivity.kt`:
   - Find `createConfiguration()` method
   - Replace `clientId` and `clientSecret` with your actual credentials
5. Build and run

## Project Structure

- `app/`: Application module
  - `src/main/java/com/punext/paypipes/example/`: Source files
    - `MainActivity.kt`: Main activity with SDK integration examples
    - `PayPipesExampleApplication.kt`: Application class
  - `src/main/res/`: Resources (layouts, strings, etc.)
  - `build.gradle.kts`: App module build configuration
- `build.gradle.kts`: Root build configuration (plugin versions)
- `settings.gradle.kts`: Project settings with Maven repository
- `gradle.properties`: Gradle configuration

## Features Demonstrated

- ✅ Card payment processing
- ✅ Card storage for future payments
- ✅ Billing address handling
- ✅ Custom theming
- ✅ Result callbacks
- ✅ Error handling

## SDK Dependency

The SDK is included via Maven repository:
```kotlin
    implementation("com.punext:paypipes:1.0.0")
```

The Maven repository is configured in `settings.gradle.kts` to point to the `../repository` directory.

## Note

⚠️ **Important**: Replace the test credentials in `MainActivity.kt` with your actual credentials before running in production.

See the main README.md for detailed integration instructions.
