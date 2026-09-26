# 다음 작업 인계

## 현재 작업 위치

- 저장소: https://github.com/hyungwoo-jo/sms-forwarder-kr (비공개)
- 폴더: /home/hyungwoo/codespace/sms-forwarder-kr
- 브랜치: main
- 기존 smsforwarder-ko 폴더/포크는 이전 기록이며 다음 변경을 거기서 시작하지 않는다.
- 이관 및 서버 빌드·서명·자동 검증은 완료됐다. 새로운 기능을 임의로 추가하지 않는다.

## 확인한 결과

- 앱 소스 1581e5abead018cde410bba6b974176927b1d035에서 clean build.
- JVM 테스트 10개, debug/release lint 오류 0개(경고 각 268개).
- arm64/universal 개인 서명 APK와 LICENSE·해시·리소스·서명 보고서는 dist/.
- 중국어 화면 리소스와 직접 코드 출력은 한국어로 변경했다.
- 저장 설정의 옛 템플릿/OTP 호환 입력·원본 개발자 주석은 보존했다.
- 개인 키·암호는 기존 .smsforwarder-local/signing 및 무시된 signing.properties를 사용한다. 출력하거나 커밋하지 않는다.

## 실제 남은 작업

DEVICE_PENDING: 사용자가 기기 연결 및 설치를 요청하면 다음을 검증한다.

1. 동일 ABI APK로 기존 개인판 업데이트 및 규칙·채널 유지.
2. 메인·설정·약관·권한·FRP 화면의 한국어 표시와 배치.
3. 알림 접근 권한 설정 후 NAVER WORKS A/B 알림과 Telegram/Webhook 전달.
4. 화면 OFF·재부팅·절전·네트워크 복구.
5. Galaxy Watch 알림 도착과 누적.

기기 미연결 상태를 완료로 표시하지 않는다. 실제 업무 메시지·토큰을 테스트 로그에 남기지 않는다.
검증과 설치 안내는 docs/VALIDATION.md 및 docs/README-install-ko.md에 있다.
필요한 재빌드는 bash scripts/build-local.sh. 자동 CI·공개 release 발행은 현재 범위에 없다.
