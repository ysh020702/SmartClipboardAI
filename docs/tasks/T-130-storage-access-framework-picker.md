# T-130 Storage Access Framework 파일 선택

## 목적

사용자가 직접 사진/파일을 선택해 DataItem 후보로 추가할 수 있는 수집 경로를 만듭니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-130-storage-access-framework-picker
- Depends on: T-040-navigation-baseline
- Blocked by: 화면 baseline 미완료
- Ready criteria: T-040이 Done
- Can run in parallel with: T-100-share-target-flow, T-110-quick-tile-flow
- Cannot run with: T-200-home-ux-redesign

## 수정 허용 파일

- 지정된 picker UI 파일
- picker handler 파일
- 관련 QA 문서

## 수정 금지 파일

- DB schema
- Share/Tile handler
- Gemini/Agent 구현

## 구현 내용

- `ActivityResultContracts.GetContent` 또는 `GetMultipleContents`를 사용합니다.
- 사용자가 취소한 경우 안전하게 처리합니다.

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

- 이미지, PDF, 일반 파일 선택이 동작합니다.

## 검증 방법

- 선택 성공/취소/읽기 실패 수동 확인

## PR에 반드시 적을 내용

- 지원 MIME type
- 취소 처리
- URI 권한 처리
