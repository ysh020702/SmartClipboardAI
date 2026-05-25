# T-000 현재 코드 감사

## 목적

현재 구현된 코드와 문서를 읽고 유지할 구조, 수정할 구조, 제거 후보를 정리합니다.

## 작업 상태
- Status: Ready
- Owner: Unassigned
- Branch: docs/T-000-current-code-audit
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
