# AI Agent 멀티 리트리벌(Multi-Retrieval) 도입 검토

> **작성일**: 2026-05-29
> **상태**: 검토 완료 — 조건부 Multi-Turn(Lazy Refinement) 추천
> **관련 문서**: `docs/AI_AGENT_IMPLEMENTATION_GUIDE.md`

---

## 1. 검토 배경

SmartClipboardAI의 TopicAgent는 현재 **Single-Turn** 구조로 동작한다. 한 번의 Gemini API 호출로 Topic 분석부터 Action 초안 생성까지 모든 작업을 완료한다.

Cline(코드 어시스턴트)은 이와 달리 **Multi-Retrieval** 패턴으로 작업한다:

1. **탐색(Exploration)** — 파일 읽기, 코드 검색 등 도구로 컨텍스트 수집
2. **추론(Reasoning)** — 수집된 정보로 분석
3. **실행(Action)** — 코드 수정, 파일 생성
4. **관찰(Observation)** — 실행 결과 확인 → 부족하면 다시 1로

이 문서는 Cline 스타일의 multi-turn 접근을 SmartClipboardAI의 TopicAgent 파이프라인에 도입하는 것이 합리적인지 검토한다.

---

## 2. 현재 Single-Turn 구조

```
TopicAgent.analyze(topic, items)
  → GeminiTopicAgent.buildPrompt()    // 모든 자료 + 지시사항을 하나의 프롬프트에 담음
  → GeminiManager.run(prompt)         // Gemini API 1회 호출
  → AgentJsonParser.parse()           // JSON 파싱
  → AgentResult (summary + keyPoints + actions)
```

### 특징

| 항목 | 값 |
|------|-----|
| API 호출 횟수 | 1회 |
| 평균 지연 시간 | 1~3초 |
| 프롬프트 크기 | items × take(500) + 시스템 프롬프트 |
| 실패 처리 | `Result.failure` 반환, Repository에서 로깅 |
| Fallback | `FakeTopicAgent` (휴리스틱) |

### 장점

- 지연 시간이 짧아 모바일 UX에 적합
- 구현이 단순하고 상태 관리가 필요 없음
- API 비용이 예측 가능 (1회)

### 단점

- 프롬프트 품질에 전적으로 의존
- Gemini가 오답을 생성해도 self-critique 불가능
- 저품질 action이 생성되어도 자동 보정 없음

---

## 3. Cline 스타일 순수 Multi-Turn 구조 (가상 설계)

SmartClipboardAI에 그대로 이식한다면 다음과 같은 파이프라인이 된다:

```
Round 1: analyze() → AgentResult (1차 분석 + action 초안)
  ↓
Round 2: refine() → 수정된 AgentResult (self-critique + 보완)
  ↓
Round 3: refine() → 최종 AgentResult (확정)
```

### Multi-Turn 이식 시 예상 문제

| 문제 | 설명 |
|------|------|
| **API 비용 폭증** | 호출 횟수가 3~5배 증가. Gemini free tier(QPS 제한) 초과 위험 |
| **지연 시간 증가** | 5~15초로 모바일 앱에서 사용자 이탈 위험. 백그라운드 처리 불가 (클립보드 감시 금지) |
| **데이터 규모 불일치** | Cline은 수백~수천 줄의 코드베이스를 점진적으로 탐색하지만, SmartClipboard는 `take(500)`자 이내의 작은 데이터셋을 한 번에 다룸 |
| **망 불안정성** | 모바일 환경에서 N회 중 1회만 실패해도 전체 파이프라인 실패 처리 복잡 |
| **컨텍스트 누적** | Round 간 컨텍스트를 유지해야 하며, 토큰 한도 관리 필요 |
| **상태 관리 복잡도** | ViewModel/Repository에 중간 상태(partial result) 관리 로직 추가 필요 |

### 결론: 순수 Multi-Turn은 현재 MVP 단계에 과투자

데이터 규모가 작고, 모바일 환경의 지연 시간 제약이 엄격한 점을 고려할 때, Cline과 동일한 수준의 multi-turn 구조는 합리적이지 않다.

---

## 4. 추천: 조건부 Multi-Turn (Lazy Refinement)

순수 multi-turn의 self-critique 효과는 채택하되, 항상 실행하지 않고 **필요할 때만** 추가 라운드를 수행하는 하이브리드 접근을 추천한다.

### 파이프라인

```
Round 1 (항상 실행): analyze() → AgentResult
  ↓
  ├─ confidence ≥ 0.6 → 저장, 완료
  │
  └─ confidence < 0.6 또는 needsUserInput 미충족 → Round 2
       ↓
Round 2 (조건부): refine() → 보완된 AgentResult
  ↓
Round 3 (사용자 주도): 사용자가 "AI 보완 요청" 탭 → refine()
```

### 실행 트리거

| 트리거 | 조건 | 자동/수동 |
|--------|------|-----------|
| 저신뢰도 action | `AgentActionDraft.confidence < 0.6` | 자동 (Round 2) |
| 누락 필드 | `payload.needsUserInput`이 비어 있지 않음 | 사용자 입력 후 자동 |
| 사용자 보완 요청 | UI에서 "AI 보완" 버튼 탭 | 수동 (Round 3) |

### 기대 효과

| 효과 | 설명 |
|------|------|
| **비용 최소화** | 평균 API 호출 1.2~1.5회 (대부분의 양질 응답은 1회로 종료) |
| **품질 향상** | 저신뢰도 action에 한해 self-critique 적용 → Cline의 강점 선택적 도입 |
| **UX 유지** | 1차 결과는 1~3초 내 표시. 보완은 백그라운드 또는 사용자 요청 시 |
| **단계적 도입 가능** | MVP에서는 Round 1만 구현, 이후 Round 2/3 추가 가능 |

---

## 5. 구현 설계 (참고)

필요한 경우를 대비한 설계 개요. MVP에는 포함하지 않는다.

### 5-1. `TopicAgent` 인터페이스 확장

```kotlin
interface TopicAgent {
    // 기존 (Round 1)
    suspend fun analyze(
        topic: Topic,
        items: List<DataItem>,
        userInstruction: String? = null
    ): Result<AgentResult>

    // 조건부 (Round 2/3)
    suspend fun refine(
        topic: Topic,
        items: List<DataItem>,
        previousResult: AgentResult,
        targetActionIndices: List<Int>, // 보완할 action index 목록
        userEdit: String? = null        // 사용자가 수정한 내용
    ): Result<AgentResult>
}
```

### 5-2. `GeminiTopicAgent.buildRefinePrompt()` 추가

기존 프롬프트 + 이전 결과 + "다음 action의 신뢰도를 높여서 다시 제안하세요" 지시.

### 5-3. ViewModel/Repository 오케스트레이션

`DataRepositoryImpl.runTopicAnalysis()`에서 1차 결과 획득 후 저신뢰도 action 필터링 → 조건부 `refine()` 호출.

### 5-4. UI 트리거

`TopicActionCard` 하단에 "AI 보완 요청" 버튼 (confidence가 낮은 action에만 표시).

---

## 6. MVP 포함 여부 결정

| 라운드 | MVP 포함 | 사유 |
|--------|----------|------|
| Round 1 (analyze) | **포함** | 핵심 기능. 이미 구현 완료 |
| Round 2 (자동 refine) | **제외** | 사용자 피드백 수집 후 추가 여부 결정 |
| Round 3 (수동 refine) | **제외** | UI/UX 설계 필요. post-MVP |

---

## 7. 결론

- **순수 Cline 스타일 multi-turn은 현재 SmartClipboardAI에 부적합**하다. 데이터 규모, 지연 시간, 비용 측면에서 과투자.
- **조건부 Multi-Turn(Lazy Refinement)** 을 추천하며, MVP는 현재 Single-Turn으로 충분하다.
- Round 2/3는 실제 사용자 피드백과 Gemini 응답 품질을 관찰한 후 post-MVP로 판단한다.