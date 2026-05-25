# T-410 TopicAction 초안 생성

## 목적

`TopicAnalysis` 결과를 요약, 일정, 리마인더, 공유 초안, TODO 같은 `TopicAction` 초안으로 변환합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-410-topic-action-draft
- Depends on: T-400-topic-analysis-draft
- Blocked by: TopicAnalysis 생성 미완료
- Ready criteria: T-400이 Done
- Can run in parallel with: none
- Cannot run with: TopicAction model 변경 task

## 수정 허용 파일

- Agent action generator 관련 파일
- `TopicAction` 변환 관련 repository 연결부
- 관련 테스트 파일

## 수정 금지 파일

- 수집 flow
- MediaStore flow
- 대형 UI 재설계

## 구현 내용

- `TopicActionStatus.DRAFT` 상태로 초안을 생성합니다.
- 사용자가 검토하기 전 실행하지 않습니다.
- 일정 후보, 요약 후보, 할 일 후보를 분리합니다.

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

- 여러 타입의 TopicAction 초안이 생성됩니다.

## 검증 방법

- 샘플 TopicAnalysis 입력 테스트
- `./gradlew test`
- `./gradlew assembleDebug`

## PR에 반드시 적을 내용

- 생성되는 action type
- 초안 상태 처리
- 사용자 검토 전 실행 없음
