# T-300 Topic 생성 플로우

## 목적

사용자가 작업 주제를 입력하고 최종 Topic을 생성하는 흐름을 구현합니다.

## 작업 상태
- Status: Not Ready
- Owner: Unassigned
- Branch: feat/T-300-topic-create-flow
- Depends on: T-040-navigation-baseline, T-140-dataitem-cluster-fields
- Blocked by: navigation/model 미완료
- Ready criteria: T-040과 T-140이 Done
- Can run in parallel with: none
- Cannot run with: T-200-home-ux-redesign, T-310-topic-data-selection-flow

## 수정 허용 파일

- Topic 생성 관련 `presentation/main/` 파일
- 필요한 ViewModel intent/state

## 수정 금지 파일

- Share/Tile/Media 수집 flow
- DB schema 추가 변경
- Manifest

## 구현 내용

- 사용자가 주제명을 입력합니다.
- AI 추천 여부와 상관없이 최종 Topic 확정은 사용자가 합니다.
- 빈 제목, 중복 제목, 취소 흐름을 처리합니다.

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

- 사용자가 새 Topic을 만들 수 있습니다.

## 검증 방법

- 새 Topic 생성
- 빈 제목 처리
- 중복 제목 처리
- `./gradlew assembleDebug`

## PR에 반드시 적을 내용

- Topic 생성 UX
- edge case 처리
- 변경한 state/intent
