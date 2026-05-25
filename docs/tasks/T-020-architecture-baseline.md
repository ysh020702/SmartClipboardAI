# T-020 아키텍처 baseline 확정

## 목적

공통 모델, DB, Repository, Navigation, Theme, Manifest 수정 순서를 확정합니다.

## 작업 상태
- Status: Done
- Owner: Codex
- Branch: codex-T-020-architecture-baseline
- Depends on: T-000-current-code-audit, T-010-agents-and-docs-setup
- Blocked by: none
- Ready criteria: T-000과 T-010이 Done
- Can run in parallel with: none
- Cannot run with: DB/Repository/Navigation 구현 task

## 수정 허용 파일

- `docs/ARCHITECTURE.md`
- `docs/PROJECT_SPEC.md`
- `docs/WORK_LOG.md`

## 수정 금지 파일

- `app/` 하위 전체

## 구현 내용

- 현재 아키텍처와 목표 아키텍처 차이를 문서화합니다.
- 충돌 위험이 큰 파일의 순차 작업 순서를 확정합니다.
- 이후 구현 task의 dependency를 점검합니다.

## 상태 변경 영향

- `T-000-current-code-audit`와 `T-010-agents-and-docs-setup`이 프로젝트 오너 확인으로 Done 처리되어 이 작업을 시작합니다.
- 이 작업이 Done이 되기 전까지 `T-030`, `T-040`, `T-050`은 계속 Not Ready입니다.
- `T-220`은 `T-010` 완료로 Ready가 되었지만, 구현 task이므로 문서 baseline 확정 후 착수하는 것을 권장합니다.

## 체크리스트
- [x] 코드 읽기
- [x] 관련 문서 확인
- [x] 선행 task 완료 여부 확인
- [x] 구현
- [x] 빌드 확인 - 문서 전용 작업이라 Android 빌드는 실행하지 않음
- [x] 테스트/수동 확인
- [x] 변경 요약 작성
- [x] PR 작성 - 오너가 바로 다음 작업 진행을 지시해 문서 기준으로 완료 처리

## 완료 기준

- 공통 파일 수정 순서가 명확합니다.
- 병렬 가능한 작업과 순차 작업이 분리됩니다.

## 검증 방법

- `docs/ARCHITECTURE.md`와 `docs/IMPLEMENTATION_PLAN.md`의 phase 순서 비교

## PR에 반드시 적을 내용

- 확정한 순차 작업
- 병렬 허용 작업
- 남은 설계 결정
