# T-220 저장 피드백 바텀시트 UX

## 목적

Share Target과 Quick Settings Tile 저장 피드백을 작고 일관된 바텀시트/투명 Activity UX로 개선합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-220-save-feedback-bottom-sheet
- Depends on: T-010-agents-and-docs-setup
- Blocked by: 문서 기준 리뷰 미완료
- Ready criteria: T-010이 Done
- Can run in parallel with: T-200-home-ux-redesign, T-210-data-list-filter-selection
- Cannot run with: T-100-share-target-flow, T-110-quick-tile-flow

## 수정 허용 파일

- `app/src/main/java/com/samsung/smartclipboard/presentation/share/`
- `app/src/main/java/com/samsung/smartclipboard/presentation/clipboard/`

## 수정 금지 파일

- DB schema
- Repository
- Manifest
- MediaStore handler

## 구현 내용

- 저장 중, 저장 성공, 실패 상태를 일관되게 보여줍니다.
- 앱 전체가 켜진 것처럼 보이지 않는 짧은 피드백을 유지합니다.

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

- Share와 Tile 피드백이 같은 UX 언어를 사용합니다.

## 검증 방법

- Share 수동 확인
- Tile 수동 확인
- `./gradlew assembleDebug`

## PR에 반드시 적을 내용

- 변경 전/후 동작
- 스크린샷 또는 화면 설명
- 실패 상태 표시 방식
