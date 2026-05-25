# T-140 DataItem cluster 필드와 Room migration

## 목적

`DataItem`과 `DataItemEntity`에 cluster 정보를 추가하고 Room migration을 작성합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-140-dataitem-cluster-fields
- Depends on: T-030-data-model-audit
- Blocked by: 모델 감사 미완료
- Ready criteria: T-030이 Done
- Can run in parallel with: none
- Cannot run with: 모든 DB/Repository/모델 수정 task

## 수정 허용 파일

- `app/src/main/java/com/samsung/smartclipboard/domain/model/DataItem.kt`
- `app/src/main/java/com/samsung/smartclipboard/data/model/DataItemEntity.kt`
- `app/src/main/java/com/samsung/smartclipboard/data/source/local/SmartClipboardDatabase.kt`
- `app/src/main/java/com/samsung/smartclipboard/data/repository/DataRepositoryImpl.kt`
- 관련 테스트 파일

## 수정 금지 파일

- UI 재설계 파일
- Manifest
- Gradle

## 구현 내용

- `clusterId`, `clusterLabel`, `clusterConfidence` nullable 필드를 추가합니다.
- Room database version과 migration을 추가합니다.
- entity/domain mapper를 갱신합니다.

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

- 기존 데이터는 cluster 필드 null로 유지됩니다.
- build가 통과합니다.

## 검증 방법

- mapper 테스트
- migration 테스트
- `./gradlew test`
- `./gradlew assembleDebug`

## PR에 반드시 적을 내용

- DB version 변경
- migration 설명
- 영향받는 mapper
