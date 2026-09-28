# 현재 상태

저장소: https://github.com/hyungwoo-jo/sms-forwarder-kr
앱 소스: `7361b767e323fcfade64409a107d8322204f1196` (versionCode 300061).

기능 복원·UI 개선 구현과 빌드·17개 테스트·서명/권한/리소스/라이선스 검증 완료.
통화 전달·기기 연락처 이름·예약/메시지 자동 작업과 암호화 파일 백업/복원 지원.
전달 상태 요약·남색/청록 테마·카드/탭 정리·앱 검색 선택 적용.
원격 서버/FRP/위치/블루투스/SMS 원격 명령은 비활성화 유지.

릴리스: https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/tag/v3.5.0.260927-kr-7361b76
DEVICE_PENDING: 실제 UI·문자/WORKS/통화/연락처·예약·파일 백업/복원·절전/재부팅·Play Protect.
상세: [VALIDATION.md](VALIDATION.md), [SECURITY-PATCH.md](SECURITY-PATCH.md).

2026-09-27: 사용자 제보 중국어 상대 시간 표시 수정, 채널 비활성 오류 안내 개선. 이어서 앱 소스·주석과 빌드 설정의 실제 중국어 문자를 제거하고 외부 중국어 오류의 로그·공통 토스트 표시를 한국어 안내로 대체했다. ABI별 versionCode 차이로 universal 업데이트가 거부되던 문제를 공통 versionCode 300061로 수정했다. 실제 기기 검증은 남음.

2026-09-28: WORKS → HTTPS Webhook 전용 `:lite` APK(versionCode 300062)를 추가했다. 기존 앱 ID와 서명을 유지하면서 APK 권한을 INTERNET/ACCESS_NETWORK_STATE로 줄였다. 기존 Webhook/앱 규칙은 하나로 식별될 때만 비활성 상태로 이관한다. Webhook 성공 후 WORKS 원본 알림을 지우는 옵션은 기본값을 껐다. 빌드·릴리스 lint·APK 권한/서명/DEX 검사 완료. 실제 설치 업데이트, WORKS 알림 형식, 서버 수신, 워치, 토스 실행은 기기 검증 대기. 안내: [README-WORKS-LITE-ko.md](README-WORKS-LITE-ko.md).
