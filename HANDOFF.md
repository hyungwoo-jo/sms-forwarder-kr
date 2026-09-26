# 다음 작업 인계

현재 저장소: https://github.com/hyungwoo-jo/sms-forwarder-kr
폴더: /home/hyungwoo/codespace/sms-forwarder-kr
브랜치: main
앱 소스: `e5570f590fdc235a0eb9045771651e7bec30d14b`. 빌드 시 clean.

## 완료

사용자 요청 보안 패치 완료: 문자·앱 알림·Webhook·Telegram 유지, 앱 선택 수정, 자동 외부 조회 제거, 위치/통화/블루투스/원격 제어/자동 작업 비활성화, FRP 네이티브 제외, 권한 축소, 외부 HTTP/리다이렉트 차단, 자동 백업/릴리스 자체 로그 중지, 새 아이콘.
12개 테스트 통과, debug/release lint 오류 0개, 서명·권한·리소스·LICENSE 검증 통과.
릴리스: https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/tag/v3.5.0.260926-kr-e5570f5

## 남은 작업

DEVICE_PENDING: 기기를 연결하거나 사용자가 설치 결과를 제공하면 기존 개인판 업데이트/규칙 유지, WORKS 앱 선택과 실제 알림·SMS 전달, 절전·재부팅, Play Protect 경고 유형을 확인한다. 워치를 쓰는 경우만 워치 전달을 확인한다. 기기 미연결을 완료로 표시하지 않는다.
새로운 기능을 임의로 시작하지 않는다. 서명 키/비밀번호를 출력하거나 커밋하지 않는다. 재빌드는 bash scripts/build-local.sh.
