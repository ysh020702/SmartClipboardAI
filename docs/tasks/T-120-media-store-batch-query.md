# T-120 MediaStore Batch Query

## 목적

앱 실행 시 Last Sync Time 이후 새 이미지/스크린샷 후보를 찾고 검토 후 저장 흐름으로 넘깁니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-120-media-store-batch-query
- Depends on: T-050-permission-and-manifest-baseline, T-040-navigation-baseline
- Blocked by: 권한 baseline과 화면 baseline 미완료
- Ready criteria: T-050과 T-040이 Done
- Can run in parallel with: T-130-storage-access-framework-picker
- Cannot run with: T-200-home-ux-redesign

## 수정 허용 파일

- `app/src/main/java/com/samsung/smartclipboard/data/source/media/`
- 지정된 media review UI 파일
- 관련 QA 문서

## 수정 금지 파일

- Share/Tile 흐름
- Gemini/Agent 구현
- DB schema

## 구현 내용

- Last Sync Time 저장 전략을 구현합니다.
- MediaStore 후보를 자동 저장하지 않고 검토 UI로 전달합니다.
- 중복 URI를 방지합니다.

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

- 새 후보 조회, 검토, 선택 저장 흐름이 동작합니다.

## 검증 방법

- 권한 없음/전체 권한/부분 권한 시나리오 수동 확인
- API 34 부분 접근 확인

## PR에 반드시 적을 내용

- Last Sync Time 저장 방식
- 중복 방지 방식
- Android 버전별 결과
