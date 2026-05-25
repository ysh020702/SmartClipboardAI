# T-310 Topic 데이터 선택 플로우

## 목적

사용자가 DataItem을 직접 선택하거나 Gemini 후보를 검토해 Topic에 연결합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-310-topic-data-selection-flow
- Depends on: T-300-topic-create-flow, T-210-data-list-filter-selection
- Blocked by: Topic 생성과 데이터 리스트 UX 미완료
- Ready criteria: T-300과 T-210이 Done
- Can run in parallel with: none
- Cannot run with: T-300-topic-create-flow, T-210-data-list-filter-selection

## 수정 허용 파일

- Topic 데이터 선택 관련 UI 파일
- 관련 ViewModel state/intent

## 수정 금지 파일

- DB schema
- Share/Tile/Media handler
- Gemini manager 구현

## 구현 내용

- 직접 선택한 DataItem을 Topic에 연결합니다.
- Gemini 추천 후보는 임시 UI 상태로 보여주고 사용자 확인 후 연결합니다.
- 필터가 바뀌어도 선택 상태를 유지합니다.

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

- Topic에 DataItem이 안정적으로 연결됩니다.

## 검증 방법

- 직접 선택
- 필터 변경 후 선택 유지
- Gemini 후보 선택 mock
- `./gradlew assembleDebug`

## PR에 반드시 적을 내용

- 선택 상태 정책
- Topic 연결 방식
- 임시 추천 UI 처리
