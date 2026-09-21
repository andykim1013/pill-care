# Firebase Setup (3단계, 기술 개요)

과거 원본 프로젝트가 사용하던 Firebase 프로젝트의 credential은 이 공개 후보에 전혀 포함되어 있지 않습니다.
이 문서는 공개 사용자가 **자신의** Firebase 프로젝트로 이 기능을 재현하기 위한 기술적 요구사항만 정리합니다.

## Firebase가 사용되는 기능
- 기능: `AlarmList` 화면에서 현재 등록된 알람들의 약물 성분 조합 간 **상호작용 위험 여부**를 판정
- 사용 클래스: **`AlarmList.java` 1개뿐** (그 외 클래스는 Firebase를 호출하지 않음 — `LoadExcelActivity.java`는 관련 import만 있고 실제 호출 없음)

## READ-only 구조
- `FirebaseApp.initializeApp(this)`로 초기화 후 `FirebaseDatabase.getInstance(...)`로 접속
- `addListenerForSingleValueEvent`로 **1회 조회만** 수행 — 지속 구독(`addValueEventListener`)이나 쓰기(`setValue`, `.push()`) 호출은 코드 전체에서 발견되지 않음
- 즉 이 앱은 Firebase에 사용자 데이터를 저장하지 않으며, 오직 "위험 조합 참조 테이블"을 읽어오는 용도로만 사용

## `drugInteractions` node
- 조회 경로: `database.getReference("drugInteractions")`
- 코드에서 확인되는 구조(2단계): `drugInteractions/<약물명>/<콤보키>: "<위험조합 성분명>"` — 약물명을 key로, 하위 children의 문자열 값들이 해당 약물과 함께 복용하면 위험한 성분명
- 실제 DB export나 스키마 문서가 없으므로 이 구조는 **코드 분석으로 확인한 범위까지만** 서술함(추측 금지 원칙 준수)

## 과거 credential 미포함
- `app/google-services.json`(원본 Firebase project_id/api_key/database_url 등)은 이 Repository에 포함되지 않음
- `AlarmList.java`의 Firebase URL은 `"YOUR_FIREBASE_DATABASE_URL"` placeholder로 대체되어 있음(원본 값 아님)
- 과거 원본 Firebase 프로젝트를 재사용하는 방법은 **안내하지 않음**(계정/과금 주체가 다른 팀 소유이므로)

## 새 Firebase 프로젝트가 필요함
이 기능을 실제로 재현하려면 사용자 본인 소유의 새 Firebase 프로젝트가 필요합니다.

## `google-services.json` 준비 방법(기술 요구사항 개요)
1. Firebase 콘솔에서 신규(또는 기존 별도) 프로젝트 생성
2. 해당 프로젝트에 **Android 앱**을 등록, package name은 반드시 `org.techtown.medicheck`로 일치시켜야 함(코드의 `namespace`/`applicationId`와 일치해야 `google-services` 플러그인이 올바르게 매칭)
3. Firebase가 발급하는 `google-services.json`을 다운로드하여 `app/google-services.json` 위치에 배치(이 파일은 `.gitignore` 대상 — 개인 Repository에도 커밋하지 않는 것을 권장)
4. Realtime Database를 활성화하고 리전을 선택

## RTDB URL placeholder 교체 필요
- `app/src/main/java/org/techtown/medicheck/AlarmList.java`에서 `FirebaseDatabase.getInstance("YOUR_FIREBASE_DATABASE_URL")`의 문자열을 본인의 Realtime Database URL로 직접 교체해야 함
- 이번 단계에서는 이 값을 `BuildConfig`/리소스/로컬 설정 파일 등으로 리팩터링하지 않았음 — 원본 코드 구조를 그대로 유지하는 것이 우선이며, 사용자가 문자열을 직접 교체하는 방식을 그대로 둠(지시사항 24번 준수)

## synthetic sample JSON 설명
- `firebase/sample-drug-interactions.json`은 위 `drugInteractions` 노드의 **구조를 보여주기 위한 synthetic 예시**일 뿐, 실제 Firebase dump도 실제 의학 정보도 아님
- `sample_drug_a`, `SAMPLE_INGREDIENT_B` 같은 값은 전부 명백한 placeholder이며, 실제 약물/성분명이 아님
- 사용자는 이 구조를 참고해 **본인이 직접 검증한 실제 약물 상호작용 데이터**를 채워 넣어야 함(의학적 정확성은 이 프로젝트가 보증하지 않음)

## build success ≠ Firebase runtime success
- `docs/build-verification.md`에서 확인된 `BUILD SUCCESSFUL`은 dummy `google-services.json`으로 **컴파일/리소스 처리 단계**가 통과했다는 의미일 뿐입니다
- 실제 앱을 기기에서 실행했을 때 `AlarmList` 화면이 정상적으로 상호작용 데이터를 받아오려면, 위 절차대로 **사용자 본인의 Firebase 프로젝트 + RTDB URL 교체 + drugInteractions 데이터 구성**이 모두 완료되어야 함
- 이번 단계에서는 실제 Firebase 네트워크 연결을 전혀 시도하지 않았음(과거 credential 접속 금지 원칙 준수)
