# T-030 DataItem/Topic 모델 감사

## 목적

`DataItem`, `DataItemEntity`, Topic 관련 모델 사용처를 확인해 cluster 필드 추가 전 영향 범위를 정리합니다.

## 작업 상태
- Status: In Progress
- Owner: Codex
- Branch: codex-T-030-data-model-audit
- Depends on: T-020-architecture-baseline
- Blocked by: none
- Ready criteria: T-020이 Done
- Can run in parallel with: none
- Cannot run with: T-040-navigation-baseline, T-050-permission-and-manifest-baseline, T-140-dataitem-cluster-fields

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

## 감사 결과 요약

- `DataItem`과 `DataItemEntity`는 현재 `id`, `type`, `content`, `title`, `source`, `mimeType`, `createdAt`만 가집니다.
- `DataItemType`은 `TEXT`, `LINK`, `IMAGE`, `FILE`, `SCREENSHOT`이며 cluster와 직접 연결된 enum은 없습니다.
- `DataRepositoryImpl`은 `DataItemEntity` 생성과 `DataItem` mapping을 모두 담당하므로 cluster 필드 추가 시 함께 수정해야 합니다.
- `DataItemDao.observeAll()`과 `TopicDao.observeItemsForTopic()`은 `SELECT *`를 사용하므로 nullable column 추가 자체는 query 구조와 충돌하지 않습니다.
- `TopicItemCrossRefEntity`는 `DataItemEntity.id`만 참조하므로 cluster 필드 추가와 직접 충돌하지 않습니다.
- `TopicAnalysisEntity.sourceItemIds`와 `AiProposalEntity.itemIds`는 ID 문자열을 저장하므로 cluster 필드와 직접 충돌하지 않지만, 장기적으로 별도 정규화 검토 대상입니다.
- `MainViewModel`의 필터, prompt 후보 검색, action 추천은 `DataItem` 필드를 읽으므로 cluster label/source를 검색 대상으로 넣을지 T-140 이후 결정해야 합니다.

## T-140 권장 변경 범위

- `DataItem.kt`: nullable cluster 필드를 뒤쪽에 기본값과 함께 추가합니다.
- `DataItemEntity.kt`: nullable Room column을 추가합니다.
- `SmartClipboardDatabase.kt`: database version을 올리고 `MIGRATION_4_5`를 추가합니다.
- `AppModule.kt`: 새 migration을 `.addMigrations(...)`에 연결합니다.
- `DataRepositoryImpl.kt`: entity/domain mapping과 cluster update API 연결부를 수정합니다.
- `DataItemDao.kt`: cluster field update query를 추가합니다.

권장 후보 필드:

- `clusterId: String?`
- `clusterLabel: String?`
- `clusterScore: Float?`
- `clusterUpdatedAt: Long?`

이 필드는 MVP에서 별도 Cluster table을 만들지 않는다는 결정과 맞습니다.

## 다음 task 상태 영향

- 이 감사가 오너 확인 후 Done이 되면 `T-140-dataitem-cluster-fields`가 Ready 후보가 됩니다.
- `T-040`, `T-050`은 공통 모델 감사가 끝날 때까지 Not Ready로 유지합니다.
- `T-220`은 이미 Ready지만 UI 구현 task이므로 공통 모델/Navigation/Manifest baseline과 충돌하지 않는지 시작 전 다시 확인해야 합니다.

## 체크리스트
- [x] 코드 읽기
- [x] 관련 문서 확인
- [x] 선행 task 완료 여부 확인
- [x] 구현
- [x] 빌드 확인 - 문서 전용 작업이라 Android 빌드는 실행하지 않음
- [x] 테스트/수동 확인
- [x] 변경 요약 작성
- [ ] PR 작성

## 완료 기준

- cluster 필드 추가 영향 범위가 문서화됩니다.

## 검증 방법

- `rg -n "DataItem|DataItemEntity|TopicAction|TopicAnalysis|TopicEntity" app/src/main/java`

## PR에 반드시 적을 내용

- 모델별 사용처
- migration 위험
- T-140 시작 가능 여부
