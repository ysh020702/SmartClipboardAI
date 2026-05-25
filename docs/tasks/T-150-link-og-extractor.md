# T-150 링크 OG 태그 추출

## 목적

링크 DataItem에 제목, 설명, 이미지 preview 정보를 보강할 수 있는 extractor를 추가합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-150-link-og-extractor
- Depends on: T-140-dataitem-cluster-fields
- Blocked by: DataItem metadata 방향 미완료
- Ready criteria: T-140이 Done
- Can run in parallel with: T-160-local-ocr-processor
- Cannot run with: Repository interface 변경 task

## 수정 허용 파일

- 신규 WebExtractor 관련 파일
- `data/ai/` 또는 `data/source/web/` 신규 파일
- 필요한 DI module
- 관련 테스트 파일

## 수정 금지 파일

- UI 대형 리팩토링
- Manifest
- MediaStore flow

## 구현 내용

- Jsoup 또는 HTML parser를 사용해 OG tag를 추출합니다.
- 네트워크 작업은 `Dispatchers.IO`에서 수행합니다.
- 실패해도 원본 URL은 유지합니다.

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

- 정상 URL과 실패 URL 모두 안전하게 처리됩니다.

## 검증 방법

- 성공 URL 테스트
- invalid URL 테스트
- 네트워크 실패 테스트

## PR에 반드시 적을 내용

- 사용 library
- IO dispatcher 사용 여부
- 실패 처리
