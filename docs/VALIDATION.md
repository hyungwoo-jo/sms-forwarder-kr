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

## 2026-09-27 중국어 소스 제거 및 표시 보호 검증

기본 versionCode 60.

- 앱 Kotlin/Java/XML/JSON/TXT 소스와 테스트 소스의 실제 한자 범위 문자를 검사해 0개를 확인했다. 주석도 포함한다.
- 구형 설정 이관, 구형 이메일 종류값, Bark OTP 인식에 필요한 역사적 중국어 값은 Unicode 이스케이프로 보존해 기존 설정 호환성을 유지한다.
- 외부 서비스나 라이브러리가 한국어가 아닌 중국어 오류를 돌려주면, 전달 로그와 공통 오류 토스트는 한국어 점검 안내로 대체한다. 해당 동작의 단위 테스트를 추가했다.
- JVM 테스트 18개, 실패·오류 0개. debug lint 오류 0개, 경고 285개.
- 이 검사는 이 저장소의 앱 소스를 대상으로 한다. 서드파티 AAR/DEX 내부 문자열까지 중국어 0개임을 의미하지는 않는다.

릴리스 소스: `03dbfa2600dfe2282139e81bb9ec5a6144357162`.

- [universal APK](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/download/v3.5.0.260927-kr-03dbfa2/SmsKR_3.5.0.260927-kr-03dbfa2_100060_universal_release.apk) — SHA-256 `e269d711c1567457330040c2db71203f368ad720b5d8743ff3113103e5e6cb44`
- [arm64-v8a APK](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/download/v3.5.0.260927-kr-03dbfa2/SmsKR_3.5.0.260927-kr-03dbfa2_300060_arm64-v8a_release.apk) — SHA-256 `58c7898a084418e399f3a88d45fcc9182afe531ef3901bcba90607ddfbc625e5`

## 2026-09-27 ABI 업데이트 호환 수정 검증

릴리스 소스: `7361b767e323fcfade64409a107d8322204f1196`.

- universal과 arm64-v8a APK 모두 versionCode `300061`을 사용한다. 이전 universal `100060` 및 arm64 `300060`보다 높다.
- 두 APK의 versionCode가 다르면 수집 검사가 실패하도록 했다.
- 저장소 앱·빌드 설정의 실제 중국어 문자는 0개이며, 검사 범위에 Gradle·ProGuard·채널 설정 파일을 추가했다.
- JVM 테스트 18개, 실패·오류 0개. debug/release lint 오류 0개.

- [universal APK](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/download/v3.5.0.260927-kr-7361b76/SmsKR_3.5.0.260927-kr-7361b76_300061_universal_release.apk) — SHA-256 `b20045729db67ee43154c7458c4d7ef5bc661d8735d2adfb977567405ce9685d`
- [arm64-v8a APK](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/download/v3.5.0.260927-kr-7361b76/SmsKR_3.5.0.260927-kr-7361b76_300061_arm64-v8a_release.apk) — SHA-256 `cdef27a05c1c5885ced891562da7b3452721d89c063d7bd3f5815ddadc3360b0`
