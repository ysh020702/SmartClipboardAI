# T-110 Quick Settings Tile 클립보드 흐름

## 목적

Quick Settings Tile을 통해 사용자가 복사한 최신 텍스트/링크를 저장하는 흐름을 안정화합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-110-quick-tile-flow
- Depends on: T-050-permission-and-manifest-baseline
- Blocked by: Manifest baseline 미완료
- Ready criteria: T-050이 Done
- Can run in parallel with: T-100-share-target-flow
- Cannot run with: Manifest 수정 task

## 수정 허용 파일

- `app/src/main/java/com/samsung/smartclipboard/presentation/tile/`
- `app/src/main/java/com/samsung/smartclipboard/presentation/clipboard/`
- `app/src/main/java/com/samsung/smartclipboard/data/source/clipboard/`

## 수정 금지 파일

- DB schema
- Share receiver
- MediaStore handler

## 구현 내용

- TileService는 직접 클립보드를 읽지 않고 투명 Activity를 엽니다.
- 빈 클립보드, 중복 저장, 링크 판별 실패를 처리합니다.

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

- 복사 후 Tile 클릭으로 최근 Primary Clip이 저장됩니다.

## 검증 방법

- 실제 기기 또는 emulator에서 Tile 추가 후 수동 테스트
- `./gradlew assembleDebug`

## PR에 반드시 적을 내용

- 테스트 기기/API
- 빈 클립보드 처리
- 중복 저장 정책
