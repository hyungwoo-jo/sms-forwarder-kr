# SMS 자동전달 — 한국어판

문자·앱 알림·선택한 통화 알림을 사용자가 지정한 채널로 전달하는 한국어 전용 Android 앱입니다.
문자·앱 알림 규칙과 Telegram·Webhook·메일 전달을 유지합니다.
통화·연락처 이름·예약 작업·암호화 파일 백업을 지원합니다. 선택 기능은 기본 OFF입니다.
위치·블루투스·원격 제어와 FRP는 계속 비활성화합니다. [보안 패치](docs/SECURITY-PATCH.md)를 확인하세요.

## APK 다운로드

- [arm64-v8a APK — 일반적인 최신 Android 휴대폰](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/download/v3.5.0.260926-kr-019d53a/SmsKR_3.5.0.260926-kr-019d53a_300058_arm64-v8a_release.apk)
- [범용 APK — CPU 종류를 모를 때](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/download/v3.5.0.260926-kr-019d53a/SmsKR_3.5.0.260926-kr-019d53a_100058_universal_release.apk)
- [릴리스·해시·설치 안내](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/tag/v3.5.0.260926-kr-019d53a)

휴대폰에 설치합니다. 기존 개인판을 갱신할 때는 같은 ABI APK를 사용하십시오.
실제 휴대폰·워치 확인은 아직 남아 있으며 워치를 사용하지 않으면 워치 항목은 해당되지 않습니다.

## 빌드

JDK 11, Android SDK 33, Build Tools 33.0.1을 사용합니다.

1. [빌드 안내](docs/BUILD.md)를 따라 로컬 환경을 준비합니다.
2. `python3 scripts/init-signing.py`로 개인 서명을 준비합니다. 기존 키가 있으면 재사용합니다.
3. `bash scripts/build-local.sh`를 실행합니다.
4. 검증된 arm64-v8a 및 범용 APK는 `dist/`에 생성됩니다.

키와 서명 비밀번호는 Git에 저장하지 않습니다. 자동 외부 배포 워크플로는 없습니다.
기존 개인판과 같은 키·앱 ID를 사용합니다. 같은 ABI의 APK로 업데이트하십시오.

## 사용

[설치·설정 안내](docs/README-install-ko.md), [현재 상태](docs/STATUS.md),
[검증 기록](docs/VALIDATION.md), [통신 정책](docs/NETWORK-AUDIT.md)를 확인하십시오.

한국어는 시스템 언어와 무관하게 적용됩니다. 원본 언어 선택 메뉴는 한국어만 제공합니다.
기존 저장 규칙 및 백업과 호환하기 위한 내부 식별자는 유지합니다.
사용자가 입력한 메시지나 외부 서비스 응답의 언어를 자동 번역하지는 않습니다.

## 원본·저작권

원본: [pppscn/SmsForwarder](https://github.com/pppscn/SmsForwarder)
기준 커밋: `a3d23026f0058420869163c1d5dfb463ce52fc15`
한국어 개인판 이관 기준: `88de2eb5`

Copyright (c) 2021, pppscn. 원본 BSD-2-Clause의 저작권 표시·조건·면책문은
[LICENSE](LICENSE)에 그대로 보존합니다. APK에도 해당 원문을 포함합니다.
수정 내역은 [CHANGES-ko.md](docs/CHANGES-ko.md)에 기록합니다.
외부 라이브러리는 각자의 라이선스를 따릅니다.
