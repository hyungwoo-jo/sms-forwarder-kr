# 기능 복원·UI 개선판 검증

앱 소스 커밋: `019d53ab58ff458783fcf9ae84e612c164a5bf72`. 빌드 시 working tree clean.
기본 versionCode 58, 기존 앱 ID와 개인 서명 유지.

- JVM 테스트 14개, 실패·오류 0개.
- Webhook·Telegram·TLS·외부 HTTP/리다이렉트 정책 회귀 테스트 통과.
- 암호화 백업 왕복, 무작위 암호문, 잘못된 비밀번호와 변조 거부 테스트 통과.
- 디버그·릴리스 빌드 성공.
- debug lint: 오류 0개, 경고 285개.
- release lint: 오류 0개, 경고 285개.
- APK v1/v2 서명 검증 및 기존 개인 인증서 동일 확인.
- 원저작권 LICENSE 원문 바이트 동일, non-debuggable, Umeng DEX 제외.
- 전체 APK 리소스 중국어 0개, canonical 태그 30개 보존.
- FRP libgojni.so 제외, 최종 병합 권한 allowlist 검증 통과.
- 통화·연락처 기능에 필요한 권한만 복원. 전체 저장소·위치·블루투스 권한은 제외 유지.

## APK

- [universal APK](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/download/v3.5.0.260926-kr-019d53a/SmsKR_3.5.0.260926-kr-019d53a_100058_universal_release.apk) — versionCode 100058
  - SHA-256: `8c1e5caad9159508996aa63c32aa801ae6f45494c4c961f7db765165ed313d73`
- [arm64-v8a APK](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/download/v3.5.0.260926-kr-019d53a/SmsKR_3.5.0.260926-kr-019d53a_300058_arm64-v8a_release.apk) — versionCode 300058
  - SHA-256: `0a78809aaf602975047ccebcf1a3e9ef57ba9514f88a2486a630e995abec25c3`

## 실제 기기 확인

DEVICE_PENDING: 연결 기기가 없어 UI 화면 배치·큰 글자·앱 검색 선택, 실제 문자/WORKS/통화 전달, 연락처 조회, 예약 실행, 파일 선택기와 실제 백업/복원, 재부팅/절전 및 Play Protect를 확인하지 못했습니다. 암호화 유닛 테스트는 Android 파일 선택기와 실제 DB 복원 검증을 대신하지 않습니다. Watch는 사용자가 워치 알림까지 사용하는 경우만 검증합니다.

## 2026-09-27 로그 시간 수정 검증

앱 소스: `923868e82d439e3eba7845f6708abcc3537861a0`, 빌드 시 clean. 기본 versionCode 59.

- 시간 단위·날짜 경계·미설정/미래 시각 테스트 3개 포함 총 17개 테스트, 실패/오류 0.
- debug/release lint 각각 오류 0, 경고 285.
- universal/arm64-v8a 서명이 기존 인증서와 일치, 권한/라이선스/리소스 검사 통과.
- APK DEX에서 `%d分钟前`, `%d秒前`, `今天%tR`, `昨天%tR`, `刚刚` 문자열 제거 확인.
- 기존 XML 검사만으로는 라이브러리 코드가 반환하는 중국어 UI를 검출하지 못했음. 상대 시간 함수 호출 검사와 화면 자산 검사를 추가.
- 호환 템플릿·OTP 정규식·의존 라이브러리 내부에는 중국어가 남을 수 있으며, 전체 DEX 중국어 0개라는 의미는 아님.
- 연결된 기기 없음. 실제 화면과 새 WORKS 알림 전달 검증은 DEVICE_PENDING 유지.

최신 릴리스: [v3.5.0.260927-kr-923868e](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/tag/v3.5.0.260927-kr-923868e)

- [universal APK](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/download/v3.5.0.260927-kr-923868e/SmsKR_3.5.0.260927-kr-923868e_100059_universal_release.apk) — SHA-256 `65f556d31f476772be24c0000d298ae8b2cad4d61bcd6f48be4367995ccb8357`

- [arm64-v8a APK](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/download/v3.5.0.260927-kr-923868e/SmsKR_3.5.0.260927-kr-923868e_300059_arm64-v8a_release.apk) — SHA-256 `4dec086be7fbbeefdfe4439fbedbbf979686ff1819f7ca4b13ea9ca1b3b1c0d0`
