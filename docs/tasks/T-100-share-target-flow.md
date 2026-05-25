# T-100 Share Target 수집 흐름

## 목적

Share Sheet에서 들어오는 링크/텍스트/이미지/파일 수신 흐름을 안정화합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-100-share-target-flow
- Depends on: T-050-permission-and-manifest-baseline
- Blocked by: Manifest baseline 미완료
- Ready criteria: T-050이 Done
- Can run in parallel with: T-110-quick-tile-flow, T-130-storage-access-framework-picker
- Cannot run with: Manifest 수정 task

## 수정 허용 파일

- `app/src/main/java/com/samsung/smartclipboard/presentation/share/`
- `app/src/main/java/com/samsung/smartclipboard/data/source/share/`
- 관련 QA 문서

## 수정 금지 파일

- DB schema
- Gradle
- Navigation baseline 파일

## 구현 내용

- 기존 `ShareReceiverActivity`와 `AndroidShareContentHandler`를 기반으로 개선합니다.
- 실패 메시지와 저장 피드백을 정리합니다.

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

- 텍스트, 링크, 이미지, 파일 공유 수신이 동작합니다.
- 실패 케이스가 사용자에게 안전하게 표시됩니다.

## 검증 방법

- 다른 앱에서 텍스트/링크/이미지/PDF 공유
- `./gradlew assembleDebug`

## PR에 반드시 적을 내용

- 테스트한 MIME type
- 실패 처리
- 공통 파일 수정 여부
