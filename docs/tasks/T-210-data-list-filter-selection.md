# T-210 데이터 리스트/필터/선택 UX

## 목적

수집 데이터 리스트를 Topic 생성의 보조 흐름으로 정리하고 타입/날짜 필터 및 선택 유지 UX를 개선합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-210-data-list-filter-selection
- Depends on: T-040-navigation-baseline
- Blocked by: navigation baseline 미완료
- Ready criteria: T-040이 Done
- Can run in parallel with: T-220-save-feedback-bottom-sheet
- Cannot run with: T-200-home-ux-redesign, T-310-topic-data-selection-flow

## 수정 허용 파일

- 데이터 리스트 관련 `presentation/main/` 파일
- 관련 UI test 또는 QA 문서

## 수정 금지 파일

- DB schema
- Manifest
- Share/Tile/Media handler

## 구현 내용

- 타입 필터와 날짜 필터를 정리합니다.
- 필터로 화면에서 사라져도 선택 상태는 유지합니다.
- Topic 생성으로 이어지는 CTA를 명확히 합니다.

## 체크리스트
- [ ] 코드 읽기
- [ ] 관련 문서 확인
- [ ] 선행 task 완료 여부 확인
- [ ] 구현
- [ ] 빌드 확인
- [ ] 테스트/수동 확인
- [ ] 변경 요약 작성
- [ ] PR 작성

## 완료 기준

- 필터와 선택 상태가 안정적으로 동작합니다.

## 검증 방법

- 타입 필터, 날짜 필터, 선택 유지 수동 확인
- `./gradlew assembleDebug`

## PR에 반드시 적을 내용

- 필터 UX 변경
- 선택 유지 정책
- 테스트 결과
