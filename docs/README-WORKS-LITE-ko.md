# 앱 알림 전달 Light

Light는 선택한 앱의 알림 제목·본문을 **Telegram Bot 또는 HTTPS Webhook**으로 직접 전달합니다. SMS·MMS·통화·연락처·자동 작업은 포함하지 않습니다. 원본 앱 알림을 삭제하지 않습니다.

versionCode 300066, targetSdk 36이며 `com.hwserve.smsforwarder`와 기존 서명을 유지합니다. 표준판 300065 위에 업데이트할 수 있지만, Android에서는 같은 ID의 Light와 표준판을 동시에 설치할 수 없습니다. 표준판으로 돌아갈 때는 300066보다 높은 버전 코드로 빌드한 APK가 필요합니다. 기존 앱 데이터는 같은 ID로 업데이트하는 동안 남지만 Light에서는 이전 DB를 수정하지 않습니다.

## 설정

1. 앱을 열고 알림 접근 버튼을 누르면 데이터 접근·전송 안내가 표시됩니다. 내용을 확인하고 계속을 누른 뒤 Android 설정에서 권한을 허용합니다. 이 권한은 다른 앱 알림의 내용도 읽을 수 있는 민감한 접근입니다. 실제 전달은 아래에 입력한 패키지에만 제한됩니다.
2. 전달할 앱의 패키지명을 입력합니다. 여러 개면 쉼표로 구분합니다. 목록 조회 권한은 없으며 최근 알림의 패키지명을 채우거나 직접 입력할 수 있습니다.
3. Telegram 직접 전송은 Bot 토큰과 채팅 ID를 입력합니다. 토픽을 쓰면 토픽 ID도 입력합니다. Webhook은 HTTPS 주소와 필요한 메서드·본문·헤더를 입력합니다. 두 경로를 각각 켜고 전체 전달 스위치를 저장합니다.
4. 이전 표준판에 활성 Telegram 또는 Webhook 발신자가 하나로 식별되면 해당 설정을 입력란에 가져옵니다. 자동으로 전달을 켜지는 않습니다. 기존 규칙과 일치하는 앱 패키지명을 확인하세요.
5. WORKS → Webhook → 서버 Telegram 경로를 쓰는 경우 **Webhook만 켜세요**. Telegram 직접 전송까지 켜면 두 통이 올 수 있습니다.

Webhook 기본 POST 본문은 `from=<앱 패키지>&content=<제목과 본문>`입니다. `Content-Type: text/plain` 헤더를 지정하면 제목과 본문만 보냅니다. 템플릿에서는 `[msg]`, `[content]`, `[org_content]`, `[title]`, `[from]`을 쓸 수 있습니다.

## 한계와 검증

- Light APK는 INTERNET·ACCESS_NETWORK_STATE 권한과 Notification Listener 서비스를 선언합니다. SMS·통화·연락처·전체 앱 조회·알림 삭제 코드는 포함하지 않습니다.
- Google은 브라우저·메신저·파일 관리자에서 설치한 앱의 알림 접근 서비스도 Play Protect 차단 대상으로 설명합니다. Light로 나눠도 차단 해제를 보장하지 않습니다.
- 네트워크 실패 또는 프로세스 종료 후 자동 재전송을 보장하지 않습니다. 두 전송 경로의 마지막 결과는 앱 화면에 표시합니다.
- 이전 Webhook의 HMAC secret, 프록시, PUT/PATCH와 Telegram 프록시는 지원하지 않습니다. 해당 설정은 자동으로 활성화되지 않습니다.
- 빌드·lint·권한·서명 검사는 수행하지만 실제 휴대폰 설치, WORKS 전달, Telegram·Webhook 수신, 토스 판정은 기기 검증이 필요합니다.

Play 내부 테스트용 AAB 준비와 설치 순서는 [안내문](PLAY-INTERNAL-TEST-LITE-ko.md)에 있습니다. [개인정보 처리방침](PRIVACY-LITE-ko.md)도 함께 확인하세요.
