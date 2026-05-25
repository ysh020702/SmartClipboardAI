# T-030 DataItem/Topic 모델 감사

## 목적

`DataItem`, `DataItemEntity`, Topic 관련 모델 사용처를 확인해 cluster 필드 추가 전 영향 범위를 정리합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: docs/T-030-data-model-audit
- Depends on: T-020-architecture-baseline
- Blocked by: 아키텍처 baseline 미완료
- Ready criteria: T-020이 Done
- Can run in parallel with: T-040-navigation-baseline, T-050-permission-and-manifest-baseline
- Cannot run with: T-140-dataitem-cluster-fields

## 수정 허용 파일

- `docs/ARCHITECTURE.md`
- `docs/WORK_LOG.md`
- `docs/tasks/T-030-data-model-audit.md`

## 수정 금지 파일

- `app/src/main/java/com/samsung/smartclipboard/domain/model/`
- `app/src/main/java/com/samsung/smartclipboard/data/model/`
- `app/src/main/java/com/samsung/smartclipboard/data/source/local/`

## 구현 내용

- `DataItem`과 `DataItemEntity` 사용처를 검색합니다.
- Topic/Analysis/Action 모델 관계를 확인합니다.
- migration 필요 범위를 정리합니다.

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

- cluster 필드 추가 영향 범위가 문서화됩니다.

## 검증 방법

- `rg -n "DataItem|DataItemEntity|TopicAction|TopicAnalysis|TopicEntity" app/src/main/java`

## PR에 반드시 적을 내용

- 모델별 사용처
- migration 위험
- T-140 시작 가능 여부
