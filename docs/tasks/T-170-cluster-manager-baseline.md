# T-170 ClusterManager baseline

## 목적

DataItem cluster 필드를 갱신하는 ClusterManager 계약과 MVP 갱신 방식을 구현합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-170-cluster-manager-baseline
- Depends on: T-140-dataitem-cluster-fields
- Blocked by: cluster 필드 migration 미완료
- Ready criteria: T-140이 Done
- Can run in parallel with: T-150-link-og-extractor
- Cannot run with: DB schema 변경 task

## 수정 허용 파일

- 신규 ClusterManager 관련 파일
- repository 연결부
- 관련 테스트 파일

## 수정 금지 파일

- UI 재설계
- Manifest
- Share/Tile flow

## 구현 내용

- 전체 재분석 또는 증분 갱신 중 MVP 방식을 확정해 구현합니다.
- 날짜 정보와 내용 유사도를 함께 고려합니다.

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

- DataItem의 cluster 필드가 갱신됩니다.

## 검증 방법

- 샘플 DataItem 목록으로 cluster update 확인
- `./gradlew test`

## PR에 반드시 적을 내용

- clustering 기준
- 전체 재분석/증분 처리 선택 이유
- 성능 우려
