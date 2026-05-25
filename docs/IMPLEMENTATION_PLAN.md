# Implementation Plan

이 문서는 전체 task의 기준표입니다. 전체 완료 체크는 프로젝트 오너가 관리합니다. 각 작업자는 자기 task 문서만 체크합니다.

## Phase 0: 현재 코드 감사 및 문서 기반 정리

### T-000-current-code-audit

- task id: `T-000-current-code-audit`
- 작업명: 현재 코드 감사
- Status: `Ready`
- Owner: `Unassigned`
- 목적: 기존 구현 중 유지/재사용/수정/제거 후보를 파일 단위로 정리합니다.
- 담당 브랜치명: `docs/T-000-current-code-audit`
- 예상 수정 파일: `docs/ARCHITECTURE.md`, `docs/WORK_LOG.md`, `docs/tasks/T-000-current-code-audit.md`
- 선행 task: 없음
- Blocked by: 없음
- Ready criteria: 현재 문서 구조가 존재하고 코드 읽기만 수행합니다.
- 병렬 진행 가능 여부: 가능
- Can run in parallel with: 없음
- Cannot run with: 공통 코드 수정 task
- 충돌 가능성이 있는 파일: `docs/ARCHITECTURE.md`, `docs/WORK_LOG.md`
- 완료 기준: 재사용 가능한 클래스와 없는 클래스가 문서에 반영됩니다.
- 검증 방법: `rg`로 클래스 존재 여부를 확인하고 문서 링크를 갱신합니다.
- 작업자가 수정해도 되는 파일 범위: 위 예상 수정 파일
- 수정하면 안 되는 파일 범위: Android 코드 전체
- 관련 task 문서 경로: `docs/tasks/T-000-current-code-audit.md`

### T-010-agents-and-docs-setup

- task id: `T-010-agents-and-docs-setup`
- 작업명: AGENTS와 협업 문서 체계 정리
- Status: `In Progress`
- Owner: `Codex`
- 목적: 병렬 협업을 위한 기준 문서와 task 문서를 생성합니다.
- 담당 브랜치명: `docs/T-010-agents-and-docs-setup`
- 예상 수정 파일: `AGENTS.md`, `docs/*.md`, `docs/tasks/*.md`, `.github/pull_request_template.md`
- 선행 task: 없음
- Blocked by: 프로젝트 오너 리뷰
- Ready criteria: 사용자 요청으로 승인됨
- 병렬 진행 가능 여부: 제한적 가능
- Can run in parallel with: `T-000-current-code-audit`
- Cannot run with: 모든 구현 task
- 충돌 가능성이 있는 파일: `AGENTS.md`, `docs/IMPLEMENTATION_PLAN.md`
- 완료 기준: 요청된 필수 문서가 생성되고 task 상태 체계가 정리됩니다.
- 검증 방법: 필수 파일 존재 확인, 금지 문구 검색
- 작업자가 수정해도 되는 파일 범위: 문서 파일
- 수정하면 안 되는 파일 범위: Android 코드
- 관련 task 문서 경로: `docs/tasks/T-010-agents-and-docs-setup.md`

### T-020-architecture-baseline

- task id: `T-020-architecture-baseline`
- 작업명: 아키텍처 baseline 확정
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: MVVM, Repository, DB, Navigation, Theme 수정 순서를 확정합니다.
- 담당 브랜치명: `docs/T-020-architecture-baseline`
- 예상 수정 파일: `docs/ARCHITECTURE.md`, `docs/PROJECT_SPEC.md`
- 선행 task: `T-000-current-code-audit`, `T-010-agents-and-docs-setup`
- Blocked by: 문서 체계 승인 및 코드 감사 결과
- Ready criteria: 선행 task가 Done
- 병렬 진행 가능 여부: 제한적 가능
- Can run in parallel with: 없음
- Cannot run with: DB/Repository/Navigation 구현 task
- 충돌 가능성이 있는 파일: `docs/ARCHITECTURE.md`
- 완료 기준: 공통 영역 수정 순서가 확정됩니다.
- 검증 방법: 공통 파일 목록과 phase 순서가 문서에 일치하는지 확인
- 작업자가 수정해도 되는 파일 범위: architecture/spec 문서
- 수정하면 안 되는 파일 범위: Android 코드
- 관련 task 문서 경로: `docs/tasks/T-020-architecture-baseline.md`

## Phase 1: 공통 기반 정리

### T-030-data-model-audit

- task id: `T-030-data-model-audit`
- 작업명: DataItem/Topic 모델 감사
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: DataItem cluster 필드 추가 전 usage와 migration 영향 범위를 정리합니다.
- 담당 브랜치명: `docs/T-030-data-model-audit`
- 예상 수정 파일: `docs/ARCHITECTURE.md`, `docs/tasks/T-030-data-model-audit.md`
- 선행 task: `T-020-architecture-baseline`
- Blocked by: 아키텍처 baseline 미확정
- Ready criteria: `T-020` Done
- 병렬 진행 가능 여부: 불가
- Can run in parallel with: 없음
- Cannot run with: `T-040-navigation-baseline`, DB 구현 task
- 충돌 가능성이 있는 파일: 모델/DB 문서
- 완료 기준: 모델 변경 영향 범위가 문서화됩니다.
- 검증 방법: `DataItem`, `DataItemEntity`, DAO, Repository usage 검색 결과 기록
- 작업자가 수정해도 되는 파일 범위: 문서
- 수정하면 안 되는 파일 범위: Kotlin 코드
- 관련 task 문서 경로: `docs/tasks/T-030-data-model-audit.md`

### T-040-navigation-baseline

- task id: `T-040-navigation-baseline`
- 작업명: Navigation/Main 화면 baseline
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: Topic/Agent 중심 화면 구조를 구현 전 문서로 확정합니다.
- 담당 브랜치명: `docs/T-040-navigation-baseline`
- 예상 수정 파일: `docs/UX_FLOW.md`, `docs/ARCHITECTURE.md`
- 선행 task: `T-020-architecture-baseline`
- Blocked by: 아키텍처 baseline 미확정
- Ready criteria: `T-020` Done
- 병렬 진행 가능 여부: 제한적 가능
- Can run in parallel with: `T-030-data-model-audit`
- Cannot run with: `T-200-home-ux-redesign`, `T-300-topic-create-flow`
- 충돌 가능성이 있는 파일: `presentation/main` 관련 문서
- 완료 기준: 화면 단위와 navigation 책임이 정리됩니다.
- 검증 방법: UX flow와 architecture 문서 간 용어 일치 확인
- 작업자가 수정해도 되는 파일 범위: 문서
- 수정하면 안 되는 파일 범위: Kotlin 코드
- 관련 task 문서 경로: `docs/tasks/T-040-navigation-baseline.md`

### T-050-permission-and-manifest-baseline

- task id: `T-050-permission-and-manifest-baseline`
- 작업명: 권한/Manifest baseline
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: Share, Tile, MediaStore, SAF에 필요한 Manifest/권한 변경을 정리합니다.
- 담당 브랜치명: `docs/T-050-permission-and-manifest-baseline`
- 예상 수정 파일: `docs/DATA_COLLECTION_STRATEGY.md`, `docs/ARCHITECTURE.md`
- 선행 task: `T-020-architecture-baseline`
- Blocked by: 아키텍처 baseline 미확정
- Ready criteria: `T-020` Done
- 병렬 진행 가능 여부: 제한적 가능
- Can run in parallel with: `T-030-data-model-audit`
- Cannot run with: `T-100-share-target-flow`, `T-110-quick-tile-flow`, `T-120-media-store-batch-query`
- 충돌 가능성이 있는 파일: `AndroidManifest.xml` 관련 문서
- 완료 기준: 권한과 Manifest 변경 승인 기준이 명확합니다.
- 검증 방법: Android API 제약과 현재 Manifest 비교 기록
- 작업자가 수정해도 되는 파일 범위: 문서
- 수정하면 안 되는 파일 범위: `AndroidManifest.xml`
- 관련 task 문서 경로: `docs/tasks/T-050-permission-and-manifest-baseline.md`

## Phase 2: 데이터 수집 흐름 구현/정리

### T-100-share-target-flow

- task id: `T-100-share-target-flow`
- 작업명: Share Target 수집 흐름 정리
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: 기존 ShareReceiver 기반 저장 흐름을 안정화하고 피드백 UX와 실패 처리를 개선합니다.
- 담당 브랜치명: `feat/T-100-share-target-flow`
- 예상 수정 파일: `presentation/share/`, `data/source/share/`, 관련 QA 문서
- 선행 task: `T-050-permission-and-manifest-baseline`
- Blocked by: Manifest baseline 미확정
- Ready criteria: `T-050` Done
- 병렬 진행 가능 여부: 가능
- Can run in parallel with: `T-110-quick-tile-flow`, `T-130-storage-access-framework-picker`
- Cannot run with: Manifest 수정 task
- 충돌 가능성이 있는 파일: `AndroidManifest.xml`, share handler
- 완료 기준: 텍스트/링크/이미지/파일 공유 수신과 실패 처리가 확인됩니다.
- 검증 방법: 수동 Share Sheet 테스트
- 작업자가 수정해도 되는 파일 범위: task 문서의 share 관련 파일
- 수정하면 안 되는 파일 범위: DB schema, Navigation, Gradle
- 관련 task 문서 경로: `docs/tasks/T-100-share-target-flow.md`

### T-110-quick-tile-flow

- task id: `T-110-quick-tile-flow`
- 작업명: Quick Settings Tile 클립보드 흐름 정리
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: Tile + 투명 Activity 기반 최근 클립보드 저장을 안정화합니다.
- 담당 브랜치명: `feat/T-110-quick-tile-flow`
- 예상 수정 파일: `presentation/tile/`, `presentation/clipboard/`, `data/source/clipboard/`
- 선행 task: `T-050-permission-and-manifest-baseline`
- Blocked by: Manifest baseline 미확정
- Ready criteria: `T-050` Done
- 병렬 진행 가능 여부: 가능
- Can run in parallel with: `T-100-share-target-flow`
- Cannot run with: Manifest 수정 task
- 충돌 가능성이 있는 파일: `AndroidManifest.xml`, clipboard handler
- 완료 기준: 복사 후 Tile 클릭 저장과 중복/빈 클립보드 처리가 확인됩니다.
- 검증 방법: 실제 기기 또는 emulator Tile 수동 테스트
- 작업자가 수정해도 되는 파일 범위: tile/clipboard 관련 파일
- 수정하면 안 되는 파일 범위: DB schema, Gradle
- 관련 task 문서 경로: `docs/tasks/T-110-quick-tile-flow.md`

### T-120-media-store-batch-query

- task id: `T-120-media-store-batch-query`
- 작업명: Last Sync Time 기반 MediaStore Batch Query
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: 새 이미지/스크린샷 후보를 찾고 검토 후 저장할 기반을 만듭니다.
- 담당 브랜치명: `feat/T-120-media-store-batch-query`
- 예상 수정 파일: `data/source/media/`, `presentation/main/`, QA 문서
- 선행 task: `T-050-permission-and-manifest-baseline`, `T-040-navigation-baseline`
- Blocked by: 권한/화면 baseline 미완료
- Ready criteria: `T-050`, `T-040` Done
- 병렬 진행 가능 여부: 제한적 가능
- Can run in parallel with: `T-130-storage-access-framework-picker`
- Cannot run with: `T-200-home-ux-redesign`
- 충돌 가능성이 있는 파일: `MainViewModel`, `MainScreen`, media handler
- 완료 기준: Last Sync Time 이후 후보 조회와 중복 방지가 확인됩니다.
- 검증 방법: MediaStore 수동 테스트, Android 14 부분 접근 확인
- 작업자가 수정해도 되는 파일 범위: media 관련 파일과 지정 UI 파일
- 수정하면 안 되는 파일 범위: Share/Tile, Gemini, DB schema
- 관련 task 문서 경로: `docs/tasks/T-120-media-store-batch-query.md`

### T-130-storage-access-framework-picker

- task id: `T-130-storage-access-framework-picker`
- 작업명: Storage Access Framework 파일 선택
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: 사용자가 직접 사진/파일을 선택해 저장하는 보조 수집 경로를 추가합니다.
- 담당 브랜치명: `feat/T-130-storage-access-framework-picker`
- 예상 수정 파일: `presentation/main/`, 신규 picker 관련 파일
- 선행 task: `T-040-navigation-baseline`
- Blocked by: 화면 baseline 미완료
- Ready criteria: `T-040` Done
- 병렬 진행 가능 여부: 가능
- Can run in parallel with: `T-100-share-target-flow`, `T-110-quick-tile-flow`
- Cannot run with: `T-200-home-ux-redesign`
- 충돌 가능성이 있는 파일: `MainActivity`, `MainScreen`
- 완료 기준: 사용자가 직접 파일을 선택하고 DataItem 후보로 저장할 수 있습니다.
- 검증 방법: 이미지/PDF/취소 시나리오 수동 확인
- 작업자가 수정해도 되는 파일 범위: picker 관련 UI와 handler
- 수정하면 안 되는 파일 범위: DB schema, Gemini
- 관련 task 문서 경로: `docs/tasks/T-130-storage-access-framework-picker.md`

## Phase 3: 데이터 저장/전처리/클러스터링 연결

### T-140-dataitem-cluster-fields

- task id: `T-140-dataitem-cluster-fields`
- 작업명: DataItem cluster 필드와 Room migration
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: README 결정대로 cluster 정보를 DataItem 필드로 추가합니다.
- 담당 브랜치명: `feat/T-140-dataitem-cluster-fields`
- 예상 수정 파일: `DataItem.kt`, `DataItemEntity.kt`, `SmartClipboardDatabase.kt`, `DataRepositoryImpl.kt`
- 선행 task: `T-030-data-model-audit`
- Blocked by: 모델 감사 미완료
- Ready criteria: `T-030` Done
- 병렬 진행 가능 여부: 불가
- Can run in parallel with: 없음
- Cannot run with: 모든 DB/Repository/모델 task
- 충돌 가능성이 있는 파일: 모델, DB, repository
- 완료 기준: nullable cluster 필드와 migration이 동작합니다.
- 검증 방법: unit/migration test, `assembleDebug`
- 작업자가 수정해도 되는 파일 범위: 지정 모델/DB/repository 파일
- 수정하면 안 되는 파일 범위: UI 재설계 파일
- 관련 task 문서 경로: `docs/tasks/T-140-dataitem-cluster-fields.md`

### T-150-link-og-extractor

- task id: `T-150-link-og-extractor`
- 작업명: 링크 OG 태그 추출
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: 링크 DataItem에 제목/설명/이미지 preview metadata를 보강합니다.
- 담당 브랜치명: `feat/T-150-link-og-extractor`
- 예상 수정 파일: 신규 WebExtractor, AI/data module, repository 연결부
- 선행 task: `T-140-dataitem-cluster-fields`
- Blocked by: DataItem metadata 방향 미확정
- Ready criteria: `T-140` Done
- 병렬 진행 가능 여부: 가능
- Can run in parallel with: `T-160-local-ocr-processor`
- Cannot run with: Repository interface 변경 task
- 충돌 가능성이 있는 파일: DI module, repository
- 완료 기준: Jsoup/네트워크 작업이 IO dispatcher에서 수행됩니다.
- 검증 방법: 성공/실패 URL 테스트
- 작업자가 수정해도 되는 파일 범위: link extractor 관련 파일
- 수정하면 안 되는 파일 범위: UI 대형 리팩토링
- 관련 task 문서 경로: `docs/tasks/T-150-link-og-extractor.md`

### T-160-local-ocr-processor

- task id: `T-160-local-ocr-processor`
- 작업명: 로컬 OCRProcessor 연동
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: 이미지/스크린샷 OCR 결과를 Agent 입력으로 사용할 수 있게 연결합니다.
- 담당 브랜치명: `feat/T-160-local-ocr-processor`
- 예상 수정 파일: 신규 OCRProcessor, media/data layer, repository 연결부
- 선행 task: `T-140-dataitem-cluster-fields`, `T-120-media-store-batch-query`
- Blocked by: DataItem metadata와 media 후보 저장 흐름 미완료
- Ready criteria: `T-140`, `T-120` Done
- 병렬 진행 가능 여부: 가능
- Can run in parallel with: `T-150-link-og-extractor`
- Cannot run with: media 저장 구조 변경 task
- 충돌 가능성이 있는 파일: media handler, repository
- 완료 기준: OCR 성공/실패가 DataItem 흐름에 안전하게 반영됩니다.
- 검증 방법: 샘플 이미지 수동 확인, 실패 케이스 확인
- 작업자가 수정해도 되는 파일 범위: OCR 관련 파일
- 수정하면 안 되는 파일 범위: Share/Tile, Navigation
- 관련 task 문서 경로: `docs/tasks/T-160-local-ocr-processor.md`

### T-170-cluster-manager-baseline

- task id: `T-170-cluster-manager-baseline`
- 작업명: ClusterManager baseline
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: DataItem cluster 필드를 갱신하는 로컬/AI 기반 cluster manager 계약을 정의합니다.
- 담당 브랜치명: `feat/T-170-cluster-manager-baseline`
- 예상 수정 파일: 신규 cluster manager, repository 연결부
- 선행 task: `T-140-dataitem-cluster-fields`
- Blocked by: cluster 필드 migration 미완료
- Ready criteria: `T-140` Done
- 병렬 진행 가능 여부: 제한적 가능
- Can run in parallel with: `T-150-link-og-extractor`
- Cannot run with: DB schema 변경 task
- 충돌 가능성이 있는 파일: repository, model
- 완료 기준: 전체 재분석과 증분 갱신 중 MVP 방식이 구현됩니다.
- 검증 방법: 샘플 DataItem cluster update 테스트
- 작업자가 수정해도 되는 파일 범위: cluster 관련 파일
- 수정하면 안 되는 파일 범위: UI 재설계
- 관련 task 문서 경로: `docs/tasks/T-170-cluster-manager-baseline.md`

## Phase 4: 홈 화면 및 데이터 리스트 UX 재설계

### T-200-home-ux-redesign

- task id: `T-200-home-ux-redesign`
- 작업명: 홈 화면 UX 재설계
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: 홈을 Topic/Agent 초안 경험 중심으로 재구성합니다.
- 담당 브랜치명: `feat/T-200-home-ux-redesign`
- 예상 수정 파일: `presentation/main/` 화면 파일
- 선행 task: `T-040-navigation-baseline`
- Blocked by: navigation baseline 미완료
- Ready criteria: `T-040` Done
- 병렬 진행 가능 여부: 제한적 가능
- Can run in parallel with: `T-220-save-feedback-bottom-sheet`
- Cannot run with: `T-210-data-list-filter-selection`, navigation 공통 수정
- 충돌 가능성이 있는 파일: `MainScreen.kt`, `MainContract.kt`
- 완료 기준: 첫 화면 CTA가 Topic/Agent 중심입니다.
- 검증 방법: Compose preview 또는 수동 실행 확인
- 작업자가 수정해도 되는 파일 범위: 홈 UI 관련 파일
- 수정하면 안 되는 파일 범위: DB, Manifest, Gradle
- 관련 task 문서 경로: `docs/tasks/T-200-home-ux-redesign.md`

### T-210-data-list-filter-selection

- task id: `T-210-data-list-filter-selection`
- 작업명: 데이터 리스트/필터/선택 UX
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: 수집 데이터 탐색과 선택을 Topic 생성의 보조 흐름으로 정리합니다.
- 담당 브랜치명: `feat/T-210-data-list-filter-selection`
- 예상 수정 파일: `presentation/main/` 데이터 리스트 파일
- 선행 task: `T-040-navigation-baseline`
- Blocked by: navigation baseline 미완료
- Ready criteria: `T-040` Done
- 병렬 진행 가능 여부: 제한적 가능
- Can run in parallel with: `T-220-save-feedback-bottom-sheet`
- Cannot run with: `T-200-home-ux-redesign`
- 충돌 가능성이 있는 파일: `MainScreen.kt`, selection state
- 완료 기준: 타입/날짜 필터와 선택 유지가 동작합니다.
- 검증 방법: 수동 필터/선택 테스트
- 작업자가 수정해도 되는 파일 범위: 데이터 리스트 UI
- 수정하면 안 되는 파일 범위: DB, Manifest
- 관련 task 문서 경로: `docs/tasks/T-210-data-list-filter-selection.md`

### T-220-save-feedback-bottom-sheet

- task id: `T-220-save-feedback-bottom-sheet`
- 작업명: 저장 피드백 바텀시트 UX
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: Share/Tile 저장 피드백을 작고 일관된 UI로 개선합니다.
- 담당 브랜치명: `feat/T-220-save-feedback-bottom-sheet`
- 예상 수정 파일: `presentation/share/`, `presentation/clipboard/`
- 선행 task: `T-010-agents-and-docs-setup`
- Blocked by: 문서 기준 리뷰
- Ready criteria: `T-010` Done
- 병렬 진행 가능 여부: 가능
- Can run in parallel with: `T-200-home-ux-redesign`, `T-210-data-list-filter-selection`
- Cannot run with: `T-100-share-target-flow`, `T-110-quick-tile-flow`
- 충돌 가능성이 있는 파일: share/clipboard feedback screen
- 완료 기준: Share와 Tile 피드백이 시각적으로 일관됩니다.
- 검증 방법: Share/Tile 수동 확인
- 작업자가 수정해도 되는 파일 범위: 피드백 UI
- 수정하면 안 되는 파일 범위: DB, Repository
- 관련 task 문서 경로: `docs/tasks/T-220-save-feedback-bottom-sheet.md`

## Phase 5: Topic 생성 및 데이터 선택 UX

### T-300-topic-create-flow

- task id: `T-300-topic-create-flow`
- 작업명: Topic 생성 플로우
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: 사용자가 주제를 입력하고 Topic을 확정하는 흐름을 구현합니다.
- 담당 브랜치명: `feat/T-300-topic-create-flow`
- 예상 수정 파일: `presentation/main/`, Topic 관련 UI 파일
- 선행 task: `T-040-navigation-baseline`, `T-140-dataitem-cluster-fields`
- Blocked by: navigation/model 미완료
- Ready criteria: `T-040`, `T-140` Done
- 병렬 진행 가능 여부: 제한적 가능
- Can run in parallel with: 없음
- Cannot run with: `T-310-topic-data-selection-flow`, `T-200-home-ux-redesign`
- 충돌 가능성이 있는 파일: Topic UI, MainViewModel
- 완료 기준: 사용자가 새 Topic을 만들 수 있습니다.
- 검증 방법: 수동 Topic 생성 테스트
- 작업자가 수정해도 되는 파일 범위: Topic 생성 UI
- 수정하면 안 되는 파일 범위: Share/Tile/Media
- 관련 task 문서 경로: `docs/tasks/T-300-topic-create-flow.md`

### T-310-topic-data-selection-flow

- task id: `T-310-topic-data-selection-flow`
- 작업명: Topic 데이터 선택 플로우
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: 사용자가 DataItem을 직접 선택하거나 Gemini 후보를 검토해 Topic에 붙입니다.
- 담당 브랜치명: `feat/T-310-topic-data-selection-flow`
- 예상 수정 파일: `presentation/main/`, topic selection state
- 선행 task: `T-300-topic-create-flow`, `T-210-data-list-filter-selection`
- Blocked by: Topic 생성과 데이터 리스트 미완료
- Ready criteria: `T-300`, `T-210` Done
- 병렬 진행 가능 여부: 제한적 가능
- Can run in parallel with: 없음
- Cannot run with: Topic 생성/데이터 리스트 동시 수정
- 충돌 가능성이 있는 파일: MainViewModel, selection state
- 완료 기준: 선택한 DataItem이 Topic에 연결됩니다.
- 검증 방법: 필터 후 선택 유지와 Topic 연결 확인
- 작업자가 수정해도 되는 파일 범위: Topic selection UI
- 수정하면 안 되는 파일 범위: DB schema
- 관련 task 문서 경로: `docs/tasks/T-310-topic-data-selection-flow.md`

## Phase 6: AI Agent 분석/초안 생성 흐름

### T-400-topic-analysis-draft

- task id: `T-400-topic-analysis-draft`
- 작업명: TopicAnalysis 초안 생성
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: Gemini Agent가 Topic과 DataItem을 분석해 TopicAnalysis를 생성합니다.
- 담당 브랜치명: `feat/T-400-topic-analysis-draft`
- 예상 수정 파일: `domain/ai/`, `data/ai/`, repository 연결부
- 선행 task: `T-310-topic-data-selection-flow`, `T-150-link-og-extractor`, `T-160-local-ocr-processor`
- Blocked by: Topic/DataItem/enrichment 흐름 미완료
- Ready criteria: 선행 task 모두 Done
- 병렬 진행 가능 여부: 제한적 가능
- Can run in parallel with: 없음
- Cannot run with: Agent contract/model 변경 task
- 충돌 가능성이 있는 파일: AI module, repository
- 완료 기준: TopicAnalysis 초안이 생성되고 저장됩니다.
- 검증 방법: fake Gemini 또는 테스트 입력으로 결과 확인
- 작업자가 수정해도 되는 파일 범위: Agent 분석 관련 파일
- 수정하면 안 되는 파일 범위: UI 대형 리팩토링
- 관련 task 문서 경로: `docs/tasks/T-400-topic-analysis-draft.md`

### T-410-topic-action-draft

- task id: `T-410-topic-action-draft`
- 작업명: TopicAction 초안 생성
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: 분석 결과를 요약/일정/리마인더/공유/TODO 초안으로 변환합니다.
- 담당 브랜치명: `feat/T-410-topic-action-draft`
- 예상 수정 파일: `domain/ai/`, `data/ai/`, `domain/model/TopicAction.kt`, repository 연결부
- 선행 task: `T-400-topic-analysis-draft`
- Blocked by: TopicAnalysis 생성 미완료
- Ready criteria: `T-400` Done
- 병렬 진행 가능 여부: 제한적 가능
- Can run in parallel with: `T-500-calendar-intent-draft`, `T-510-notes-share-draft`는 contract가 고정된 후 가능
- Cannot run with: TopicAction model 변경 task
- 충돌 가능성이 있는 파일: TopicAction model, repository
- 완료 기준: 여러 타입의 TopicAction이 DRAFT 상태로 생성됩니다.
- 검증 방법: 샘플 TopicAnalysis 입력 테스트
- 작업자가 수정해도 되는 파일 범위: TopicAction 생성 관련 파일
- 수정하면 안 되는 파일 범위: 수집 flow
- 관련 task 문서 경로: `docs/tasks/T-410-topic-action-draft.md`

## Phase 7: 외부 앱 연동 초안

### T-500-calendar-intent-draft

- task id: `T-500-calendar-intent-draft`
- 작업명: Calendar intent 초안
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: Calendar insert intent로 사용자가 검토 가능한 일정 초안을 엽니다.
- 담당 브랜치명: `feat/T-500-calendar-intent-draft`
- 예상 수정 파일: `presentation/handoff/`, Action review UI
- 선행 task: `T-410-topic-action-draft`
- Blocked by: TopicAction 초안 생성 미완료
- Ready criteria: `T-410` Done
- 병렬 진행 가능 여부: 가능
- Can run in parallel with: `T-510-notes-share-draft`
- Cannot run with: Action model 변경 task
- 충돌 가능성이 있는 파일: HandoffLauncher, Action UI
- 완료 기준: 사용자 승인 전 Calendar 앱에 초안만 전달됩니다.
- 검증 방법: Calendar 앱 실행 수동 확인
- 작업자가 수정해도 되는 파일 범위: Calendar handoff 관련 파일
- 수정하면 안 되는 파일 범위: 수집/DB schema
- 관련 task 문서 경로: `docs/tasks/T-500-calendar-intent-draft.md`

### T-510-notes-share-draft

- task id: `T-510-notes-share-draft`
- 작업명: Notes 공유 초안
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: 요약/노트 초안을 Android Share Sheet로 전달합니다.
- 담당 브랜치명: `feat/T-510-notes-share-draft`
- 예상 수정 파일: `presentation/handoff/`, Action review UI
- 선행 task: `T-410-topic-action-draft`
- Blocked by: TopicAction 초안 생성 미완료
- Ready criteria: `T-410` Done
- 병렬 진행 가능 여부: 가능
- Can run in parallel with: `T-500-calendar-intent-draft`
- Cannot run with: Action model 변경 task
- 충돌 가능성이 있는 파일: HandoffLauncher, Action UI
- 완료 기준: 사용자가 공유 대상을 직접 선택합니다.
- 검증 방법: Android chooser 수동 확인
- 작업자가 수정해도 되는 파일 범위: share draft 관련 파일
- 수정하면 안 되는 파일 범위: Gemini/DB
- 관련 task 문서 경로: `docs/tasks/T-510-notes-share-draft.md`

## Phase 8: QA, 오류 처리, 문서 정리

### T-900-qa-build-test

- task id: `T-900-qa-build-test`
- 작업명: QA, 빌드, 테스트 정리
- Status: `Not Ready`
- Owner: `Unassigned`
- 목적: MVP 흐름 전체를 수동/자동으로 검증하고 문서를 정리합니다.
- 담당 브랜치명: `test/T-900-qa-build-test`
- 예상 수정 파일: QA 문서, 테스트 파일
- 선행 task: `T-500-calendar-intent-draft`, `T-510-notes-share-draft`
- Blocked by: 주요 MVP 기능 미완료
- Ready criteria: Phase 7 핵심 task Done
- 병렬 진행 가능 여부: 가능
- Can run in parallel with: 작은 fix task
- Cannot run with: 대형 구조 변경 task
- 충돌 가능성이 있는 파일: QA 문서
- 완료 기준: 주요 시나리오가 체크리스트로 검증됩니다.
- 검증 방법: `test`, `assembleDebug`, 실제 기기 수동 확인
- 작업자가 수정해도 되는 파일 범위: QA/테스트 문서와 테스트 코드
- 수정하면 안 되는 파일 범위: 기능 구현 코드
- 관련 task 문서 경로: `docs/tasks/T-900-qa-build-test.md`

## 현재 추천 가능한 task

현재 `Ready` 상태는 `T-000-current-code-audit`뿐입니다. `T-010-agents-and-docs-setup`은 현재 문서 작성 작업으로 `In Progress`이며, 구현 task는 모두 선행 문서/구조 task가 끝나기 전까지 시작하면 안 됩니다.
