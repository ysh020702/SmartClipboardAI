# Task Status Guide

이 문서는 Codex와 팀원이 task 상태를 해석하고 변경하는 기준입니다.

## 상태 정의

### Not Ready

아직 기획, 구조, 선행 작업이 불명확해서 시작하면 안 되는 상태입니다.

예시:

- DB 모델이 확정되지 않았는데 UI가 해당 필드를 사용해야 함
- Navigation baseline이 없는데 새 화면 구현이 필요함
- Manifest 변경 승인이 없는데 permission 작업을 시작하려 함

### Ready

지금 바로 작업 가능한 상태입니다.

조건:

- 모든 `Depends on` task가 `Done`
- owner가 비어 있거나 작업자가 배정 가능
- 수정 허용 파일 범위가 명확함
- 다른 `In Progress` task와 editable files가 겹치지 않음
- 완료 기준과 검증 방법이 명확함

### In Progress

누군가 작업 중인 상태입니다.

규칙:

- 다른 작업자는 같은 task를 시작하지 않습니다.
- 같은 파일을 수정해야 하는 task는 시작하지 않습니다.
- 작업자는 자기 task 문서의 체크리스트만 갱신합니다.

### Blocked

선행 task, 기술 결정, 권한, 외부 API, 설계 충돌 때문에 진행할 수 없는 상태입니다.

Blocked일 때 기록할 정보:

- 막힌 이유
- 필요한 결정
- 기다리는 task id
- 임시 우회 여부
- 누가 unblock할 수 있는지

### Done

완료된 상태입니다.

Done 기준:

- task의 완료 기준 충족
- 검증 방법 수행
- 변경 파일과 테스트 결과 정리
- PR 리뷰 완료 및 merge
- 프로젝트 오너가 최종 확인

## 상태를 변경할 수 있는 사람

- `Not Ready -> Ready`: 프로젝트 오너 또는 phase 책임자
- `Ready -> In Progress`: task owner
- `In Progress -> Blocked`: task owner
- `Blocked -> Ready`: blocker를 해결한 프로젝트 오너 또는 phase 책임자
- `In Progress -> Done`: PR merge 후 프로젝트 오너
- `Done -> 다른 상태`: 프로젝트 오너만 가능

## Ready로 바꾸기 위한 기준

1. 선행 task가 모두 `Done`입니다.
2. 수정 허용 파일이 명확합니다.
3. 공통 파일 수정 승인 여부가 명확합니다.
4. 완료 기준이 확인 가능합니다.
5. 검증 방법이 실행 가능합니다.
6. 병렬 충돌 위험이 문서화되어 있습니다.

## 팀원이 "나 뭐 해야 해?"라고 물었을 때 Codex 판단 방식

Codex는 아래 순서로 답합니다.

1. `docs/IMPLEMENTATION_PLAN.md`와 `docs/tasks/`에서 `Ready` task를 찾습니다.
2. `In Progress`, `Blocked`, `Not Ready`, `Done` task는 추천하지 않습니다.
3. Ready task가 있으면 지금 가능한 작업으로 제시합니다.
4. Not Ready/Blocked task가 왜 시작 불가인지 설명합니다.
5. Ready task가 하나도 없으면 새 task를 만들지 않고 blocker를 보고합니다.

답변 형식:

- 지금 가능한 작업
- 아직 시작하면 안 되는 작업과 이유
- 추천하는 다음 작업
