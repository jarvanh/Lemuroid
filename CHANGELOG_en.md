# Changelog

All notable changes to this project will be documented in this file.

## [Recent Updates]

### 🎮 Gamepad & Inputs
**Commit:** `f675a74e` (2026-04-27 11:07:15)
- **Enhanced Compatibility**: Completely overhauled the gamepad detection logic (`LemuroidInputDeviceGamePad.kt`). Removed the strict requirement for Android's `hasKeys()` to report specific buttons (like A/B/X/Y). This massively improves out-of-the-box compatibility for Google Stadia controllers, generic Chinese gamepads, and TV remotes that inaccurately report their capabilities to the OS.
- **Smart Bindings Feedback**: Added real-time UI detection in `TVGameActivity.kt`. If a gamepad connects but fails to map its default keys correctly (or is disabled), the system now triggers specific toast notifications guiding the user to the "External devices" settings to manually rebind their keys.
- **Default Shortcuts**: Updated the default Game Menu shortcut to prioritize `L1+R1`, providing a more natural and reliable fallback for TV controllers lacking thumbstick clicks.

### 📺 Android TV Storage & UX
**Commit:** `8897570c` (2026-04-27 11:07:15), `11b796d8` (2026-04-27 11:07:15), `d0d25d90` (2026-04-27 11:07:14)
- **ROM Directory Fix**: Resolved the "Flash close" and "None" display issues when selecting ROM directories on Android TV.
- **Path Resolution Resiliency**: Updated `SettingsScreen.kt` and `TVSettingsFragment.kt` to gracefully handle both Storage Access Framework (SAF) URIs (`content://`) and Legacy Absolute Paths (`/storage/emulated/0/...`). If SAF parsing fails, it safely falls back to standard string display.
- **Storage Access Downgrade**: Temporarily downgraded `targetSdkVersion` to 29 to bypass Scoped Storage limitations on restrictive TV boxes, ensuring the legacy folder picker (`TVFolderPickerLauncher`) successfully reads and indexes game files.
- **UI Enhancements**: Added an explicit ROM directory display preference (`pref_key_tv_rom_directory`) to the TV Settings UI, allowing users to verify their selected paths instantly.

### 🛠 Refactoring & Chores
**Commit:** `f675a74e` (2026-04-27 11:07:15)
- **Operation Monitoring**: Restrict library operation monitoring (`PendingOperationsMonitor`) specifically to library indexing to prevent unrelated background operations from silently blocking ROM scans.
- **Cleanup**: Removed temporary remote logging scripts (`RemoteLogger.kt`, `log_server.py`) and associated logic previously used for device debugging without ADB.
