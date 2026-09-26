# 한국어 전용 저장소 이관

- 현재 작업 저장소: https://github.com/hyungwoo-jo/sms-forwarder-kr
- 로컬 폴더: /home/hyungwoo/codespace/sms-forwarder-kr
- 기본 브랜치: main
- 이관 기준 개인판: 88de2eb5
- 원본과 기존 개인 포크는 보존한다. 이후 변경 및 빌드는 이 저장소에서 수행한다.
- 이 저장소는 독립 Git 기록으로 시작하며 원본 출처와 BSD-2-Clause LICENSE를 보존한다.
- 개인 키는 기존 보호된 경로에서 재사용하며 Git에 넣지 않는다.
- applicationId com.hwserve.smsforwarder, versionCode 기준 56으로 갱신한다.
- 기본 리소스 및 앱 locale을 한국어로 고정한다. 중국어/영어 locale 및 선택을 제거한다.
- 원본 자동 CI·후원/이슈 템플릿·중국어 README·스크린샷은 이관하지 않는다.
- 저장 DB의 이전 버전 migration 및 중국어 템플릿 별칭은 데이터 호환 목적으로 유지한다.
- 기기에서 발생한 외부 서비스 응답·연락처·메시지는 입력 그대로 처리한다.
- 휴대폰·워치 통합 검증은 연결 기기가 없어 DEVICE_PENDING이다.
