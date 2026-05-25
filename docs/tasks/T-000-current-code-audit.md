# T-000 현재 코드 감사

## 목적

현재 구현된 코드와 문서를 읽고 유지할 구조, 수정할 구조, 제거 후보를 정리합니다.

## 작업 상태
- Status: Done
- Owner: Codex
- Branch: codex-T-000-current-code-audit
- Depends on: none
- Blocked by: none
- Ready criteria: 문서 작업만 수행하며 Android 코드 수정이 필요하지 않음
- Can run in parallel with: T-010-agents-and-docs-setup
- Cannot run with: 공통 Kotlin 코드 수정 task

## 수정 허용 파일

- `docs/ARCHITECTURE.md`
- `docs/WORK_LOG.md`
- `docs/tasks/T-000-current-code-audit.md`

## 수정 금지 파일

- `app/` 하위 전체
- `build.gradle.kts`
- `gradle/libs.versions.toml`
- `AndroidManifest.xml`

## 구현 내용

- README와 현재 코드 구조를 비교합니다.
- `OCRProcessor`, `WebExtractor`, `GeminiManager`, `ClusterManager` 계열 클래스 존재 여부를 확인합니다.
- 재사용 가능한 클래스와 새로 만들어야 할 클래스를 문서에 반영합니다.

## 감사 결과 요약

- 재사용 후보: Share Target, Quick Settings Tile + 투명 Activity clipboard 저장, MediaStore query, Topic/Analysis/Action 모델, Handoff formatter/launcher, Hilt/Coroutine 기반 DI 구조.
- 수정 후보: `DataRepositoryImpl` 책임 분리, `MainScreen.kt` 화면 분리, MediaStore 자동 저장을 검토 후 저장으로 변경, `AiProposal` 영구 저장 구조 재검토.
- 신규 필요: `WebExtractor`, `OCRProcessor`, `GeminiManager`, `ClusterManager`, SAF picker 계약과 구현.
- 코드 수정 여부: Android 구현 코드는 수정하지 않았고 문서만 갱신했습니다.

## 다른 task 상태 영향

- 프로젝트 오너가 `T-000` 결과를 확인했습니다.
- `T-010-agents-and-docs-setup`도 오너 확인으로 `Done` 처리되었습니다.
- 이에 따라 `T-020-architecture-baseline`은 시작 가능 상태가 되었고, 현재 문서 작업으로 진행 중입니다.
- `T-220-save-feedback-bottom-sheet`도 의존성만 보면 `Ready`가 되었지만, 구현 task이므로 문서 baseline 확정 후 착수하는 것을 권장합니다.

## 체크리스트
- [x] 코드 읽기
- [x] 관련 문서 확인
- [x] 선행 task 완료 여부 확인
- [x] 구현
- [x] 빌드 확인 - 문서 전용 작업이라 Android 빌드는 실행하지 않음
- [x] 테스트/수동 확인
- [x] 변경 요약 작성
- [x] PR 작성 - 오너가 현재 문서를 확인했으며, 별도 PR 생성은 사용자 요청 전까지 보류

## 완료 기준

- 현재 구현 재사용 후보가 `docs/ARCHITECTURE.md`에 정리됩니다.
- 없는 클래스 계열이 명확히 기록됩니다.
- 다음 구조 task가 참고할 수 있는 감사 결과가 남습니다.

## 검증 방법

- `rg -n "OCR|Ocr|WebExtractor|Gemini|Cluster|ClusterManager|Jsoup|ML Kit|MlKit|Embedding|embed" app/src/main/java README.md docs`
- `rg --files app/src/main/java/com/samsung/smartclipboard`

## PR에 반드시 적을 내용

- 확인한 주요 파일
- 유지할 구조
- 수정할 구조
- 아직 없는 클래스
- 코드 수정 없음 여부
