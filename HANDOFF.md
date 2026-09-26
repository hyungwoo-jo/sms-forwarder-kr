# 다음 작업 인계

현재 저장소: https://github.com/hyungwoo-jo/sms-forwarder-kr
폴더: /home/hyungwoo/codespace/sms-forwarder-kr
브랜치: main
앱 소스: `019d53ab58ff458783fcf9ae84e612c164a5bf72`. 빌드 시 clean.
릴리스: https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/tag/v3.5.0.260926-kr-019d53a

## 완료

사용자 요청 기능 복원·UI 개선·새 APK 배포 완료.
통화·연락처·예약/메시지 자동 작업과 암호화 파일 백업을 선택 기능으로 지원.
전달 상태 요약·색/카드/하단 탭·앱 검색 선택 UI 적용.
문자/앱/Webhook/Telegram 유지, 외부 조회 제거와 HTTPS 정책 유지.
민감한 원격 제어·FRP·위치·블루투스는 비활성화 유지.
14개 테스트 통과, debug/release lint 오류 0개, APK 서명·권한·리소스·LICENSE 검증 통과.

## 남은 작업

DEVICE_PENDING: 기기 연결/사용자 설치 결과를 받아 UI 배치·실제 문자/WORKS/통화/연락처·예약 실행·파일 선택기와 DB 백업/복원·재부팅/절전·Play Protect를 검증한다. 워치 사용 시 워치 알림도 확인한다.
기기 미연결을 완료로 표시하지 않는다. 새로운 기능을 임의로 시작하지 않는다.
서명 키/암호를 출력하거나 커밋하지 않는다. 재빌드: bash scripts/build-local.sh.
