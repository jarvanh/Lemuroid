# Android TV ROM Scanning and Directory Selection Fix Explanation

**Related Commits:**
- `58618ee3` (2026-04-27 09:57:09) - feat: improve ROM directory path resolution and add display functionality to TV settings
- `71dd99bd` (2026-04-27 09:57:09) - refactor: update external folder resolution to include legacy paths and restrict library operation monitoring to indexing only

## Background
On Android TV (especially highly customized boxes, projectors, and older systems), users often encounter the following issues when selecting a game ROM directory in Lemuroid settings:
1. **Flash close**: The app instantly returns to the previous screen immediately after clicking "Select Directory", with no response.
2. **Scanning Failure**: Even if a directory is successfully selected, returning to the settings shows "None", and no ROMs can be scanned.

## Root Cause Analysis
1. **Disabled SAF Framework**: Lemuroid defaults to using Android's official Storage Access Framework (SAF, specifically the `ACTION_OPEN_DOCUMENT_TREE` Intent) for directory selection. However, many smart TVs and set-top boxes have heavily customized systems that completely remove the native system file picker component. This causes the Intent to throw an `ActivityNotFoundException`, leading to the "flash close".
2. **Strict Scoped Storage Permissions**: Android 11+ enforces strict Scoped Storage. If a TV device lacks proper support for the `MANAGE_EXTERNAL_STORAGE` permission, or if the SAF authorization callback fails, the app cannot read files even after obtaining a path.
3. **Settings State Desync (UI Parsing Exception)**: To bypass the missing SAF issue, previous code introduced a traditional Legacy file picker (which retrieves absolute paths like `/storage/emulated/0/...`) and saved it to `pref_key_legacy_external_folder`. However, the `SettingsScreen` on mobile and the TV UI still forced the parsing of `content://` format SAF URIs. This caused the parsing of standard absolute paths to fail, resulting in the UI directly displaying "None" or throwing exceptions.

## Detailed Fix Implementation

### 1. SDK and Permission Adaptation
- Downgraded `targetSdkVersion` to 29 and preserved `requestLegacyExternalStorage="true"` in the AndroidManifest. This allows Lemuroid to bypass complex Scoped Storage restrictions on older TV systems and iterate files directly via traditional absolute paths.
- Perfected the legacy `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE` request flow, ensuring correct I/O permissions are granted before popping up the Legacy picker.

### 2. Smooth Path Compatibility and Parsing (UI Fix)
- **TV Settings UI Echoing**: Modified `tv_settings.xml` and `TVSettingsFragment.kt` to add an explicit ROM Directory preference. The system now prioritizes reading SAF format paths; if empty, it automatically reads the Legacy absolute path, allowing users to clearly see their selected directory.
- **Fault-Tolerant Fallback**: Refactored the path formatting logic in `SettingsScreen.kt` and configuration read points. When the system attempts to use `DocumentFile.fromTreeUri()` to parse a traditional absolute path (e.g., `/storage/xxx`) and throws an `IllegalArgumentException`, it no longer crashes or returns null. Instead, it directly returns the absolute path as a plain String for display.

### 3. Removing Scanning Blockers
- Optimized the `PendingOperationsMonitor` condition logic. Previously, any background operation could block the ROM scanning task. The fix strictly limits the blocking condition to when `LIBRARY_INDEX` is in progress, preventing unrelated state flows from causing "fake hangs" or preventing scans from triggering.

## Final Result
Regardless of whether a user's Android TV supports the modern SAF system file manager, Lemuroid can now smoothly obtain the absolute path via its internal Legacy Picker, correctly display it in the UI, and flawlessly scan and index all ROM files within that directory.
