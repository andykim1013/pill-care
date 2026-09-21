# Source Integrity Report (2단계 작성, 3단계에서 §5·§7·§9 갱신)

## 1. 원본 Archive 정보
- 경로: `General/MediCheck_ver2.5.zip` (원본 프로젝트 폴더 내, 수정하지 않음)
- 파일 크기: 211,042,269 bytes (약 201.3MB)
- SHA256: `1D05A84122296AFFAA53C40B9DE88F259B6D36CC59544F0A5392B7EBC639EAD6`
- zip entry 수: 1,117개 (파일 719개 + 디렉터리 398개)
- 파일시스템상 수정시각: 2026-09-20 23:15 (⚠️ 이 값은 최근 파일 복사/이동에 의한 시각으로 추정되며 실제 개발 시점을 반영하지 않음)
- 신뢰 가능한 실제 시점 근거: zip **내부 entry 타임스탬프**가 2025-03-25 ~ 2025-05-25 범위 → ver2.1(~2025-05-12)보다 늦은 최종 버전임을 재확인

## 2. 필수 구조 재확인 (전부 FOUND)
```
app/                          FOUND
app/src/main/                 FOUND
app/src/main/AndroidManifest.xml  FOUND
app/build.gradle.kts          FOUND
build.gradle.kts              FOUND
settings.gradle.kts           FOUND
gradle.properties             FOUND
gradle/libs.versions.toml     FOUND
gradlew                       FOUND
gradlew.bat                   FOUND
google-services.json          FOUND (공개본에서는 제외 처리)
local.properties              FOUND (공개본에서는 제외 처리)
```

## 3. Java 11개 class 존재 확인
`org.techtown.medicheck` 패키지, `app/src/main/java/org/techtown/medicheck/` 경로에 11개 전부 확인:
MainActivity, LoadExcel, LoadExcelActivity, AlarmList, AlarmReceiver, AlarmSetting, MedicineAdapter, MedicineItem, MyInfo, SnoozeReceiver, StopAlarmReceiver — **11/11 확인**

Test source 2개도 원본 그대로 확인: `ExampleInstrumentedTest.java`(androidTest), `ExampleUnitTest.java`(test)

## 4. 원본 문서(hwpx) source ↔ 원본 zip source 비교
비교 대상: `[ENG]Pill Care(필 케어)_소스코드V1.0.0.hwpx` (2025.05.28 작성) vs `MediCheck_ver2.5.zip` 내부 실제 `.java` (zip 소스 기준, 공개본 보안수정 반영 전)

방법: hwpx 내부 section XML에서 텍스트 추출 → 공백/개행 정규화 후 zip 소스와 문자 단위 유사도(SequenceMatcher) 비교

| class | 유사도 | 판정 |
|---|---|---|
| MainActivity | 99.93% | 완전 일치(공백/개행 차이만) |
| LoadExcel | 99.51% | 완전 일치(공백/개행 차이만) |
| LoadExcelActivity | 99.87% | 완전 일치(공백/개행 차이만) |
| AlarmList | 99.91% | 완전 일치(공백/개행 차이만) |
| AlarmReceiver | 99.84% | 완전 일치(공백/개행 차이만) |
| AlarmSetting | 99.87% | 완전 일치(공백/개행 차이만) |
| MedicineAdapter | 99.91% | 완전 일치(공백/개행 차이만) |
| MedicineItem | 99.67% | 완전 일치(공백/개행 차이만) |
| MyInfo | 99.69% | 완전 일치(공백/개행 차이만) |
| SnoozeReceiver | 98.96% | 완전 일치(공백/개행 차이만, 상대적으로 짧은 파일이라 문서변환 잡음 비중이 큼) |
| StopAlarmReceiver | 100.00% | 완전 일치 |

**결론**: 11개 class 전부 "코드 내용 차이" 또는 "문서에 일부 누락" 없음. 차이는 전부 HWPX → 텍스트 변환 과정의 공백/개행/이미지 대체텍스트 잡음으로 판단됨. (참고: 이 비교는 zip 원본 소스 기준이며, 공개본에서 1줄 수정한 AlarmList.java의 보안 수정본과 비교한 것이 아님)

## 5. 원본과 공개본 byte 비교 (3단계 기준 갱신)
| 구분 | 개수 | 비고 |
|---|---|---|
| byte-identical (zip entry와 SHA256 동일) | 75 | 2단계 76개 중 `activity_load_excel.xml` 1개가 3단계에서 build correction으로 수정되어 75개로 감소 |
| 보안상 최소 수정 (2단계, byte 다름) | 1 | AlarmList.java |
| build correction (3단계, byte 다름) | 1 | activity_load_excel.xml (`:layout_marginTop` → `android:layout_marginTop`) |
| 신규 생성 — 설정 예시 (2단계) | 1 | app/google-services.example.json |
| 신규 생성 — synthetic 오디오 (3단계) | 2 | app/src/main/res/raw/aquaa.wav, warningsound.wav |
| 신규 생성 — synthetic 샘플 데이터 (3단계) | 1 | firebase/sample-drug-interactions.json |
| 신규 생성 — 문서 (3단계) | 2 | docs/build-verification.md, docs/firebase-setup.md |
| **공개 후보 총 파일 수** | **85** | |

의도치 않은 변경 없음 — 수정된 파일은 AlarmList.java(2단계), activity_load_excel.xml(3단계) 2개뿐이며 변경 내용은 각각 `security-cleanup.md`, 본 문서 §7에 기록. 그 외 파일(`gradle.properties` 포함, 3단계 build 검증 중 임시 수정 후 SHA256 검증하여 원복)은 전부 원본과 byte-identical 유지.

## 6. 실제 dependency / version (build.gradle.kts, app/build.gradle.kts, libs.versions.toml 직접 확인, 추측 없음)
- Android Gradle Plugin: `com.android.tools.build:gradle:8.1.1` (루트 `build.gradle.kts` buildscript classpath)
  - ⚠️ 원본 자체의 불일치 발견: `gradle/libs.versions.toml`의 `agp = "8.6.0"` (버전 카탈로그 alias용)과 buildscript classpath의 `8.1.1`이 서로 다름 — 원본에 존재하던 불일치이며 이번 단계에서 임의로 통일하지 않음
- google-services 플러그인: `com.google.gms:google-services:4.3.15`
- compileSdk: 35 / minSdk: 26 / targetSdk: 35
- Java 호환성: sourceCompatibility/targetCompatibility = VERSION_1_8
- Gradle Wrapper: 8.7 (`gradle-wrapper.properties`)
- 주요 라이브러리:
  - androidx.appcompat 1.7.0, material 1.12.0(카탈로그) **및** `com.google.android.material:material:1.11.0`(중복 하드코딩 — 원본 자체 중복, 미수정)
  - androidx.activity 1.10.1, androidx.constraintlayout 2.2.1, androidx.core:core 1.9.0
  - org.apache.poi:poi:5.2.3 (2회 중복 선언 — 원본 자체 중복, 미수정), org.apache.poi:poi-ooxml:5.2.3
  - com.google.firebase:firebase-database:20.3.0, com.google.firebase:firebase-analytics:21.3.0
  - test: junit 4.13.2 / androidx.test.ext:junit 1.2.1 / espresso-core 3.6.1
- 버전은 원본 그대로이며 이번 단계에서 업그레이드하지 않았음

## 7. 정적 구조검사에서 발견된 원본 자체 결함 — 3단계에서 build correction 적용
- `app/src/main/res/layout/activity_load_excel.xml` 43번째 줄: `:layout_marginTop="0dp"` — `android:` 네임스페이스 접두사가 누락된 잘못된 XML 속성(원본 zip에도 동일하게 존재하는 원본 결함)
- **3단계 조치**: 공개 후보 사본에서만 `android:layout_marginTop="0dp"`로 최소 수정(그 외 레이아웃 구조/위치/색상/텍스트 무변경). `[실제 2025 원본 기반 공개용 build correction]`으로 분류
- 수정 전/후 SHA256:
  - 수정 전(원본): `f246abfa33e0d1dda2657923f695388825d51b4ca14c69d01ed99d87a3e08939`
  - 수정 후(공개본): 3단계 수정본 (XML well-formed 재검사 통과, 25개 XML 전수 오류 0건 확인)
- 이 수정 덕분에 `docs/build-verification.md`의 실제 `assembleDebug` 빌드가 XML 파싱 오류 없이 통과함을 확인

## 8. 제외된 파일 통계
- 빌드/IDE 캐시(.gradle, .idea, app/build 등): 636개 (2단계 기준. 3단계 build 산출물도 검증 후 전량 삭제 완료)
- 보안상 제외: 2개 (`app/google-services.json`, `local.properties`) — 3단계 build 검증 시 dummy/임시 값으로 잠깐 생성했다가 build 직후 삭제, 공개본에는 없음
- 출처 미확인 오디오: 4개 (원본 mp3, 3단계에서도 계속 제외) — 대신 synthetic 무음 WAV 2개로 대체

## 9. 3단계 build 검증 결과 요약
전체 내용은 `docs/build-verification.md` 참고. 핵심: JDK 26 비호환(Java 문제) → 기존 설치된 JDK 21로 전환 → 비ASCII 로컬 경로 문제(환경 문제, 원본 결함 아님) → 임시 우회 후 **BUILD SUCCESSFUL** (32 tasks, 3m 41s). AGP 버전 표기 불일치는 실제 충돌이 아님(카탈로그의 `agp=8.6.0`은 미사용 dead entry, 실제 적용은 buildscript classpath의 `8.1.1`)을 빌드 성공으로 재확인 — AGP 설정 미수정.
