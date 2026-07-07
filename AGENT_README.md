# AI Agent Context Guide

If you are an AI assistant tasked with modifying or extending **Link Log**, read this document first. It outlines the architectural decisions, constraints, and standard operating procedures for this codebase.

## 🏗️ Architecture
- **Framework**: Android natively built entirely using **Jetpack Compose**. Avoid XML layouts; they are explicitly not used.
- **Pattern**: MVVM (Model-View-ViewModel) paired with Repository patterns.
- **DI**: Hilt. Ensure you use `@HiltViewModel` for ViewModels, and inject dependencies into composables via `hiltViewModel()`.
- **Database**: Room database (`LinkLogDatabase.kt`).
  - `GroupDao`, `FolderDao`, `LinkDao`.
  - Links belong to Folders. Folders belong to Groups.
  - Links have a `userRank` property which is used for custom drag-and-drop reordering. Do not break the sequential indexing logic (`updateLinkRankAndShift` in `LinkRepository.kt`).

## 🎨 UI/UX Guidelines
- **Spacing**: The UI enforces an 8dp grid spacing system (e.g., `16.dp` padding, `8.dp` gaps, `12.dp` card internals). Refer to `Spacing` object in `Theme.kt`.
- **Colors**: Theming is strict. 
  - Dark Mode: Deep indigo (`#1A1B2E` base, `#4C3AA0` accent).
  - Light Mode: Warm amber (`#FFF8ED` base, `#E8890C` accent).
  - Do NOT hardcode colors; use `MaterialTheme.colorScheme` tokens.
- **Components**: We use customized components (e.g., `AddLinkBottomSheet`, `CylinderDropdown`). Use these existing components rather than building new ad-hoc versions.

## ⚙️ Key Services
- **`LinkScraper.kt`**: Responsible for URL metadata extraction. It cascades through OpenGraph tags, YouTube Data API, and finally falls back to `yt-dlp` using `youtubedl-android`.
- **`DownloadService.kt`**: A Foreground Service that handles media downloading via `yt-dlp`. It updates a `DownloadRepository` which the UI observes.
- **`BackupManager.kt`**: Uses the Storage Access Framework (SAF) to zip and backup the Room SQLite files (`linklog.db`, `-shm`, `-wal`).

## ⚠️ Important Constraints
1. **Never use wildcard imports** if avoidable, except for `androidx.compose.*` in standard UI files.
2. **Deprecations**: Ensure no deprecated Compose APIs are introduced. (e.g., use `LocalClipboard` instead of `LocalClipboardManager`).
3. **Database migrations**: If you change an Entity, you MUST increment the version in `LinkLogDatabase.kt` and provide a migration, otherwise the app will crash on startup for existing users.
