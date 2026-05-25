# T-500 Calendar intent 초안

## 목적

Calendar insert intent를 통해 사용자가 검토 가능한 일정 초안을 엽니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-500-calendar-intent-draft
- Depends on: T-410-topic-action-draft
- Blocked by: TopicAction 초안 생성 미완료
- Ready criteria: T-410이 Done
- Can run in parallel with: T-510-notes-share-draft
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

- Calendar action 초안을 Android Calendar insert intent로 전달합니다.
- 저장은 Calendar 앱에서 사용자가 최종 결정합니다.
- 날짜 정보가 불완전하면 사용자에게 수정 필요 상태를 보여줍니다.

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

- Calendar 앱이 초안 상태로 열립니다.

## 검증 방법

- Calendar 앱 설치/미설치 케이스 확인
- 날짜 후보 있음/없음 확인
- `./gradlew assembleDebug`

## PR에 반드시 적을 내용

- intent payload
- 실패 처리
- 사용자 검토 지점
