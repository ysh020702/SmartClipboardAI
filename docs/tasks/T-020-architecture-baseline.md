# T-020 아키텍처 baseline 확정

## 목적

공통 모델, DB, Repository, Navigation, Theme, Manifest 수정 순서를 확정합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: docs/T-020-architecture-baseline
- Depends on: T-000-current-code-audit, T-010-agents-and-docs-setup
- Blocked by: 코드 감사와 문서 체계 리뷰 미완료
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

- 공통 파일 수정 순서가 명확합니다.
- 병렬 가능한 작업과 순차 작업이 분리됩니다.

## 검증 방법

- `docs/ARCHITECTURE.md`와 `docs/IMPLEMENTATION_PLAN.md`의 phase 순서 비교

## PR에 반드시 적을 내용

- 확정한 순차 작업
- 병렬 허용 작업
- 남은 설계 결정
