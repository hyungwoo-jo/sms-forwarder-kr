# Network audit

This document records the upstream baseline and the Stage C result. It
distinguishes runtime requests from repositories contacted only while Gradle
resolves build dependencies.

| Feature | Source location | Observed baseline behaviour | Planned action |
|---|---|---|---|
| Umeng analytics | `utils/sdkinit/UMengInit.kt`, `App.kt` | Initializes release analytics after privacy consent | Removed SDK, initialization, calls, rules, and obsolete privacy notice. |
| App update | `utils/sdkinit/XUpdateInit.kt`, `MainActivity.kt` | Automatic and manual requests to upstream update service | Removed; the About screen opens this fork's releases page only on user action. |
| Remote tips | `widget/GuideTipsDialog.kt` | Fetches a remote tips URL | Replaced with packaged personal-build guidance. |
| FRP library | `MainActivity.kt`, `Constants.kt` | Downloads `libgojni.so` from upstream service | Bundled in `src/main/jniLibs` for all four ABIs; no runtime download. |
| Phone-area lookup | `PhoneUtils.kt`, `MsgInfo.kt` | Sends a phone number to `cx.shouji.360.cn` when template uses the tag | Explicit, default-off settings control added. |
| Public IP lookup | `workers/NetworkWorker.kt` | Contacts ipify for a network feature | Verify trigger and disable unsolicited lookup. |
| Telegram | `utils/sender/TelegramUtils.kt` | Sends to user-selected Bot API or custom URL | Preserve with secret-safe logs. |
| Webhook and other channels | `utils/sender/*` | Send to user-configured endpoints | Preserved with normal TLS verification; persisted request diagnostics redact sensitive values. |

The clean Stage C universal debug APK contains `libgojni.so` for arm64-v8a,
armeabi-v7a, x86, and x86_64, and contains no `libumeng-spy.so` artifact.
Device-level runtime traffic remains an acceptance test in stage H.
