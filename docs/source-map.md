# Source Map (4단계 기준으로 갱신)

상태 태그 정의:
- `[실제 2025 원본 그대로]` — 원본 zip entry와 byte-identical
- `[실제 2025 원본 기반 공개용 보안 정리]` — 원본 소스이나 값 일부를 placeholder로 최소 수정
- `[실제 2025 원본 기반 공개용 build correction]` — 원본 소스의 명백한 오타/결함을 build 통과를 위해 최소 수정
- `[신규 공개용 설정 예시]` — 원본에는 없던, 이번 단계에서 새로 만든 예시 파일
- `[신규 공개용 synthetic placeholder]` — 원본 대체용으로 이번 단계에서 직접 생성한 무음/가짜 리소스
- `[신규 공개용 synthetic example]` — 구조 설명용으로 새로 만든 가짜 예시 데이터
- `[공개본 제외]` — pill-care에 복사하지 않음(또는 4단계에서 제거)
- `[초기/중간 버전]` — 최종본이 아닌 이전 버전
- `[문서 교차검증용]` — 소스는 아니지만 원본 zip 소스와 대조 검증에 사용한 문서

## Gradle / 프로젝트 설정
| 경로 | 상태 |
|---|---|
| `build.gradle.kts` | [실제 2025 원본 그대로] |
| `settings.gradle.kts` | [실제 2025 원본 그대로] |
| `gradle.properties` | [실제 2025 원본 그대로] |
| `gradle/libs.versions.toml` | [실제 2025 원본 그대로] |
| `gradle/wrapper/gradle-wrapper.jar` | [실제 2025 원본 그대로] |
| `gradle/wrapper/gradle-wrapper.properties` | [실제 2025 원본 그대로] |
| `gradlew` / `gradlew.bat` | [실제 2025 원본 그대로] |
| `.gitignore` (root) / `app/.gitignore` | [실제 2025 원본 그대로] |
| `app/build.gradle.kts` | [실제 2025 원본 그대로] |
| `app/proguard-rules.pro` | [실제 2025 원본 그대로] |
| `local.properties` | [공개본 제외] — 로컬 Android SDK 경로 포함. 3단계 build 검증 중 임시 생성 후 build 완료 즉시 삭제(공개본에 없음) |
| `app/google-services.json` | [공개본 제외] — 실제 Firebase API 키 포함. 3단계 build 검증 중 dummy 값으로만 임시 생성 후 즉시 삭제(공개본에 없음) |
| `app/google-services.example.json` | [신규 공개용 설정 예시] — placeholder 값만 포함 |

⚠️ AGP 버전 표기(`build.gradle.kts`의 classpath 8.1.1 vs `libs.versions.toml`의 agp=8.6.0)는 3단계 build 검증 결과 **실제 충돌이 아님**으로 확인(카탈로그 항목은 미사용 dead entry). 상세 근거는 `docs/build-verification.md` 참고. 이번 단계에서 두 파일 모두 수정하지 않음(byte-identical 유지).

## Java 소스 (`app/src/main/java/org/techtown/medicheck/`)
| 클래스 | 상태 |
|---|---|
| MainActivity.java | [실제 2025 원본 그대로] |
| LoadExcel.java | [실제 2025 원본 그대로] |
| LoadExcelActivity.java | [실제 2025 원본 그대로] |
| AlarmList.java | [실제 2025 원본 기반 공개용 보안 정리] — Firebase RTDB URL 1곳만 placeholder로 교체 |
| AlarmReceiver.java | [실제 2025 원본 그대로] |
| AlarmSetting.java | [실제 2025 원본 그대로] |
| MedicineAdapter.java | [실제 2025 원본 그대로] |
| MedicineItem.java | [실제 2025 원본 그대로] |
| MyInfo.java | [실제 2025 원본 그대로] |
| SnoozeReceiver.java | [실제 2025 원본 그대로] |
| StopAlarmReceiver.java | [실제 2025 원본 그대로] |
| `app/src/androidTest/.../ExampleInstrumentedTest.java` | [실제 2025 원본 그대로] |
| `app/src/test/.../ExampleUnitTest.java` | [실제 2025 원본 그대로] |
| `app/StopAlarmReceiver.java` (app 루트, 패키지 경로 밖) | **[원본 archive에는 존재하지만 build source set 밖의 잔재 파일이라 공개본에서 제외]** — 4단계에서 pill-care 사본에서만 삭제. `package org.techtown.medicheck;` 선언과 `import androidx.core.app.NotificationManagerCompat;`이 빠진 불완전 fragment(3단계 분석). Gradle 기본 source set(`app/src/main/java`, `app/src/test/java`, `app/src/androidTest/java`)에 포함되지 않으며, `app/build.gradle.kts`에 커스텀 sourceSets 선언도 없음을 재확인. 3단계 실제 `assembleDebug` **BUILD SUCCESSFUL**이 이 파일 없이도(즉 존재해도 무관하게) 달성되어 build 무관함을 실증. 원본 `General/MediCheck_ver2.5.zip`에는 그대로 보존됨(원본 archive 무수정) |
| `app/alarmSetting.java` (app 루트, 32 bytes) | **[원본 archive에는 존재하지만 build source set 밖의 잔재 파일이라 공개본에서 제외]** — 위와 동일 사유로 4단계에서 pill-care 사본에서만 삭제. 내용 없는 빈 scaffold(`public class alarmSetting {}`), 실제 `AlarmSetting.java`와 무관. 원본 archive에는 그대로 보존됨 |

## 리소스 (`app/src/main/res/`)
| 구분 | 상태 |
|---|---|
| `drawable/`, `layout/`(activity_load_excel.xml 제외), `mipmap-*/`, `values/`, `values-night/`, `xml/` 전체 | [실제 2025 원본 그대로] |
| `layout/activity_load_excel.xml` | [실제 2025 원본 기반 공개용 build correction] — 원본의 `:layout_marginTop="0dp"`(네임스페이스 누락) → `android:layout_marginTop="0dp"`로 최소 수정. 그 외 구조/위치/색상/텍스트 변경 없음 |
| `AndroidManifest.xml`, `ic_launcher-playstore.png` | [실제 2025 원본 그대로] |
| `raw/alarm_sound.mp3`, `raw/beeth.mp3` (코드 미참조) | [공개본 제외] — 출처/라이선스 미확인, 3단계에서도 계속 제외 |
| `raw/aquaa.mp3`, `raw/warningsound.mp3` (코드에서 R.raw로 실사용) | [공개본 제외] — 출처/라이선스 미확인, 3단계에서도 계속 제외 |
| `raw/aquaa.wav`, `raw/warningsound.wav` | [신규 공개용 synthetic placeholder] — Python `wave` 표준 라이브러리로 직접 생성한 0.6초 무음 WAV(8kHz, mono, 16bit). 2025년 당시 원본 알람음이 아님. `R.raw.aquaa`/`R.raw.warningsound` 리소스 참조를 build 목적으로만 충족 |

## Firebase 재현용 신규 파일
| 경로 | 상태 |
|---|---|
| `firebase/sample-drug-interactions.json` | [신규 공개용 synthetic example] — `drugInteractions` 노드 구조 설명용, 실제 Firebase dump/의학정보 아님 |

## 교차검증용 문서 (pill-care에는 미포함, 원본 폴더에만 존재)
| 문서 | 상태 |
|---|---|
| `General/04. 구현/[ENG] 소스코드/[ENG]Pill Care(필 케어)_소스코드V1.0.0.hwpx` | [문서 교차검증용] — 11개 class 전체 대조 완료 (source-integrity.md 참고) |

## 버전 이력
| Archive | 상태 |
|---|---|
| `MediCheck ver2.1.zip` (최상위/General 사본) | [초기/중간 버전] — 공개 소스에서 제외, 섞지 않음 |
| `General/MediCheck_ver2.5.zip` | Source of Truth (원본 archive 자체는 [공개본 제외], 내부 파일만 선별 반영) |
