# 다음 작업 인계

현재 저장소: https://github.com/hyungwoo-jo/sms-forwarder-kr
폴더: /home/hyungwoo/codespace/sms-forwarder-kr
브랜치: main
앱 소스: `923868e82d439e3eba7845f6708abcc3537861a0`. 빌드 시 clean.
릴리스: https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/tag/v3.5.0.260927-kr-923868e

## 완료

사용자 요청 기능 복원·UI 개선·새 APK 배포 완료.
통화·연락처·예약/메시지 자동 작업과 암호화 파일 백업을 선택 기능으로 지원.
전달 상태 요약·색/카드/하단 탭·앱 검색 선택 UI 적용.
문자/앱/Webhook/Telegram 유지, 외부 조회 제거와 HTTPS 정책 유지.
민감한 원격 제어·FRP·위치·블루투스는 비활성화 유지.
17개 테스트 통과, debug/release lint 오류 0개, APK 서명·권한·리소스·LICENSE 검증 통과.

## 남은 작업

DEVICE_PENDING: 기기 연결/사용자 설치 결과를 받아 UI 배치·실제 문자/WORKS/통화/연락처·예약 실행·파일 선택기와 DB 백업/복원·재부팅/절전·Play Protect를 검증한다. 워치 사용 시 워치 알림도 확인한다.
기기 미연결을 완료로 표시하지 않는다. 새로운 기능을 임의로 시작하지 않는다.
서명 키/암호를 출력하거나 커밋하지 않는다. 재빌드: bash scripts/build-local.sh.

## 2026-09-27 사용자 수정 반영

로그에서 중국어 상대 시간이 보인다는 제보를 수정함. 로그·작업·문자 조회·통화 조회 4곳을 한국어 포맷터로 교체. APK DEX의 중국어 분/초/오늘/어제/방금 문자열이 없어짐을 확인함. 기본 versionCode 59.
비활성 채널 오류는 규칙에 연결된 Sender.status != 1에서 HTTP 요청 전 발생함. Telegram 채널의 테스트 버튼은 사용 스위치를 확인하지 않으므로 테스트 성공과 규칙 활성 상태는 다를 수 있음. 해결 안내를 한국어 오류 문구에 추가했으며 사용 설정을 자동 변경하지 않음.

## 2026-09-27 중국어 소스 제거 릴리스 준비

- versionCode 60으로 앱 Kotlin/Java/XML/JSON/TXT 소스와 테스트 소스의 실제 중국어 문자를 0개로 만들었다. 원본 중국어 주석은 제거했다.
- 구형 데이터 이관·구형 이메일 유형·Bark OTP 정규식의 중국어 값은 Unicode 이스케이프로 유지해 기존 설정의 동작을 보존한다.
- 외부 서비스나 라이브러리 오류가 중국어를 포함하면 전달 로그와 공통 오류 토스트는 한국어 점검 안내를 표시한다.
- 릴리스 소스 `03dbfa2600dfe2282139e81bb9ec5a6144357162`: JVM 테스트 18개 성공, debug/release lint 오류 0, 서명 APK 생성·검사 완료. GitHub 릴리스 발행 후 실제 기기 설치·WORKS 전달 검증은 DEVICE_PENDING으로 남긴다.
