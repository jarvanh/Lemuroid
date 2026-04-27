# Android TV 端 ROM 扫描与目录选择修复说明

**相关提交记录 (Commits):**
- `58618ee3` (2026-04-27 09:57:09) - feat: improve ROM directory path resolution and add display functionality to TV settings
- `71dd99bd` (2026-04-27 09:57:09) - refactor: update external folder resolution to include legacy paths and restrict library operation monitoring to indexing only

## 问题背景
在 Android TV（尤其是国内魔改盒子、投影仪及老旧系统）上，用户在 Lemuroid 设置中选择游戏 ROM 目录时，经常会遇到以下问题：
1. **闪退 (Flash close)**：点击“选择目录”后瞬间回到原界面，没有任何反应。
2. **扫描失败**：好不容易选中了目录，返回后显示“无 (None)”，且无法扫描到任何 ROM 游戏。

## 根因分析
1. **SAF 框架被阉割**：Lemuroid 默认依赖 Android 官方的 Storage Access Framework (SAF，即 `ACTION_OPEN_DOCUMENT_TREE` Intent) 来选择目录。但许多智能电视和机顶盒系统深度定制，直接删除了系统原生的文件选择器组件，导致 Intent 抛出 `ActivityNotFoundException`，引发闪退。
2. **分区存储 (Scoped Storage) 权限收紧**：Android 11+ 引入了严格的分区存储。电视设备如果没有正确适配 `MANAGE_EXTERNAL_STORAGE` 权限，或者 SAF 授权回调失败，即使拿到路径也无法读取文件。
3. **设置项状态不同步 (UI 解析异常)**：为了解决 SAF 缺失的问题，代码中引入过传统的 Legacy 文件选择器（获取 `/storage/emulated/0/...` 绝对路径）并存入 `pref_key_legacy_external_folder`。但移动端的 `SettingsScreen` 和电视端的 UI 依旧强制要求解析 `content://` 格式的 SAF URI，导致解析常规路径失败，直接在 UI 上显示为 “None” 或抛出异常。

## 具体修复逻辑

### 1. SDK 与权限适配
- 将 `targetSdkVersion` 降级至 29，并在 AndroidManifest 中保留 `requestLegacyExternalStorage="true"`。这允许 Lemuroid 在较老的电视系统上绕过复杂的分区存储限制，直接通过传统绝对路径遍历文件。
- 完善了传统的 `READ_EXTERNAL_STORAGE` 和 `WRITE_EXTERNAL_STORAGE` 请求流程，确保在 Legacy 选取器弹出前赋予正确的 I/O 权限。

### 2. 目录数据的平滑兼容与解析 (UI 修复)
- **TV 端配置回显**：修改了 `tv_settings.xml` 和 `TVSettingsFragment.kt`，新增了显式的 ROM 目录首选项（Preference）。现在系统会优先尝试读取 SAF 格式路径，若为空则自动读取 Legacy 绝对路径，让用户清楚地看到自己选中的路径。
- **容错解析兜底**：在 `SettingsScreen.kt` 及相关配置读取处，修改了路径格式化逻辑。当系统尝试使用 `DocumentFile.fromTreeUri()` 解析传统绝对路径（例如 `/storage/xxx`）抛出 `IllegalArgumentException` 时，不再崩溃或返回空值，而是直接将该绝对路径作为 String 返回展示。

### 3. 解除扫描阻塞机制
- 优化了 `PendingOperationsMonitor` 的判断逻辑。过去任何后台操作都可能阻塞 ROM 扫描任务，修复后将阻塞条件严格限制在 `LIBRARY_INDEX` (库索引) 进行时，防止由于状态流管理不当造成的“假死”或“无法触发扫描”。

## 最终效果
无论用户的 Android TV 是否支持现代的 SAF 系统文件管理器，Lemuroid 都可以通过自身的 Legacy Picker 平稳地拿到绝对路径，在 UI 上正确回显，并无障碍地扫描和索引该路径下的所有 ROM 文件。
