# Tasks

이 폴더의 canonical task 문서는 `T-XXX-*.md` 형식을 사용합니다.

## 읽는 순서

1. `AGENTS.md`
2. `docs/TASK_STATUS_GUIDE.md`
3. `docs/IMPLEMENTATION_PLAN.md`
4. 담당 `docs/tasks/T-*.md`

## 상태 규칙

- `Ready`인 task만 시작할 수 있습니다.
- `Not Ready`는 선행 작업이 끝나기 전까지 시작하지 않습니다.
- `In Progress`는 이미 누군가 작업 중입니다.
- `Blocked`는 blocker가 해결될 때까지 시작하지 않습니다.
- `Done`은 완료된 작업이므로 새 승인 없이 수정하지 않습니다.

## 작업자 규칙

- 자기 task 문서의 체크리스트만 수정합니다.
- 다른 task 문서의 상태를 임의로 바꾸지 않습니다.
- `docs/IMPLEMENTATION_PLAN.md`의 전체 상태 변경은 프로젝트 오너가 관리합니다.
- 공통 파일 수정이 필요하면 PR 전에 사유를 문서화하고 승인을 받습니다.

## 현재 Ready task

- `T-000-current-code-audit`

`T-010-agents-and-docs-setup`은 현재 문서 작성 작업으로 `In Progress`입니다. 구현 task는 아직 시작하지 않습니다.
