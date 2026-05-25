# T-040 Navigation/Main 화면 baseline

## 목적

Topic/Agent 중심 UX를 구현하기 전에 Main 화면과 navigation 책임을 문서로 확정합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: docs/T-040-navigation-baseline
- Depends on: T-020-architecture-baseline
- Blocked by: 아키텍처 baseline 미완료
- Ready criteria: T-020이 Done
- Can run in parallel with: T-030-data-model-audit, T-050-permission-and-manifest-baseline
- Cannot run with: T-200-home-ux-redesign, T-210-data-list-filter-selection, T-300-topic-create-flow

## 수정 허용 파일

- `docs/UX_FLOW.md`
- `docs/ARCHITECTURE.md`
- `docs/WORK_LOG.md`

## 수정 금지 파일

- `app/src/main/java/com/samsung/smartclipboard/presentation/main/`

## 구현 내용

- 화면 단위를 Home, Media Review, Data Browser, Topic Create, Topic Detail, Action Review로 나눕니다.
- `MainScreen.kt` 분리 기준을 정리합니다.

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

- UI 구현자가 화면별 task를 독립적으로 이해할 수 있습니다.

## 검증 방법

- `docs/UX_FLOW.md` 화면 단위와 `docs/IMPLEMENTATION_PLAN.md` task 의존성 비교

## PR에 반드시 적을 내용

- 확정한 화면 단위
- MainScreen 분리 제안
- 시작하면 안 되는 UI task
