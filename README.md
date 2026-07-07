<div align="center">
  <img src="app/src/main/res/mipmap-xxhdpi/ic_launcher.png" width="120" alt="Link Log Logo">
  <h1>Link Log</h1>
  <p><b>Your Personal Command Center for Saved Links and Media.</b></p>
</div>

## 🚀 Overview

**Link Log** is a modern, privacy-focused Android application designed to help you organize, categorize, and archive your digital life. Whether you're saving articles, curating YouTube playlists, or archiving social media reels, Link Log puts you in control.

Built with bleeding-edge Android technologies—Jetpack Compose, Hilt, Room, and Media3—Link Log delivers a blazing-fast, buttery-smooth experience.

## ✨ Features

- **📂 Infinite Organization**: Structure your saves with Groups and Folders.
- **⚡ Quick Paste & Share**: Intercept links shared from any app, or auto-paste instantly from your clipboard.
- **🎵 Deep Media Extraction**: Automatically extracts rich metadata (titles, descriptions, thumbnails) from YouTube, Instagram, X/Twitter, and more.
- **📥 Native Downloader**: Built-in `yt-dlp` integration allows you to save videos and audio locally at your preferred quality.
- **▶️ In-App Player**: Play downloaded media seamlessly without leaving the app using the integrated ExoPlayer.
- **🎨 Dynamic Theming**: Toggle between an immersive Deep Indigo (Dark Mode) or a Warm Amber (Light Mode).
- **💾 Full Data Ownership**: Export your entire database into a zip file and restore it whenever you want. No cloud accounts required.

## 🛠️ Tech Stack

- **UI**: Jetpack Compose, Material Design 3
- **Architecture**: MVVM + Repository Pattern
- **Dependency Injection**: Hilt
- **Local Database**: Room (SQLite)
- **Media Player**: Media3 (ExoPlayer)
- **Downloader**: `youtubedl-android` (yt-dlp wrapper)
- **Async & Concurrency**: Kotlin Coroutines & Flow

## 📦 Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/Synthetic-Sage/Link-Log.git
   ```
2. Open the project in **Android Studio** (Koala or newer recommended).
3. Let Gradle sync and resolve dependencies.
4. Build and deploy to your emulator or physical device.

## 🤝 Contributing

Contributions, issues, and feature requests are always welcome! Feel free to check the [issues page](https://github.com/Synthetic-Sage/Link-Log/issues) if you want to contribute.

## 📝 License

This project is open-source. Feel free to fork, modify, and distribute as per standard open-source conventions.
