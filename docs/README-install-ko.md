# 개인판 설치와 갱신

`dist/`에는 개인 키로 서명한 arm64-v8a와 universal 릴리스 APK가 있습니다. 일반적인 최신 Android 휴대폰에서는 arm64-v8a APK를 사용합니다. CPU가 불확실하면 universal을 선택하십시오. 같은 기기에 갱신할 때는 계속 같은 ABI 종류를 사용하십시오.

1. APK를 휴대폰으로 복사하고 파일 관리자에서 열어 설치합니다. 필요한 경우 해당 파일 관리자의 알 수 없는 앱 설치를 허용합니다.
2. 이 개인판 ID는 `com.hwserve.smsforwarder`이며 원본 앱과 병행 설치할 수 있습니다. 기존 개인판을 갱신할 때는 삭제하지 않고 같은 개인 키의 새 APK를 설치합니다.
3. 앱 설정에서 한국어를 선택합니다. 전달할 기능에 해당하는 권한만 허용합니다. 앱 알림 전달에는 Android 설정의 알림 접근 권한이 필요합니다.
4. 발신자를 만든 뒤 규칙을 연결합니다. Telegram은 본인의 Bot 토큰과 채팅 ID, Webhook은 본인의 대상 URL과 필요한 헤더를 입력합니다. ntfy 등 plain text 목적지는 `Content-Type: text/plain; charset=utf-8`과 본문 `[msg]`를 사용할 수 있습니다.
5. 자동 실행과 배터리 제한 설정은 휴대폰 제조사의 Android 설정에서 확인합니다. 초기 설정 전에는 발신 채널을 자동 활성화하지 않습니다.

## 직접 빌드

JDK 11, Android API 33와 Build Tools 33.0.1을 준비하고 `scripts/env.local.sh.example`을 참고해 로컬 환경을 설정합니다.

```bash
source scripts/env.local.sh
python3 scripts/init-signing.py
bash scripts/build-local.sh
```

개인 키는 작업공간의 `.smsforwarder-local/signing/`, 서명 설정은 저장소의 gitignore 된 `signing.properties`에 있습니다. 이 파일들은 GitHub에 올리지 마십시오. 키와 암호를 별도로 안전하게 백업해야 기존 설치를 갱신할 수 있습니다. 키를 잃으면 기존 설치에 동일한 앱 ID로 업데이트할 수 없습니다.

ABI별 versionCode는 원본의 `ABI 코드 × 100000 + 기본 코드` 방식을 유지합니다. universal은 100055, arm64-v8a는 300055 계열입니다. arm64에서 universal로 바꾸면 versionCode가 낮아질 수 있어 설치가 거절될 수 있습니다. 향후 갱신 시 기본 versionCode를 증가시키십시오.

## 확인 범위

서버에서 빌드·서명·자동 테스트를 확인했습니다. 휴대폰과 Galaxy Watch의 실제 알림 전달, 재부팅·절전 동작은 기기 연결 후 확인해야 합니다. 검증 결과는 `docs/VALIDATION.md`에 기록합니다. 날짜 기반 버전과 서명 때문에 APK의 바이트 단위 재현성을 보장하지 않습니다.
