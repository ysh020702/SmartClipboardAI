# T-400 TopicAnalysis 초안 생성

## 목적

Gemini Agent가 Topic과 연결 DataItem을 분석해 `TopicAnalysis` 초안을 생성합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-400-topic-analysis-draft
- Depends on: T-310-topic-data-selection-flow, T-150-link-og-extractor, T-160-local-ocr-processor
- Blocked by: Topic/DataItem/enrichment 흐름 미완료
- Ready criteria: Depends on의 모든 task가 Done
- Can run in parallel with: none
- Cannot run with: Agent contract/model 변경 task

## 수정 허용 파일

- `app/src/main/java/com/samsung/smartclipboard/domain/ai/`
- `app/src/main/java/com/samsung/smartclipboard/data/ai/`
- 필요한 repository 연결부
- 관련 테스트 파일

## 수정 금지 파일

- Share/Tile/Media 수집 flow
- UI 대형 리팩토링
- Manifest

## 구현 내용

- Agent 입력 모델을 Topic, DataItem, 사용자 의도로 구성합니다.
- Gemini 호출 전 fake/mock 구현을 둘 수 있습니다.
- 결과는 `TopicAnalysis`로 저장합니다.

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

- 샘플 Topic에서 분석 초안이 생성됩니다.

## 검증 방법

- fake Gemini 입력/출력 테스트
- `./gradlew test`
- `./gradlew assembleDebug`

## PR에 반드시 적을 내용

- Agent 입력/출력 계약
- Gemini 실제 호출 여부
- 실패 처리
