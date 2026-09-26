# 최신 보안 패치 통신 정책

최신 배포판은 [SECURITY-PATCH.md](SECURITY-PATCH.md)를 기준으로 합니다. 자동 ipify·전화번호 지역 조회·Geocoder 역지오코딩을 제거했습니다. 원격 제어와 FRP는 비활성화했고 네이티브 libgojni.so를 APK에서 제외합니다. HTTP 기반 전송은 HTTPS 및 인증서 검증을 사용하고 자동 리다이렉트를 따르지 않습니다. 문자·이메일 전송은 해당 프로토콜 정책이 별도로 적용됩니다. 전체 의존성 전수 감사나 실제 기기 트래픽 검증은 수행하지 않았습니다.

## 과거 기록 (초기 APK 전용)

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
| Public IP lookup | `App.kt`, `receiver/NetworkChangeReceiver.kt`, `workers/NetworkWorker.kt` | Network-change receiver queues the worker; it calls api.ipify.org/api6.ipify.org before checking whether user tasks exist | Present in published source 1581e5a; no opt-in guard. Removal/default-off gating remains unresolved. |
| Telegram | `utils/sender/TelegramUtils.kt` | Sends to user-selected Bot API or custom URL | Preserve with secret-safe logs. |
| Webhook and other channels | `utils/sender/*` | Send to user-configured endpoints | Preserved with normal TLS verification; persisted request diagnostics redact sensitive values. |

The clean Stage C universal debug APK contains `libgojni.so` for arm64-v8a,
armeabi-v7a, x86, and x86_64, and contains no `libumeng-spy.so` artifact.
Device-level runtime traffic remains an acceptance test in stage H.

## Published APK follow-up audit (2026-09-26)

Target: application source `1581e5abead018cde410bba6b974176927b1d035`, public APK versionCode base 56.
This is a source audit, not handset packet capture or a complete review of embedded native/dependency binaries.

- Confirmed automatic third-party request path: normal (non-pure-client) application initialization registers the network receiver. A changed connected network queues NetworkWorker; its first operations are GET requests to ipify. A user network automation task is not required. No message body, phone number or Bot token is included in these two GET URLs; the recipient can still observe source IP and request metadata.
- Conditional phone-number lookup: `cx.shouji.360.cn/phonearea.php?number=...` remains behind `enablePhoneAreaLookup`, default false, and the phone-area template path. If enabled and invoked, the queried number is sent externally.
- Conditional location address lookup: LocationService invokes Android Geocoder.getFromLocation with coordinates. Location is default off; the platform geocoder may use a network backend, depending on the device.
- Residual HTTP default: XHttpSDK base URL is gitee.com. Initialization itself is not a request; reviewed app callers mostly use absolute endpoint URLs. This audit does not prove that every user-supplied relative URL or dependency path avoids that default.
- Sender services include Telegram, email, Webhook/Gotify/Bark, ServerChan, PushPlus, DingTalk, WeCom and Feishu. Sending content to these services follows selected sender/rule or test/task actions. Choosing one's Bot does not make the provider's infrastructure self-hosted.
- FRP is bundled and uses user-started/configured relay settings; the seeded 88.88.88.88 example is not a personal endpoint and should be replaced before running it. The example record is initially disabled.
- GitHub help/source/release links are opened by user action. Source attribution links in Feishu cards are payload links, not extra app-side upload requests. Framework WebView fallback links still exist.
- Build repositories are separate from APK runtime endpoints.
- Umeng, XUpdate, remote tips fetch and upstream native download paths were removed. Reviewed application source does not show automatic upload of all messages to the upstream author. A full traffic/native/dependency audit has not been completed.
- The bundled privacy text's statement that external transfers occur only through configured forwarding should not be treated as a complete description of this release: the automatic ipify path is an exception. A later APK must correct the behavior and privacy disclosure together.

App picker source findings:
- Installed-app categories and startup preloading default to false in SettingUtils.
- RulesEditFragment.initAppSpinner returns without presenting choices when both category flags are false.
- NotificationListener access is a separate requirement from local installed-app enumeration.
- LoadAppListWorker does not reset LoadingAppList in its exception path; a loading failure can prevent another load in the same process. The user's actual screen has not yet been reproduced.

Official references:
- [Play Protect warning guidance](https://developers.google.com/android/play-protect/warning-dev-guidance)
- [Android restricted settings](https://support.google.com/android/answer/12623953?hl=ko)
- [Android Geocoder](https://developer.android.com/reference/android/location/Geocoder)
