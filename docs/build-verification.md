# Build Verification Report (3단계)

이번 검증은 **compile/resource/build 검증**만을 목적으로 하며, Firebase runtime 연동 검증이 아닙니다.
실제 Firebase 네트워크 접속은 이번 단계에서 전혀 수행하지 않았습니다.

## 빌드 환경 (기존 설치된 환경만 사용, 신규 설치 없음)
- OS: Windows
- Java: `C:\Program Files\Java\jdk-21` (OpenJDK 21, 이미 설치되어 있던 JDK — 신규 설치 없음)
  - 참고: 이 세션의 기본 `JAVA_HOME`은 JDK 26.0.2였으나, Gradle 8.7에 내장된 Kotlin 스크립트 컴파일러가 "26.0.2" 버전 문자열을 파싱하지 못해 사용 불가(아래 최초 build 실패 참고) → 이미 설치되어 있던 JDK 21로 전환
- Android SDK: `C:\Users\user\AppData\Local\Android\Sdk` (기존 설치, 신규 설치 없음)
  - platforms: android-34, android-35
  - build-tools: 34.0.0, 35.0.0
- Gradle Wrapper: **8.7** (원본 그대로 유지, 변경 없음)
- Build 명령: `gradlew.bat assembleDebug --stacktrace` (원본 Gradle Wrapper 사용, 시스템 Gradle 미사용)

## 최초 build 시도 (원본 AGP 설정 그대로)
**결과: 실패**
- 원인 분류: **Java** (Gradle/AGP 설정과 무관) — 시스템 기본 JDK 26.0.2를 Gradle 8.7의 Kotlin DSL 스크립트 컴파일러가 인식하지 못함 (`IllegalArgumentException: 26.0.2`, `JavaVersion.parse` 실패)
- AGP 버전 불일치(8.1.1 vs 8.6.0)와는 무관한 순수 Java 툴체인 호환성 문제
- 조치: 새 JDK 설치 없이, 이미 설치되어 있던 JDK 21로 `JAVA_HOME` 전환 후 재시도

## 2차 build 시도 (JDK 21로 전환)
**결과: 실패**
- 원인 분류: **AGP/Gradle** — `Your project path contains non-ASCII characters` 오류. 작업 폴더 경로(`...\학기설계 프로젝트 파일\3학년 설계 1학기\pill-care`)에 한글이 포함되어 AGP의 Windows 경로 검사에 걸림
- 이는 **원본 코드의 결함이 아니라 이번 검증을 수행 중인 로컬 디스크 경로(한글 폴더명)에서만 발생하는 환경적 제약**. GitHub에 올라가 일반적인(영문) 경로에 clone되면 재발하지 않음
- AGP 버전 불일치와는 무관

## 3차 build 시도 (경로 검사 임시 우회)
**결과: 성공**
- 조치: `gradle.properties`에 `android.overridePathCheck=true`를 **빌드 검증 중에만 임시로 추가** → 빌드 완료 직후 원본과 SHA256이 완전히 동일하도록 즉시 복구(검증 완료, 아래 24번 항목 참고)
- 이 설정은 **공개 후보(pill-care)에 영구 반영되지 않음** — 로컬 한글 경로에서만 필요한 임시 우회

## AGP 버전 충돌 여부 분석
- 원본에는 두 가지 AGP 선언이 공존:
  1. 루트 `build.gradle.kts`의 `buildscript { dependencies { classpath("com.android.tools.build:gradle:8.1.1") } }` (구버전 방식)
  2. `gradle/libs.versions.toml`의 `agp = "8.6.0"` 버전 카탈로그 항목 (신버전 방식, `alias(libs.plugins.android.application) apply false`로 루트에 선언은 되어 있으나 **app 모듈은 `id("com.android.application")`을 버전 없이 참조**하므로 실제로는 카탈로그 alias가 아니라 buildscript classpath의 8.1.1이 적용됨)
- **결론: 실제 build는 AGP 8.1.1로 수행되었고 정상적으로 성공했습니다.** `agp = "8.6.0"` 카탈로그 항목은 선언만 되어 있을 뿐 실제로 어떤 모듈에서도 참조되지 않는 **미사용(dead) 항목**으로 확인됨
- 이번 build는 AGP version conflict로 인한 실패가 아니었으므로, 지시사항 18번 원칙("성공하면 AGP 설정을 수정하지 않는다")에 따라 **AGP 관련 설정은 전혀 수정하지 않았음**

## 기타 compile 오류
- `app:compileDebugJavaWithJavac` 단계에서 경고만 발생: `source value 8 is obsolete`, `target value 8 is obsolete` — 원본의 `JavaVersion.VERSION_1_8` 설정에 대한 최신 javac의 통상적인 경고이며 **빌드 실패 아님**, 수정하지 않음
- 컴파일 오류(error) 0건

## 적용한 최소 수정 (build를 위해서만, 3단계 지시사항 범위 내)
| 수정 | 대상 | 영구 반영 여부 |
|---|---|---|
| XML 네임스페이스 오타 수정 | `app/src/main/res/layout/activity_load_excel.xml` | ✅ 영구 반영 (공개 후보) |
| synthetic 무음 WAV 2개 추가 | `app/src/main/res/raw/aquaa.wav`, `warningsound.wav` | ✅ 영구 반영 (공개 후보) |
| 임시 `local.properties` | SDK 경로 지정용 | ❌ build 후 삭제 |
| 임시 `app/google-services.json` (dummy 값) | google-services 플러그인 구성 요구사항 충족용 | ❌ build 후 삭제 |
| 임시 `gradle.properties` 경로검사 우회 라인 | 로컬 한글 경로 문제 우회 | ❌ build 후 즉시 원복(SHA256 검증 완료) |

## 최종 build 결과
```
BUILD SUCCESSFUL in 3m 41s
32 actionable tasks: 32 executed
```

## APK 생성 여부
- 생성됨: `app/build/outputs/apk/debug/app-debug.apk`
- 크기: 19,512,823 bytes (약 18.6MB)
- `processDebugGoogleServices` task 정상 완료 (임시 dummy google-services.json 기준 — 실제 Firebase 자격증명 아님)

## build output 정리 여부
- `app/build/`, `.gradle/` 등 build 산출물 전체 삭제 완료
- `app-debug.apk`는 GitHub 공개 대상이 아니므로 **삭제 완료** (로컬에도 남기지 않음)
- 임시 `local.properties`, 임시 `app/google-services.json` **삭제 완료**
- `gradle.properties`는 build 전과 SHA256이 동일함을 재확인(41a9783e6d8fba6d52cf787adab4b02b9d0593169e30590c51aa9be2eebe8182) — 의도치 않은 잔여 변경 없음

## Firebase build vs runtime 구분 (중요)
- **Android compile/build: 검증됨** (dummy 설정값으로 `assembleDebug` 성공)
- **Firebase runtime 연동: 미검증** — 실제 Firebase 프로젝트에 연결한 적 없음, 네트워크 요청 없음
- **실제 `drugInteractions` DB: 존재하지 않음** — 이번 build는 해당 데이터 유무와 무관하게 성공(코드는 런타임에 조회하며, build 타임에는 데이터 접근 없음)
- `BUILD SUCCESSFUL`은 **"Firebase 연동 성공"을 의미하지 않음**
