# Security Cleanup Log (2단계 작성, 3단계에서 build 검증용 임시 파일 기록 추가)

실제 credential/key 값은 이 문서에 기록하지 않는다. 위치와 조치 내용만 기록한다.

## google-services.json
- 원본 위치: `MediCheck_ver2.5.zip` 내부 `app/google-services.json`
- 내용: 실제 Firebase project_id / project_number / firebase_url / **실제 API key** / storage_bucket 포함
- 조치: **공개본(pill-care)에 복사하지 않음**
- 대체: `app/google-services.example.json` 신규 생성 — 모든 값을 `YOUR_PROJECT_ID`, `YOUR_PROJECT_NUMBER`, `YOUR_APP_ID`, `YOUR_API_KEY`, `YOUR_DATABASE_URL` 형태의 명백한 placeholder로 대체
- 참고: 실제 앱 실행 시에는 사용자가 자신의 Firebase 콘솔에서 새 프로젝트를 만들고 `google-services.json`을 직접 발급받아 `app/` 아래에 배치해야 함 (다음 단계에서 README/설정 가이드로 문서화 예정)

## local.properties
- 원본 위치: `MediCheck_ver2.5.zip` 내부 `local.properties`
- 내용: `sdk.dir=C:\AndroidStudio` (로컬 Android SDK 경로)
- 조치: **공개본에 복사하지 않음** — Android Studio가 최초 빌드 시 자동 재생성하는 파일이므로 복원 불필요

## AlarmList.java — Firebase Realtime Database URL 하드코딩
- 원본 위치: `app/src/main/java/org/techtown/medicheck/AlarmList.java` (zip 기준 222번째 줄)
- 원본 코드: `FirebaseDatabase database = FirebaseDatabase.getInstance("<실제 Firebase RTDB URL — 이 문서에는 값 비공개>");`
- 조치: 공개본에서만 URL 문자열을 `"YOUR_FIREBASE_DATABASE_URL"`로 최소 치환
- 변경 범위: **이 한 줄, 문자열 리터럴 값만** — 클래스 구조/메서드/로직/포맷은 원본과 완전히 동일
- 원칙 준수 확인: 기능 로직 변경 없음, 클래스 구조 변경 없음, 리팩터링 없음
- 분류: [실제 2025 원본 기반 공개용 보안 정리] (더 이상 byte-identical 원본 아님)

## 빌드/IDE 캐시 (.gradle, .idea, app/build 등)
- 조치: **공개본에 복사하지 않음** (636개 항목) — Android Studio/Gradle이 재생성 가능

## 다음 단계(3단계 이후)에서 만들 최종 `.gitignore` 후보 (이번 단계에서는 파일 생성하지 않음, 기록만)
```
app/google-services.json
local.properties
.gradle/
.idea/
**/build/
*.iml
captures/
.externalNativeBuild/
```

## 이번 단계에서 확인된 Firebase 사용처 전수조사 결과 (docs/version-history.md, 본문 16번 항목과 연동)
- 실제 Firebase API 호출: `AlarmList.java` 1개 파일뿐 (READ 전용, "drugInteractions" 노드 1회 조회)
- `LoadExcelActivity.java`: Firebase import만 존재, 실제 호출 코드 없음 (미사용 import)
- 그 외 파일의 "getInstance" 검색 결과는 전부 `Calendar.getInstance()`로, Firebase와 무관함을 확인(오탐 배제)

---

## 3단계: build 검증용 임시 파일 (생성 후 삭제 완료)

### temporary `app/google-services.json`
- 목적: `com.google.gms.google-services` Gradle 플러그인이 build 구성(configuration) 단계에서 이 파일의 존재를 요구하므로, **build 통과 여부만 확인**하기 위해 임시 생성
- 값: **과거/실제 Firebase 값을 전혀 사용하지 않음.** `google-services.example.json`과 동일한 형태의 명백한 dummy 값만 사용(`project_id: "dummy-build-only"`, `current_key: "DUMMY_BUILD_ONLY_KEY_NOT_REAL"` 등)
- 결과: `processDebugGoogleServices` task 정상 완료 — 이 파일 없이는 build 자체가 configuration 단계에서 실패함을 확인
- 처리: **build 완료 직후 삭제** — 최종 공개 후보(pill-care)에는 포함되지 않음, `app/google-services.example.json`만 남음

### temporary `local.properties`
- 목적: Gradle이 로컬 Android SDK 위치를 찾기 위해 필요(이 세션에는 `ANDROID_HOME`/`ANDROID_SDK_ROOT` 환경변수가 설정되어 있지 않았음)
- 값: 이 머신에 **이미 설치되어 있던** SDK 경로(`C:\Users\user\AppData\Local\Android\Sdk`)만 사용 — 새 SDK 설치 없음
- 처리: **build 완료 직후 삭제** — 최종 공개 후보에는 포함되지 않음

### temporary `gradle.properties` 수정 (경로 검사 우회)
- 목적: 이번 검증 작업 폴더 경로에 한글이 포함되어 있어 AGP가 `Your project path contains non-ASCII characters` 오류를 발생시킴(원본 코드 결함이 아니라 로컬 검증 환경 경로 문제)
- 조치: `android.overridePathCheck=true` 한 줄을 build 시도 중에만 추가 → **build 완료 직후 원본과 완전히 동일한 내용으로 복구**, SHA256 재검증 완료(`41a9783e6d8fba6d52cf787adab4b02b9d0593169e30590c51aa9be2eebe8182` — 원본과 일치)
- 공개 후보의 `gradle.properties`는 이 임시 조치의 흔적이 전혀 남아있지 않음(byte-identical 유지)

### build 산출물 정리
- `.gradle/`, `app/build/`, `app-debug.apk` 등 build 산출물 전량 삭제 완료(상세: `docs/build-verification.md`)
