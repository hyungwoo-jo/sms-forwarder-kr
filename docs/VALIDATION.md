# 보안 패치 검증 결과

앱 소스 커밋: `e5570f590fdc235a0eb9045771651e7bec30d14b`. 빌드 시 working tree clean.

- 문자 및 앱 알림 전달 유지. 기본 versionCode 57.
- JVM 테스트 12개, 실패·오류 0개.
- 디버그·릴리스 빌드 성공.
- debug lint: 오류 0개, 경고 276개.
- release lint: 오류 0개, 경고 276개.
- APK 중국어 리소스 0개, 원본 템플릿 태그 30개 보존.
- 개인 서명 v1/v2 검증, 기존 인증서와 동일.
- APK LICENSE 원문 바이트 동일, non-debuggable, Umeng DEX 제외.
- APK libgojni.so 제외 및 병합 권한 allowlist 검증 통과.
- 외부 HTTP와 미설정 URL 차단, loopback 허용 및 리다이렉트 인증정보 전달 방지 테스트 통과.
- Webhook 본문·Telegram 전송 및 신뢰/비신뢰 TLS 회귀 테스트 통과.

## APK

- [universal APK](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/download/v3.5.0.260926-kr-e5570f5/SmsKR_3.5.0.260926-kr-e5570f5_100057_universal_release.apk) — versionCode 100057
  - SHA-256: `5a6cf44dffb2b53d0b41f319cd2b29115ab3c4530855530f3ff586fcfff27404`
- [arm64-v8a APK](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/download/v3.5.0.260926-kr-e5570f5/SmsKR_3.5.0.260926-kr-e5570f5_300057_arm64-v8a_release.apk) — versionCode 300057
  - SHA-256: `8c943e3eaac5372da07aa5deaf4a10ebdd34e2cace659574db9e2ff6dfa3aec8`

## 실제 기기 검증

DEVICE_PENDING: 연결 휴대폰이 없어 설치·WORKS 앱 선택/실제 알림·SMS 수신·재부팅/절전 및 Play Protect 판정을 확인하지 못했습니다. Watch는 사용자가 워치 알림까지 사용하려는 경우에만 검증합니다. 전체 의존성 또는 실제 트래픽 전수 보안 감사는 아닙니다.

[보안 변경과 사용 제한](SECURITY-PATCH.md)
