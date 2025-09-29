# FuPlay - Simple Android Video Player

A simple, lightweight video player for Android devices built with Kotlin and ExoPlayer.

## Features

- **Video File Selection**: Browse and select video files from device storage
- **Full-Screen Playback**: Immersive video viewing experience
- **Modern UI**: Clean Material Design 3 interface
- **ExoPlayer Integration**: Robust video playback with standard controls
- **Permission Handling**: Smart storage permission management for all Android versions
- **Landscape Orientation**: Optimized for landscape video viewing

## Screenshots

The app consists of two main screens:
1. **Main Screen**: File selection and app launch point
2. **Player Screen**: Full-screen video playback with controls

## Installation

### From Release (Recommended)
1. Go to the [Releases](https://github.com/kmfoysal06/fuplay/releases) page
2. Download the latest `fuplay-vX.X.X.apk` file
3. Enable "Install from unknown sources" in your Android settings
4. Install the APK file

### Build from Source
1. Clone this repository
2. Open in Android Studio
3. Build and run on your device

Or use Gradle command line:
```bash
./gradlew assembleDebug
```

## Usage

1. **Launch the app** - FuPlay will appear in your app drawer
2. **Grant permissions** - Allow storage access when prompted
3. **Select video** - Tap "Select Video" to browse your files
4. **Play video** - Tap "Play" to start watching in full-screen

## Technical Details

- **Minimum Android Version**: API 21 (Android 5.0)
- **Target Android Version**: API 34 (Android 14)
- **Video Library**: ExoPlayer 2.19.1
- **UI Framework**: Material Design 3
- **Programming Language**: Kotlin
- **Architecture**: Standard Android Activities

## Development

### Build Requirements
- Android Studio or Gradle 8.2+
- JDK 17
- Android SDK with API 34

### Building
```bash
# Debug build
./gradlew assembleDebug

# Release build  
./gradlew assembleRelease
```

### GitHub Actions
This project includes automated CI/CD:
- Builds APK on every push/PR
- Creates releases with APK attachments for tagged commits
- Use tags like `v1.0.0` to trigger releases

## Contributing

Feel free to submit issues and pull requests to improve FuPlay!

## License

This project is open source. Feel free to use and modify as needed.