# T-200 홈 화면 UX 재설계

## 목적

홈 화면을 수집 데이터 목록 중심이 아니라 Topic/Agent 초안 경험 중심으로 재설계합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-200-home-ux-redesign
- Depends on: T-040-navigation-baseline
- Blocked by: navigation baseline 미완료
- Ready criteria: T-040이 Done
- Can run in parallel with: T-220-save-feedback-bottom-sheet
- Cannot run with: T-210-data-list-filter-selection, T-300-topic-create-flow

## 수정 허용 파일

- 홈 화면 관련 `presentation/main/` 파일
- 필요한 preview/test 파일

## 수정 금지 파일

- DB schema
- Manifest
- Share/Tile handler
- Gemini/Agent 구현

## 구현 내용

- 첫 화면 CTA를 Topic 생성과 Agent 초안 검토로 맞춥니다.
- 새 후보 검토, 최근 Topic, 검토할 초안을 우선 노출합니다.
- 기존 One UI palette는 유지합니다.

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

- 홈에서 사용자가 다음 작업을 명확히 시작할 수 있습니다.

## 검증 방법

- Android Studio preview 또는 emulator 수동 확인
- `./gradlew assembleDebug`

## PR에 반드시 적을 내용

- UX 변경 요약
- 스크린샷
- 범위 밖 수정 없음
