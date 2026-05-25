# T-510 Notes 공유 초안

## 목적

요약/노트 초안을 Android Share Sheet로 전달해 사용자가 Notes 또는 다른 앱을 선택하게 합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-510-notes-share-draft
- Depends on: T-410-topic-action-draft
- Blocked by: TopicAction 초안 생성 미완료
- Ready criteria: T-410이 Done
- Can run in parallel with: T-500-calendar-intent-draft
- Cannot run with: Action model 변경 task

## 수정 허용 파일

- `app/src/main/java/com/samsung/smartclipboard/presentation/handoff/`
- Action review UI 관련 파일
- 관련 QA 문서

## 수정 금지 파일

- DB schema
- 수집 flow
- Gemini prompt/generator

## 구현 내용

- Notes 전용 구현이 아니라 공유 초안 전달로 시작합니다.
- 사용자는 Android chooser에서 Samsung Notes 또는 다른 앱을 선택합니다.

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

- 공유 초안 chooser가 열리고 제목/본문이 전달됩니다.

## 검증 방법

- Samsung Notes 또는 대체 앱으로 공유 확인
- 대상 앱 없음 케이스 확인
- `./gradlew assembleDebug`

## PR에 반드시 적을 내용

- 공유 payload
- chooser 동작
- 실패 처리
