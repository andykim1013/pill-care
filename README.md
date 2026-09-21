# Pill Care (MediCheck)

처방 데이터 기반 약물 상호작용 확인 및 복약 알람 Android 애플리케이션

> ⚠️ **이 프로젝트는 2025학년도 1학기 대학 팀 설계 수업(캡스톤 성격)에서 제작된 교육용/프로토타입 Android 애플리케이션입니다.**
> 실제 의료기기가 아니며, 실제 의료적 판단·처방·복약 결정에 사용해서는 안 됩니다. 이 Repository와 앱이 제공하는 어떠한 정보도 의학적 정확성이나 임상적 검증을 거치지 않았습니다.

## 목차
- [프로젝트 소개](#프로젝트-소개)
- [문제 정의 / 목적](#문제-정의--목적)
- [핵심 기능](#핵심-기능)
- [미구현 / 최종 범위 제외 기능](#미구현--최종-범위-제외-기능)
- [시스템 Architecture](#시스템-architecture)
- [기술 스택](#기술-스택)
- [Source Provenance](#source-provenance)
- [Repository 구조](#repository-구조)
- [Build 검증 결과](#build-검증-결과)
- [Build 방법](#build-방법)
- [Firebase 설정](#firebase-설정)
- [Firebase 검증 상태](#firebase-검증-상태)
- [알람음(오디오) 처리](#알람음오디오-처리)
- [사용자 데이터 저장](#사용자-데이터-저장)
- [실행/검증 상태 요약](#실행검증-상태-요약)
- [의료/안전 Disclaimer](#의료안전-disclaimer)
- [알려진 한계](#알려진-한계)
- [개발 과정](#개발-과정)
- [팀 프로젝트](#팀-프로젝트)
- [관련 문서](#관련-문서)

## 프로젝트 소개

- **Pill Care** — 이 프로젝트의 공식 서비스/문서상 명칭(한글: 필 케어)
- **MediCheck** — 실제 Android Studio 프로젝트명이자 코드상 명칭. package `org.techtown.medicheck`

Pill Care와 MediCheck는 **같은 프로젝트**를 가리킵니다. 팀 산출물(제안서, 설계서, 발표자료 등)에서는 "Pill Care"라는 이름을 사용했고, 실제 Android 소스 코드/Gradle 프로젝트/패키지명은 "MediCheck"로 작성되었습니다.

## 문제 정의 / 목적

다제약물(여러 약을 동시에 복용)을 복용하는 사용자는 약물 간 상호작용을 스스로 확인하기 어렵고, 복약 일정을 놓치기 쉽습니다. 이 프로젝트는 다음을 목표로 합니다.

- 사용자가 처방받은 약 정보를 Excel 파일로 업로드해 앱에서 확인
- 등록된 약물 성분과 복용 금지 성분/질환 정보를 대조해 위험 여부를 표시
- 약을 복용해야 하는 시간에 알람으로 알려주는 복약 일정 관리

> 이 프로젝트는 "다제약물 상호작용 확인 및 복약 일정관리"라는 문제의식에서 출발한 **학습 목적의 프로토타입**입니다. 실제 약물 사고를 감소시켰다거나 환자 안전을 입증했다는 임상적 근거는 없습니다.

## 핵심 기능

실제 소스 코드(`app/src/main/java/org/techtown/medicheck/`)에서 확인되는 구현 기능만 기술합니다.

- 처방 Excel 파일 업로드 (`LoadExcel`, `LoadExcelActivity`)
- Apache POI 기반 Excel parsing
- 약물 목록 표시 (`MedicineItem`, `MedicineAdapter`)
- 복용 금지 성분 / 만성질환 설정 (`MyInfo`)
- Firebase Realtime Database의 `drugInteractions` 참조 데이터 조회
- 약물 조합 위험 경고 (`AlarmList`)
- 복약 알람 설정, 반복 알람(요일/주기) (`AlarmSetting`)
- 알람 On/Off, 알람 Stop / Snooze (`AlarmReceiver`, `SnoozeReceiver`, `StopAlarmReceiver`)
- 캘린더 기반 오늘 복약 목록 표시 (`MainActivity`)
- `SharedPreferences` 기반 사용자 설정/알람 데이터 로컬 저장

## 미구현 / 최종 범위 제외 기능

프로젝트 초기 기획 단계에서 논의되었으나 **실제 코드로 구현되지 않았거나 최종 범위에서 제외된 기능**입니다. 위 핵심 기능과 혼동하지 않도록 별도 표기합니다.

| 기능 | 상태 |
|---|---|
| 스마트워치(심박/맥박) 연동 | 초기 기획 아이디어, 미구현 |
| OCR/사진 촬영 약 인식 | 초기 기획 아이디어, 미구현 |
| AI 기반 위험군 분석 | 초기 기획 아이디어, 미구현 |
| 다국어 지원 | 초기 기획 아이디어, 미구현 |
| 처방 사이트 자동 크롤링 | 시도했으나 보안/접근 문제로 폐기 → 사용자가 직접 Excel을 다운로드해 업로드하는 방식으로 대체 |

## 시스템 Architecture

```
[사용자]
   │
   │ 처방 Excel 선택
   ▼
[LoadExcel / LoadExcelActivity]
   │
   │ Apache POI parsing
   ▼
[MedicineItem / MedicineAdapter]
   │
   ├───────────────┐
   ▼               ▼
[SharedPreferences] [Firebase Realtime Database]
   │               │
   │               └─ drugInteractions 노드 READ-only 조회
   ▼
[MainActivity / AlarmList]
   │
   ▼
[AlarmManager / NotificationCompat / Snooze / Stop]
```

- Firebase는 **READ-only**로만 사용됩니다. 코드 전체에서 `setValue`/`push` 등 쓰기 호출은 발견되지 않았습니다(자세한 근거: [`docs/firebase-setup.md`](docs/firebase-setup.md)).
- 사용자 개인 설정(복용 금지 성분, 만성질환, 알람 목록 등)은 기기 로컬의 `SharedPreferences`에만 저장되며 서버로 전송되지 않습니다.

## 기술 스택

| 구분 | 내용 |
|---|---|
| Platform | Android Native |
| Language | Java |
| Build | Gradle Wrapper 8.7 |
| Android | compileSdk 35 / minSdk 26 / targetSdk 35 |
| Database | Firebase Realtime Database (READ-only) |
| Excel | Apache POI 5.2.3 / POI-OOXML 5.2.3 |
| Local Storage | SharedPreferences |
| Notifications | AlarmManager / NotificationCompat |

원본 Gradle 설정에는 AGP(Android Gradle Plugin) 표기가 두 군데(`build.gradle.kts`의 buildscript classpath와 `gradle/libs.versions.toml`의 version catalog)에 존재하며 서로 다른 버전을 적고 있습니다. 실제로 어느 쪽이 build에 적용되는지, 그리고 이것이 실제 충돌을 일으키는지에 대한 분석은 [`docs/build-verification.md`](docs/build-verification.md)에 정리되어 있습니다.

## Source Provenance

이 Repository의 각 파일이 **2025년 원본 그대로인지, 공개를 위해 손을 댄 것인지**를 명확히 구분합니다. 전체 파일 단위 상세 목록은 [`docs/source-map.md`](docs/source-map.md)를 참고하세요.

| 구분 | 상태 |
|---|---|
| 대부분의 Android source/config/resource | 2025년 `ver2.5` 실제 원본 (byte-identical) |
| `AlarmList.java` | 실제 원본 기반, Firebase Database URL 문자열 1곳만 공개용 placeholder로 치환 |
| `activity_load_excel.xml` | 실제 원본 기반, XML 네임스페이스 오타 1건을 build 통과를 위해 최소 수정 |
| `aquaa.wav` / `warningsound.wav` | 신규 공개용 synthetic 무음 placeholder (2025년 원본 음원 아님) |
| `firebase/sample-drug-interactions.json` | 신규 공개용 synthetic example (실제 데이터 아님) |
| `app/google-services.example.json` | 신규 공개용 설정 예시 (placeholder 값만 포함) |

## Repository 구조

```
pill-care/
├── app/            # Android 소스 (org.techtown.medicheck)
├── firebase/       # Firebase 구조 설명용 synthetic sample 데이터
├── gradle/         # Gradle version catalog, wrapper
├── docs/           # 원본 출처/보안정리/빌드검증 등 상세 문서
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew / gradlew.bat
├── .gitignore
└── README.md
```

## Build 검증 결과

**환경**: Windows, Gradle Wrapper 8.7, JDK 21

- 기본 JDK 26은 Gradle 8.7과 호환되지 않아 build가 실패했습니다(Kotlin DSL 스크립트 컴파일러가 JDK 26 버전 문자열을 인식하지 못함).
- 이미 설치되어 있던 JDK 21로 전환한 뒤 `assembleDebug`를 실행해 **BUILD SUCCESSFUL**을 확인했습니다.

이 JDK 21 환경은 2025년 당시 원본 개발 환경이 아니라, **이 공개용 Repository를 2026년에 검증한 build verification 환경**입니다. 상세 로그와 오류 분류는 [`docs/build-verification.md`](docs/build-verification.md)에 정리되어 있습니다.

### 비ASCII 경로 주의

검증 중 작업 경로에 비ASCII(한글) 문자가 포함되어 AGP의 경로 검증 오류가 발생한 적이 있습니다. 이 Repository를 clone할 때는 **가능하면 ASCII 문자만 포함된 경로**를 사용하는 것을 권장합니다. 문제가 발생할 경우의 우회 방법은 [`docs/build-verification.md`](docs/build-verification.md)를 참고하세요.

## Build 방법

### Prerequisites

1. Android SDK (compileSdk 35, build-tools 35.0.0 이상)
2. Firebase 설정 — 아래 [Firebase 설정](#firebase-설정) 섹션을 먼저 진행해야 `google-services` 플러그인이 정상적으로 구성 단계를 통과합니다.

### Windows

```
gradlew.bat assembleDebug
```

이 명령은 이번 검증에서 실제로 **BUILD SUCCESSFUL**을 확인했습니다.

### Linux / macOS

```
./gradlew assembleDebug
```

이번 검증은 Windows 환경에서만 수행했으며, Linux/macOS에서의 동작은 **확인되지 않았습니다.** 위 명령이 동일하게 성공한다고 단정하지 마세요.

## Firebase 설정

이 Repository에는 **2025년 원본 Firebase credential이 포함되어 있지 않습니다.** `app/google-services.json`(실제 값)은 제외되었고, `app/google-services.example.json`(placeholder)만 포함되어 있습니다.

이 기능을 재현하려면:

1. 자신의 Firebase 프로젝트를 새로 생성
2. Android 앱 등록 시 package name을 `org.techtown.medicheck`로 일치시킴
3. Realtime Database 활성화
4. 발급받은 자신의 `google-services.json`을 `app/`에 배치
5. `AlarmList.java`의 `"YOUR_FIREBASE_DATABASE_URL"`을 자신의 Realtime Database URL로 교체
6. `drugInteractions` 노드 구조는 [`firebase/sample-drug-interactions.json`](firebase/sample-drug-interactions.json)을 참고해 직접 구성

기술적 요구사항의 상세 내용은 [`docs/firebase-setup.md`](docs/firebase-setup.md)에 정리되어 있습니다.

## Firebase 검증 상태

| 항목 | 상태 |
|---|---|
| Android compile/build | ✅ 검증 완료 |
| Firebase runtime connection | ❌ 미검증 |
| 실제 원본 Firebase DB | Repository에 포함되어 있지 않음 |

`assembleDebug`의 BUILD SUCCESSFUL은 dummy 설정값으로 컴파일/리소스 처리 단계가 통과했다는 의미이며, **"Firebase 연동 완료", "실시간 DB 동작 검증 완료", "end-to-end 검증 완료"를 의미하지 않습니다.**

### Sample Firebase data

[`firebase/sample-drug-interactions.json`](firebase/sample-drug-interactions.json)은 코드에서 확인되는 `drugInteractions` 노드의 **구조를 설명하기 위한 synthetic example**입니다. 실제 Firebase dump가 아니며, 실제 약물 상호작용 의학정보도 아닙니다. `sample_drug_a`, `SAMPLE_INGREDIENT_B` 같은 값은 전부 명백한 placeholder입니다.

## 알람음(오디오) 처리

원본 앱이 사용하던 알람음(mp3 4종)은 출처와 재배포 권리가 확인되지 않아 이 공개 Repository에서 제외되었습니다.

대신 `app/src/main/res/raw/aquaa.wav`, `app/src/main/res/raw/warningsound.wav`는 **build와 리소스 참조(`R.raw.aquaa`, `R.raw.warningsound`)를 유지하기 위해 Python 표준 라이브러리(`wave`)로 직접 생성한 0.6초 무음 placeholder**입니다.

> 현재 이 GitHub 공개본의 알람음은 **2025년 당시 실제 앱에서 사용된 음원이 아닙니다.**

## 사용자 데이터 저장

실제 런타임 사용자 설정(복용 금지 성분, 만성질환, 알람 목록, 식사시간 등)은 기기 로컬의 `SharedPreferences`에 저장되는 구조입니다. 이 Repository에는 **실제 사용자 데이터, 실제 복약 데이터, 실제 환자 데이터가 전혀 포함되어 있지 않습니다.**

## 실행/검증 상태 요약

| 항목 | 상태 |
|---|---|
| Source structure | 검증됨 |
| XML/static check | 검증됨 |
| assembleDebug | 성공 |
| APK 생성 | 확인 후 제거(Repository에 미포함) |
| Firebase runtime | 미검증 |
| Device/emulator 실행 | 미검증 |
| Clinical validation | 없음 |

## 의료/안전 Disclaimer

- 이 프로젝트는 **교육 및 학기설계 목적으로 개발한 prototype**입니다.
- 의료 전문가의 판단을 **대체하지 않습니다.**
- 실제 복약 여부나 약물 상호작용 판단 용도로 **사용하지 마세요.**
- `firebase/sample-drug-interactions.json`의 sample 데이터는 **의학 정보가 아닙니다.**

## 알려진 한계

- Firebase runtime 미검증
- 실제 원본 Firebase DB export 없음 (코드 분석 기반 구조 추정만 가능)
- Device/emulator에서의 실제 실행(runtime) 미검증 — 이번 검증은 compile/build 단계까지만 수행
- 원본 알람 mp3 공개 제외, synthetic 무음 오디오로 대체
- 원본이 참조하던 외부 처방 정보 사이트("내가 먹는 약 한눈에!")가 현재도 정상 동작하는지 확인되지 않음

## 개발 과정

```
초기 아이디어
   │
   ▼
자동 크롤링 시도
   │
   ▼ (보안/접근 문제)
수동 Excel 업로드 방식으로 전환
   │
   ▼
MediCheck ver2.1 (Firebase 미도입)
   │
   ▼
MediCheck ver2.5 — Firebase + Apache POI-OOXML 등 포함, 최종 버전
```

## 팀 프로젝트

이 프로젝트는 **7인 팀 프로젝트**로, PM(Project Manager) / CM(Configuration Manager) / QA(Quality Assurance) / ENG(Engineer) 역할로 분담해 진행되었습니다. 이 공개 Repository에는 팀원 개인의 실명, 학번, 연락처 등 개인정보를 포함하지 않습니다.

## 관련 문서

- [`docs/source-map.md`](docs/source-map.md) — 파일별 원본 출처/수정 상태
- [`docs/source-integrity.md`](docs/source-integrity.md) — 원본 archive 무결성 검증 결과
- [`docs/security-cleanup.md`](docs/security-cleanup.md) — 제외/치환된 보안 관련 항목 기록
- [`docs/missing-or-excluded-files.md`](docs/missing-or-excluded-files.md) — 제외된 파일과 이유
- [`docs/version-history.md`](docs/version-history.md) — ver2.1 → ver2.5 버전 이력
- [`docs/build-verification.md`](docs/build-verification.md) — 실제 build 검증 로그와 환경
- [`docs/firebase-setup.md`](docs/firebase-setup.md) — Firebase 재현을 위한 기술 요구사항
