# 한국어 전용판 검증

이 문서는 sms-forwarder-kr의 빌드 결과를 기록한다. 이전 포크의 성공을 새 저장소의 성공으로 표시하지 않는다.

- 기본 앱 리소스 1,216개, 중국어 표시 리소스 없음: PASS
- 라이브러리 기본 중국어 문구 35개 한국어 override: 소스 준비 완료
- canonical 템플릿 30개 유지, Umeng/자동 업데이트/인증서 우회 정적 검사: PASS
- 새 저장소 단위 테스트·debug/release lint: PENDING
- 새 저장소 arm64/universal 서명 APK: PENDING
- 실제 휴대폰·워치: DEVICE_PENDING
- 중국어 호환 입력 별칭은 DB migration·OTP 입력 처리에만 남긴다.
- 외부 서버 오류·수신 메시지·사용자 저장 설정은 입력 그대로이므로 자동 번역을 보장하지 않는다.
- Android 19~23 실기기 및 전체 네이티브 코드 감사는 수행하지 않았다.
