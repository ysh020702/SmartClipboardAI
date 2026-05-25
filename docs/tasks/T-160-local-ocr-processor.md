# T-160 로컬 OCRProcessor 연동

## 목적

이미지/스크린샷 OCR 결과를 Agent 입력으로 사용할 수 있게 연결합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-160-local-ocr-processor
- Depends on: T-140-dataitem-cluster-fields, T-120-media-store-batch-query
- Blocked by: DataItem metadata와 MediaStore 검토 저장 흐름 미완료
- Ready criteria: T-140과 T-120이 Done
- Can run in parallel with: T-150-link-og-extractor
- Cannot run with: media 저장 구조 변경 task

## 수정 허용 파일

- 신규 OCRProcessor 관련 파일
- media enrichment 관련 파일
- 관련 테스트 또는 QA 문서

## 수정 금지 파일

- Share/Tile flow
- Navigation baseline
- DB schema 추가 변경

## 구현 내용

- 로컬 OCR 알고리즘을 interface 뒤에 연결합니다.
- OCR 실패 시 원본 DataItem은 유지합니다.
- 이미 OCR 결과가 있으면 재분석하지 않습니다.

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

- 이미지 URI 입력으로 OCR 결과를 얻을 수 있습니다.
- 실패 케이스가 안전합니다.

## 검증 방법

- 샘플 이미지 수동 확인
- OCR 실패 이미지 확인
- `./gradlew assembleDebug`

## PR에 반드시 적을 내용

- OCR 알고리즘 위치
- 재분석 방지 방식
- 실패 처리
