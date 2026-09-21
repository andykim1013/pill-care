# Version History (1단계 원본 조사 + 2단계 재확인 근거)

## ver2.1 → 초기/중간 버전
- 위치: 원본 폴더 최상위 `MediCheck ver2.1.zip` (General 폴더에도 동일 사본 존재)
- zip 내부 entry 최종 타임스탬프: 2025-05-12
- Java class 8개만 존재: `AlarmList, AlarmReceiver, AlarmSetting, LoadExcel, MainActivity, MyInfo, SnoozeReceiver, StopAlarmReceiver`
- Firebase 미도입 (`google-services.json` 없음, `firebase-database`/`firebase-analytics` 의존성 없음)
- Apache POI는 `poi:5.2.3`만 존재, `poi-ooxml`은 아직 없음
- 처방 엑셀 업로드 후 "약물 조합 간 상호작용(Firebase 조회)" 기능이 아직 구현되지 않은 단계
- 공개 소스 후보(pill-care)에는 **포함하지 않음** — 참고용으로만 원본 폴더에 보존

## ver2.5 → 최종 Source of Truth
- 위치: `General/MediCheck_ver2.5.zip`
- zip 내부 entry 최종 타임스탬프: 2025-05-25 (ver2.1보다 늦음)
- Java class 11개로 확장: 위 8개 + `LoadExcelActivity, MedicineAdapter, MedicineItem` 신규 추가
- Firebase Realtime Database 도입 (`google-services.json`, `firebase-database:20.3.0`, `firebase-analytics:21.3.0`) → `AlarmList.java`에서 "drugInteractions" 노드를 조회해 등록된 약물 간 상호작용을 판정하는 기능 완성
- Apache POI에 `poi-ooxml:5.2.3` 추가 → 엑셀(.xlsx) 파싱 안정화
- UI 리소스 확장: `btnaddalarm.png, btnalarmlist.png, btnloadexcel.png, btnmyinfo.png, excelload_icon.png, myinfo_icon.png` 및 `item_medicine.xml, activity_load_excel_dummy.xml` 레이아웃 추가
- `warningsound.mp3` 알람음 추가(상호작용 경고 전용)
- **문서 교차검증**: `[ENG]소스코드V1.0.0.hwpx`(작성일 2025.05.28)의 11개 class 목차·본문이 ver2.5의 11개 class와 class 단위로 정확히 일치(`source-integrity.md` 4번 항목 참고) → ver2.5가 실제 제출/완료된 최종본임을 재확인
- **이번 2단계의 Source of Truth**로 채택, `pill-care/` 소스 트리의 기반이 됨

## 미구현 기능 (ver2.1/ver2.5 어느 쪽에도 코드로 존재하지 않음, 1단계 확인)
- AI 기반 위험군 분석
- 사진 촬영 약 인식(OCR)
- 스마트워치(심박/맥박) 연동
- 다국어 지원
- 감염병 확산 연계 의약품 수급 예측
- 처방 사이트 자동 크롤링(보안 문제로 시도 후 폐기, 수동 업로드 방식으로 대체)

이번 2단계에서도 위 미구현 기능을 코드로 추가하거나 구현하지 않았음(지시사항 27번 준수).
