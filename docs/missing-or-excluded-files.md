# Missing or Excluded Files (2단계 작성, 3·4단계에서 갱신)

| 항목 | 원본 존재 여부 | 공개본(pill-care) 포함 여부 | 제외 이유 | build/runtime 영향 |
|---|---|---|---|---|
| `app/StopAlarmReceiver.java` (app 루트) | 존재 (ver2.5.zip, build 경로 밖) | **4단계에서 공개본 제외** | [원본 archive에는 존재하지만 build source set 밖의 잔재 파일이라 공개본에서 제외] — package/import 누락된 불완전 fragment | 없음 — Gradle 기본 source set 밖이라 3단계 `assembleDebug` 성공에 이미 영향 없었음을 실증. 원본 zip에는 그대로 보존 |
| `app/alarmSetting.java` (app 루트) | 존재 (ver2.5.zip, build 경로 밖) | **4단계에서 공개본 제외** | [원본 archive에는 존재하지만 build source set 밖의 잔재 파일이라 공개본에서 제외] — 내용 없는 빈 scaffold(`public class alarmSetting {}`) | 없음 — 동일 사유. 원본 zip에는 그대로 보존 |
| `app/google-services.json` | 존재 (ver2.5.zip) | 제외 (3단계에서도 dummy 값 임시 생성 후 삭제, 최종 미포함) | 실제 Firebase API 키 포함 | 3단계 실측 확인: dummy 값으로 `assembleDebug` **BUILD SUCCESSFUL**. 단, 실제 앱 실행 시에는 사용자가 자신의 Firebase 프로젝트에서 재발급 필요 |
| `local.properties` | 존재 (ver2.5.zip) | 제외 (3단계에서도 임시 생성 후 삭제, 최종 미포함) | 로컬 PC의 Android SDK 경로(개인 환경 정보) | 영향 없음 — Android Studio가 최초 빌드 시 자동 재생성 |
| `app/src/main/res/raw/alarm_sound.mp3` (11.46MB) | 존재 | 제외 (3단계에서도 계속 제외, 대체 없음) | 출처/재배포 라이선스 미확인 | 없음 — 코드에서 `R.raw.*`로 참조되지 않는 미사용 리소스. 3단계 build로 실측 확인(영향 없음) |
| `app/src/main/res/raw/beeth.mp3` (11.46MB, alarm_sound.mp3와 동일 크기) | 존재 | 제외 (3단계에서도 계속 제외, 대체 없음) | 출처/재배포 라이선스 미확인 | 없음 — 코드에서 참조되지 않는 미사용 리소스. 3단계 build로 실측 확인(영향 없음) |
| `app/src/main/res/raw/aquaa.mp3` (3.67MB) | 존재 | **원본은 제외.** 대신 `app/src/main/res/raw/aquaa.wav`(0.6초 무음 synthetic placeholder, 직접 생성)로 대체 | 출처/재배포 라이선스 미확인 | `AlarmReceiver.java`의 `R.raw.aquaa` 참조를 synthetic WAV로 충족 → 3단계 실측: **BUILD SUCCESSFUL**. ⚠️ 원본 알람음이 공개된 것이 아니라 무음 placeholder임에 주의 |
| `app/src/main/res/raw/warningsound.mp3` (62.97KB) | 존재 | **원본은 제외.** 대신 `app/src/main/res/raw/warningsound.wav`(0.6초 무음 synthetic placeholder, 직접 생성)로 대체 | 출처/재배포 라이선스 미확인 | `AlarmList.java`의 `R.raw.warningsound` 참조를 synthetic WAV로 충족 → 3단계 실측: **BUILD SUCCESSFUL**. ⚠️ 원본 알람음이 공개된 것이 아니라 무음 placeholder임에 주의 |
| `MediCheck ver2.1.zip` (최상위/General 사본) | 존재 | 제외 | 초기/중간 버전, 최종 소스와 섞으면 혼선 | 없음 — 공개 소스 트리와 무관 |
| `General/MediCheck_ver2.5.zip` (원본 archive 자체) | 존재 | 제외 | 211MB 대용량, GitHub 100MB 제한 초과, 이미 내부 파일을 선별 반영했으므로 archive 자체를 다시 넣을 필요 없음 | 없음 |
| 회의록(.hwp 다수) | 존재 (원본 폴더) | 제외 (이번 단계 범위 아님) | 팀원 실명+학번 노출, 1단계에서 PII로 지목됨 | 없음 — 소스 빌드와 무관 |
| 회의 영상 / 시연 영상 (mp4) | 존재 (원본 폴더) | 제외 (이번 단계 범위 아님) | 대용량 + 팀원 초상권/음성 포함 가능성 | 없음 |
| 중간/기말 발표 PPT | 존재 (원본 폴더) | 제외 (이번 단계 범위 아님) | 문서 자료는 3단계 이후 별도 판단 대상 | 없음 |
| 요구사항/설계/시험/완료보고서 (HWP/HWPX) | 존재 (원본 폴더) | 제외 (이번 단계 범위 아님) | 문서 자료는 3단계 이후 별도 판단 대상 | 없음 |
| `26졸업 논문 ppt.pptx` / `.pdf` | 존재 (원본 폴더) | **완전 제외** | 1단계에서 이 팀 프로젝트와 무관한 개인 졸업논문 자료로 확인됨(지도교수 상이, 팀원 개인 학번 노출) | 없음 |

## 요약
- 현재 pill-care는 **소스 트리(app/ + gradle 설정) + docs + firebase 샘플**을 포함하며, 문서/영상/회의록/무관자료는 여전히 복사 대상에 포함하지 않았음
- 대용량 원본 zip 2종은 리포지토리 후보에 포함하지 않음
- **원본 알람음 mp3 4종은 3단계에서도 전부 제외 상태 유지** — 대신 `aquaa.wav`, `warningsound.wav` 2개는 Python 표준 라이브러리로 직접 생성한 무음 synthetic placeholder이며, 2025년 당시 원본 알람음이 아님을 명확히 함(오인 방지)
- `firebase/sample-drug-interactions.json`은 원본에 없던 신규 synthetic 예시 파일(구조 설명용, 실제 데이터 아님)
