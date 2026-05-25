# T-050 권한/Manifest baseline

## 목적

Share Target, Tile, MediaStore, SAF에 필요한 권한과 Manifest 변경 기준을 정리합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: docs/T-050-permission-and-manifest-baseline
- Depends on: T-020-architecture-baseline, T-030-data-model-audit
- Blocked by: 모델 감사 미완료
- Ready criteria: T-020과 T-030이 Done
- Can run in parallel with: none
- Cannot run with: T-100-share-target-flow, T-110-quick-tile-flow, T-120-media-store-batch-query

## 수정 허용 파일

- `docs/DATA_COLLECTION_STRATEGY.md`
- `docs/ARCHITECTURE.md`
- `docs/WORK_LOG.md`

## 수정 금지 파일

- `app/src/main/AndroidManifest.xml`

## 구현 내용

- 현재 Manifest와 필요한 Manifest 변경 후보를 비교합니다.
- Android 버전별 media 권한 정책을 정리합니다.

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

- 구현 task가 Manifest 수정 필요 여부를 판단할 수 있습니다.

## 검증 방법

- `AndroidManifest.xml` 읽기
- `docs/DATA_COLLECTION_STRATEGY.md` 권한 표 확인

## PR에 반드시 적을 내용

- 현재 권한
- 추가/수정 필요 권한
- 사전 승인 필요한 항목
