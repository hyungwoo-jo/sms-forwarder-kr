# 현재 작업 상태

현재 저장소: https://github.com/hyungwoo-jo/sms-forwarder-kr  
작업 폴더: /home/hyungwoo/codespace/sms-forwarder-kr  
브랜치: main

| 단계 | 상태 | 근거 |
|---|---|---|
| 독립 저장소 이관 | DONE | 이전 개인판 88de2eb5를 독립 Git 기록으로 이관, 원본·기존 포크 보존 |
| 한국어 기본 리소스 | DONE | 앱 문자열 1,216개, 라이브러리 기본 문구 35개, 중국어 locale 제거 |
| 한국어 전용 UI | DONE | 한국어 locale 고정, 중국어/영어 선택 제거, Kotlin 직접 출력 한국어화 |
| 원본 저작권·호환성 | DONE | BSD-2-Clause 보존 및 APK assets 포함, canonical 태그 30개 유지 |
| 새 저장소 debug/단위 테스트/lint | IN_PROGRESS | 이전 포크의 결과를 새 빌드 성공으로 간주하지 않음 |
| 새 저장소 release/서명/리소스 검증 | TODO | 동일 앱 ID·개인 서명, versionCode 56 |
| GitHub 소스·설치 안내 인계 | IN_PROGRESS | main 기준으로 경로 갱신 |
| 휴대폰·Galaxy Watch 검증 | DEVICE_PENDING | 연결 기기 없음 |
| 선택 CI | NOT RUN | 로컬 서명·빌드 사용, 자동 배포 없음 |

저장된 옛 중국어 템플릿·이메일 설정과의 호환 별칭은 내부에서 유지한다.
원본 주석·외부 라이브러리 바이너리·사용자 메시지는 화면 번역의 검사 대상과 구분한다.
실제 기기에서 화면 배치·외부 라이브러리 동적 오류·알림 전달을 확인해야 한다.
