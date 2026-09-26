# 한국어 전용판 검증 결과

검증 대상: https://github.com/hyungwoo-jo/sms-forwarder-kr
소스 커밋: `1581e5abead018cde410bba6b974176927b1d035`
빌드 시 Git working tree: clean

| 항목 | 결과 | 근거 |
|---|---|---|
| 기본 앱 문자열 | PASS | 1,216개 키, 중국어 표시 리소스 0개 |
| 라이브러리 기본 UI | PASS | 중국어 기본 문구 35개 한국어 override |
| 중국어 locale 및 언어 선택 제거 | PASS | 한국어 기본 리소스·locale 고정, ko 이외의 locale 제외 |
| canonical 태그 및 개인정보 정적 검사 | PASS | 원본 템플릿 30개 유지, trust-all/Umeng/업데이트 제거·조회 OFF 검사 |
| JVM 테스트 | PASS | 10개 실행, 실패·오류 0개 |
| debug 및 release 빌드 | PASS | scripts/build-local.sh exit 0 |
| debug 및 release lint | PASS | 각각 오류 0개, 경고 268개 |
| APK 리소스 | PASS | aapt의 전체 resource table에 중국어 없음, 각 ABI resources.txt |
| 개인 서명 및 앱 ID | PASS | v1/v2 검증, com.hwserve.smsforwarder, 기존 개인판과 동일 인증서 |
| 저작권 원문 | PASS | APK assets/LICENSE.txt가 저장소 LICENSE와 바이트 단위 동일 |
| APK 구성 | PASS | release non-debuggable, DEX 내 com/umeng/ 없음, ABI별 FRPC native library 포함 |
| 실제 휴대폰·Galaxy Watch | DEVICE_PENDING | 설치·화면·절전·알림 전달은 연결 기기에서 확인 필요 |
| 선택 CI | NOT RUN | 로컬 빌드·서명, 자동 배포 없음 |

## 릴리스

- universal: `dist/SmsKR_3.5.0.260926-kr-1581e5a_100056_universal_release.apk`
  - versionCode: 100056
  - SHA-256: `41fe110de97ddb112bd0430edea9246fa0ad2f5183803b5a2a36b0c1637146af`
- arm64-v8a: `dist/SmsKR_3.5.0.260926-kr-1581e5a_300056_arm64-v8a_release.apk`
  - versionCode: 300056
  - SHA-256: `9cfb04d6d516b24f66d4d5d36c280f4c80234f209e9add664778f25cf744654b`

동일 signing certificate SHA-256: `e476e7b37e0119f0b01edd874c3cf3bc88547ec7d322988cfdf5d9813b90e710`

## 검증 범위

- 전송·TLS 테스트는 실제 봇·알림 대신 로컬 모의 서버를 사용했다. 기기의 Android callback 전체 검증은 별도다.
- 예전 중국어 템플릿 별칭·이메일 저장값·OTP 입력 규칙은 내부 호환 처리를 위해 보존한다.
- 원본 코드의 개발자 주석과 외부 라이브러리 바이너리를 전부 번역한 것은 아니다.
- 외부 서비스 오류·사용자 설정·수신 메시지 언어는 입력 그대로이며 자동 번역을 보장하지 않는다.
- 실제 기기에서 UI 문구의 자연스러움·배치·동적 오류 메시지·워치 알림을 추가 검토해야 한다.
- 기존 mail/provider META-INF 항목에 v1 서명 경고가 남는다. v2 검증은 성공했다. Android 19~23 실기기 검증은 수행하지 않았다.
- 전체 Kotlin·native 코드 보안 감사와 모든 기능의 무통신 보장을 의미하지 않는다.
- 날짜·빌드 시간으로 APK 바이트 단위 재현성을 보장하지 않는다.
