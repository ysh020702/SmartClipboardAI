# T-900 QA, 빌드, 테스트 정리

## 목적

MVP 흐름 전체를 수동/자동으로 검증하고 QA 문서를 정리합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: test/T-900-qa-build-test
- Depends on: T-500-calendar-intent-draft, T-510-notes-share-draft
- Blocked by: 외부 앱 연동 초안 미완료
- Ready criteria: T-500과 T-510이 Done
- Can run in parallel with: 작은 fix task
- Cannot run with: 대형 구조 변경 task

## 수정 허용 파일

- QA 문서
- 테스트 파일
- 작은 test-only fixture

## 수정 금지 파일

- 기능 구현 코드
- DB schema
- Manifest
- Gradle

## 구현 내용

- Share/Tile/MediaStore/SAF/Topic/Agent/Calendar/Notes 흐름을 체크리스트화합니다.
- 빌드와 테스트 결과를 기록합니다.
- 남은 이슈를 분류합니다.

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

- MVP 주요 흐름의 QA 결과가 문서화됩니다.

## 검증 방법

- `./gradlew test`
- `./gradlew assembleDebug`
- 실제 기기 또는 emulator 수동 테스트

## PR에 반드시 적을 내용

- 테스트 환경
- 통과/실패 항목
- 남은 이슈
