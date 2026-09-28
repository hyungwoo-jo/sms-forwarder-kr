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

2026-09-29: 사용자가 경량판의 기능 축소가 지나치다고 지적했다. 후속 업데이트 300063은 전체판의 문자·통화·앱 알림, Telegram·Webhook 및 다른 전송 채널, 자동 작업과 암호화 백업을 복원한다. 사용하지 않는 READ_SMS와 광범위한 앱 조회 선언만 제거한다. 300062 경량판은 더 이상 권장하지 않는다. 토스의 실제 판정은 기기 검증 대기.

2026-09-29: 사용자가 300063의 Play Protect 차단을 보고했다. Light 300064에는 앱 알림 → Telegram·Webhook 직접 전송을 넣고 원본 알림 삭제를 제거했다. 표준판은 전체 기능을 유지한 300065로 올려 Light에서 복귀 가능하게 한다. Google의 공식 안내에 따르면 사이드로드 앱의 Notification Listener 또는 RECEIVE_SMS 자체가 차단 사유일 수 있으므로 두 APK 모두 차단 해제를 보장하지 않는다. 정확한 경고 문구와 설치 경로를 기다리는 중이다.
