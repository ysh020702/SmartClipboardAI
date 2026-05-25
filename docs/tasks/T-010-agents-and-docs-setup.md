# T-010 AGENTS와 문서 체계 정리

## 목적

여러 개발자가 GitHub에서 병렬 협업할 수 있도록 AGENTS, 계획 문서, task 문서, PR 템플릿을 정리합니다.

## 작업 상태
- Status: In Progress
- Owner: Codex
- Branch: docs/T-010-agents-and-docs-setup
- Depends on: none
- Blocked by: 프로젝트 오너 리뷰
- Ready criteria: 사용자 요청으로 문서 생성 허용
- Can run in parallel with: T-000-current-code-audit
- Cannot run with: 기능 구현 task

## 수정 허용 파일

- `AGENTS.md`
- `docs/PROJECT_SPEC.md`
- `docs/ARCHITECTURE.md`
- `docs/DATA_COLLECTION_STRATEGY.md`
- `docs/UX_FLOW.md`
- `docs/IMPLEMENTATION_PLAN.md`
- `docs/BRANCH_RULES.md`
- `docs/WORK_LOG.md`
- `docs/TASK_STATUS_GUIDE.md`
- `docs/tasks/`
- `.github/pull_request_template.md`

## 수정 금지 파일

- `app/` 하위 Kotlin/Android 구현 파일
- Gradle 파일
- Manifest 파일

## 구현 내용

- 협업 기준과 의존성 규칙을 문서화합니다.
- Phase별 task plan을 작성합니다.
- 각 task 문서를 생성합니다.
- PR 템플릿을 추가합니다.

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

- 사용자 요청의 필수 문서가 모두 존재합니다.
- task 상태 규칙이 문서화됩니다.
- Ready task만 추천할 수 있는 구조가 문서화됩니다.

## 검증 방법

- 필수 파일 존재 확인
- 필수 파일 존재와 금지 문구 검색을 실행합니다.

## PR에 반드시 적을 내용

- 생성/수정 문서 목록
- 구현 코드 수정 없음
- 다음 Ready task
- 사용자 리뷰가 필요한 항목
