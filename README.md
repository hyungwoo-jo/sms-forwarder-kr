# SMS 자동전달 한국어판

[SmsForwarder](https://github.com/pppscn/SmsForwarder)를 바탕으로 만든 한국어 Android 앱입니다.

## 버전

- [표준판](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/tag/v3.5.0.260929-standard-50c4cf4): 문자, 앱 알림, 통화 알림 등을 규칙에 따라 전달합니다.
- [알림 릴레이](https://github.com/hyungwoo-jo/sms-forwarder-kr/releases/tag/v1.0.0-notifyrelay-identity-test): 선택한 앱의 알림 제목과 본문을 Telegram 또는 HTTPS Webhook으로 전달합니다. 독립 앱 ID를 사용합니다.

## 사용 방법

1. 해당 버전의 APK를 설치하고 앱을 엽니다.
2. 앱 알림을 전달하려면 Android 설정에서 알림 접근을 허용하고, 전달할 앱을 선택하거나 패키지명을 입력합니다.
3. Telegram 봇 토큰·채팅 ID 또는 HTTPS Webhook 주소를 설정한 뒤 전달을 켭니다. 서버가 Telegram으로 다시 보내는 Webhook이라면 직접 Telegram 전송은 꺼 두어 중복을 피합니다.

표준판의 문자 전달에는 기기의 문자 권한과 별도 규칙 설정이 필요합니다. 알림 릴레이는 문자 전송 기능이 없습니다. 자세한 설정은 [설치 안내](docs/README-install-ko.md)를 참고하세요.

## 빌드와 라이선스

빌드는 [빌드 안내](docs/BUILD.md)를 참고하세요. 개인 서명키와 전달 대상의 비밀값은 저장소에 포함하지 않습니다.

원본 저작권은 Copyright (c) 2021, pppscn입니다. 원본의 BSD-2-Clause 저작권 표시, 조건, 면책 조항은 [LICENSE](LICENSE)에 보존했습니다. 외부 라이브러리는 각 라이선스를 따릅니다.
