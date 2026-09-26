# 로컬 빌드

작업 저장소: https://github.com/hyungwoo-jo/sms-forwarder-kr  
작업 폴더: /home/hyungwoo/codespace/sms-forwarder-kr

## 도구

- JDK 11 (현재 로컬: Temurin 11.0.32.1)
- Android SDK 33 / Build Tools 33.0.1
- Gradle wrapper 7.3.3 / Android Gradle Plugin 7.2.2 / Kotlin 1.7.21

## 환경 준비

로컬 SDK 경로를 gitignore된 `local.properties`의 `sdk.dir`에 적는다.
`scripts/env.local.sh`에 다음 환경변수를 실제 설치 경로로 지정한다.

```bash
export JAVA_HOME=/path/to/jdk-11
export ANDROID_HOME=/path/to/android-sdk
export GRADLE_USER_HOME=/path/to/gradle-home
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
```

현재 서버는 `/home/hyungwoo/codespace/.smsforwarder-local/`의 기존 도구와 키를 재사용한다.
개인 서명 설정은 gitignore된 `signing.properties`에만 있다. 키·암호를 Git에 올리지 않는다.
키가 없는 새로운 환경에서만 `python3 scripts/init-signing.py`로 생성한다.
기존 설치를 갱신하려면 기존 키를 재사용해야 한다.

## 실행

```bash
bash scripts/build-local.sh
```

한국어/개인정보 정적 검사 → debug 빌드·JVM 테스트·lint → release 리소스 재생성 →
release 빌드·lint → 서명·패키지·ABI·DEX·한국어 리소스 검사 → `dist/` 수집 순서다.

release applicationId는 `com.hwserve.smsforwarder`, debug는 `.debug`를 추가한다.
versionCode 기본값 56, universal 100056, arm64-v8a 300056이다.
ABI별 versionCode가 다르므로 같은 ABI APK로 갱신한다.
리소스 병합 캐시를 다시 생성하며 전체 clean은 기본으로 실행하지 않는다.
기기 설치·실제 채널 발송·공개 release 발행은 이 스크립트에 포함하지 않는다.

`dist/`의 APK 2종·해시·BUILD-INFO·서명 검사·설치 안내·LICENSE를 함께 보관한다.
날짜와 빌드 시간이 포함되므로 APK의 바이트 단위 재현성은 보장하지 않는다.
원본 legacy mail/provider META-INF로 인한 v1 경고가 있을 수 있으며 v2 서명 검증도 수행한다.
실제 결과는 [VALIDATION.md](VALIDATION.md)에 기록한다.

## 의존성

Google Maven, Maven Central, JitPack과 고정 legacy artifact용 Huawei Maven mirror를 사용한다.
빌드용 다운로드 저장소이며 앱의 자동 런타임 전송 대상이 아니다.
기존 고정 의존성을 유지하고 외부 라이브러리는 각 라이선스를 따른다.
