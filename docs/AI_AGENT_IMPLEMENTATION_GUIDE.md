# AI Agent 구현 가이드 (실무 작업자용)

> **대상**: SmartClipboardAI 프로젝트에서 Topic 기반 AI Agent(TopicAnalysis + TopicAction 초안 생성)를 구현할 Kotlin/Android 개발자 또는 AI 코딩 도우미
> **전제**: 방금 완료한 Gemini MVVM 재배치(`docs/GEMINI_MVVM_MIGRATION.md`) 결과를 기준으로 합니다. 아래 클래스/인터페이스는 이미 존재합니다.
> **용어**: "Agent" = Gemini API를 호출하여 Topic에 연결된 DataItem들을 분석하고, `TopicAnalysis` + 여러 `TopicAction`(SUMMARY/CALENDAR/REMINDER/TODO/SHARE_DRAFT)을 생성하는 파이프라인

---

## 0. 작업 전 이미 존재하는 것들 (건드리지 말 것)

```
domain/ai/GeminiManager.kt          ← 인터페이스: suspend fun run(prompt: String): String
domain/ai/GeminiClient.kt           ← 인터페이스: suspend fun refineText(type: InputType, text: String): LlmStructuredOutput
data/ai/DefaultGeminiManager.kt     ← 구현체: okhttp로 Gemini generateContent 호출
data/ai/DefaultGeminiClient.kt      ← 구현체: GeminiManager 호출 + JSON 파싱 → LlmStructuredOutput
di/AiModule.kt                      ← 이미 GeminiManager, GeminiClient, SourceExtractor, KnowledgeRepository 바인딩 완료

domain/model/TopicAction.kt         ← TopicActionType(SUMMARY,CALENDAR,REMINDER,SHARE_DRAFT,TODO), TopicActionStatus(DRAFT,EDITED,EXECUTED,DISMISSED)
domain/model/TopicAnalysis.kt       ← data class TopicAnalysis(id, topicId, summary, keyPoints, sourceItemIds, createdAt)
domain/model/LlmStructuredOutput.kt ← GeminiClient.refineText()의 출력 모델 (title, topic, purpose, summary, keywords, cleanedContent, groupKey, groupReason)

data/repository/DataRepositoryImpl.kt ← runTopicAnalysis()가 현재 휴리스틱으로 구현되어 있음 ← 이걸 Gemini Agent로 교체하는 게 핵심 작업
```

---

## 1. 작업 목표 (3가지)

### 1-1. `TopicAgent` 인터페이스 + 구현체 만들기

**이유**: 기존 `GeminiClient.refineText()`는 "단일 텍스트 1건 → LlmStructuredOutput" 구조입니다. Topic 분석은 "여러 DataItem 묶음 → TopicAnalysis + 여러 TopicAction"이므로, 별도 인터페이스가 필요합니다.

### 1-2. `DataRepositoryImpl.runTopicAnalysis()`를 Gemini Agent로 교체

현재 171~197행의 휴리스틱(if문 기반)을 Gemini API 호출로 대체합니다.

### 1-3. Handoff Tool 정리 (기존 `HandoffLauncher`/`HandoffDraftFormatter` 보강)

기존 handoff 코드가 이미 있으니, Agent가 생성한 `TopicAction.editablePayload`를 파싱해서 실행하는 Tool 계층을 정리합니다.

---

## 2. 작업 1: TopicAgent 만들기

### 2-1. 새 파일 생성 목록

| 파일 | 내용 |
|------|------|
| `domain/ai/TopicAgent.kt` | 인터페이스 |
| `domain/model/AgentResult.kt` | Agent 출력 모델 2개 (`AgentPlan`, `AgentActionDraft`) |
| `data/ai/GeminiTopicAgent.kt` | Gemini 구현체 |
| `data/ai/FakeTopicAgent.kt` | 테스트용 fake (Gemini 실패 시 fallback) |
| `data/ai/AgentJsonParser.kt` | Gemini JSON 응답을 AgentResult로 변환하는 파서 |

### 2-2. `domain/ai/TopicAgent.kt`

```kotlin
package com.samsung.smartclipboard.domain.ai

import com.samsung.smartclipboard.domain.model.AgentResult
import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.Topic

interface TopicAgent {
    /**
     * Topic과 연결된 DataItem들을 분석하여 AgentResult를 반환합니다.
     *
     * @param topic 분석할 Topic
     * @param items Topic에 연결된 DataItem 목록 (빈 리스트 가능 → 실패 Result 반환)
     * @param userInstruction 사용자 추가 지시사항 (nullable, 없으면 null)
     * @return Result<AgentResult> 성공/실패를 Result로 감쌈
     */
    suspend fun analyze(
        topic: Topic,
        items: List<DataItem>,
        userInstruction: String? = null
    ): Result<AgentResult>
}
```

> **왜 `suspend`이고 `Result`로 감싸나**: Gemini 호출은 IO 바운드 + 실패 가능성이 높습니다. 예외를 던지지 않고 `Result.failure(exception)`를 반환해서 ViewModel/repository에서 안전하게 분기합니다.

### 2-3. `domain/model/AgentResult.kt`

```kotlin
package com.samsung.smartclipboard.domain.model

import kotlinx.serialization.Serializable

/**
 * TopicAgent.analyze()의 최종 출력.
 * TopicAnalysisEntity 1개 + TopicActionEntity N개로 분해되어 저장됩니다.
 */
data class AgentResult(
    val topicId: Long,
    val summary: String,
    val keyPoints: List<String>,
    val sourceItemIds: List<Long>,
    val actions: List<AgentActionDraft>
)

/**
 * TopicAgent가 추천한 개별 action 초안.
 * TopicActionEntity로 변환되어 저장됩니다.
 */
data class AgentActionDraft(
    val type: TopicActionType,
    val confidence: Float,      // 0.0~1.0, Gemini가 추정한 신뢰도
    val reason: String,         // 왜 이 action을 추천했는지 한 줄 설명
    val title: String,          // 사용자에게 보여줄 action 제목
    val body: String,           // action 본문 (사용자가 편집 가능한 초안)
    val payload: String?,       // 앱별 payload JSON 문자열 (editablePayload 저장용)
    val sourceItemIds: List<Long> // 이 action의 근거가 된 DataItem id 목록
)
```

### 2-4. `data/ai/GeminiTopicAgent.kt`

**핵심**: `GeminiManager.run(prompt)`를 호출해 JSON 응답을 받고, `AgentJsonParser`로 파싱합니다.

```kotlin
package com.samsung.smartclipboard.data.ai

import com.samsung.smartclipboard.domain.ai.GeminiManager
import com.samsung.smartclipboard.domain.ai.TopicAgent
import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.AgentResult
import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.DataItemType
import com.samsung.smartclipboard.domain.model.Topic
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiTopicAgent @Inject constructor(
    private val geminiManager: GeminiManager,
    private val parser: AgentJsonParser
) : TopicAgent {

    override suspend fun analyze(
        topic: Topic,
        items: List<DataItem>,
        userInstruction: String?
    ): Result<AgentResult> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("분석할 자료가 없습니다."))
        }

        val prompt = buildPrompt(topic, items, userInstruction)

        return try {
            val rawResponse = geminiManager.run(prompt)
            if (rawResponse.isBlank()) {
                Result.failure(IllegalStateException("Gemini 응답이 비어 있습니다."))
            } else {
                parser.parse(topic.id, items.map { it.id }.toSet(), rawResponse)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildPrompt(
        topic: Topic,
        items: List<DataItem>,
        userInstruction: String?
    ): String {
        val itemDescriptions = items.joinToString("\n---\n") { item ->
            buildString {
                append("id: ${item.id}\n")
                append("type: ${item.type.name}\n")
                if (!item.title.isNullOrBlank()) append("title: ${item.title}\n")
                append("content: ${item.content.take(500)}\n")
                if (!item.source.isNullOrBlank()) append("source: ${item.source}\n")
            }
        }

        val instructionLine = if (!userInstruction.isNullOrBlank()) {
            "\n[사용자 추가 지시]\n$userInstruction\n"
        } else ""

        return """
            당신은 SmartClipboardAI의 초안 생성 Agent입니다.
            주어진 Topic과 자료를 분석해 실행 가능한 초안을 JSON으로만 출력하세요.

            [금지]
            - 사용자 확인 없는 외부 앱 실행 금지
            - 설명 문장 금지. JSON만 출력
            - 근거 없는 장소, 참석자, 수신자를 상상하지 마세요
            - source item id는 입력에 있는 id만 사용하세요
            - action은 최대 5개까지 생성

            [Topic]
            title: ${topic.title}
            ${instructionLine}

            [자료 목록] (${items.size}개)
            $itemDescriptions

            [JSON 스키마]
            {
              "summary": "주제와 자료의 핵심 요약 (2~3문장)",
              "keyPoints": ["핵심 포인트 1", "2", "3"],
              "sourceItemIds": [1, 2, 3],
              "recommendedActions": [
                {
                  "type": "SUMMARY|CALENDAR|REMINDER|TODO|SHARE_DRAFT",
                  "confidence": 0.91,
                  "reason": "추천 이유 한 줄",
                  "title": "action 제목",
                  "body": "사용자가 검토할 초안 본문",
                  "payload": { "app": "NOTES|CALENDAR|REMINDER|SHARE", ... },
                  "sourceItemIds": [1, 2]
                }
              ]
            }

            [Action 생성 기준]
            - SUMMARY: 자료가 2개 이상이면 기본 후보 (노트 정리, 리서치, 회의록)
            - CALENDAR: 날짜/시간/장소 정보가 명확할 때만 (ISO-8601 형식)
            - REMINDER: 마감/제출/준비/연락 등 후속 행동이 있을 때
            - TODO: 해야 할 일은 보이지만 payload가 불충분할 때
            - SHARE_DRAFT: 누군가에게 전달할 메시지/메일 초안이 자연스러울 때

            [payload 규칙 (action type별)]
            SUMMARY → {"app":"NOTES","noteTitle":"...","noteBody":"...","sourceItemIds":[...],"needsUserInput":[]}
            CALENDAR → {"app":"CALENDAR","eventTitle":"...","eventDescription":"...","startTime":"2026-05-30T14:00:00+09:00","endTime":"...","location":null,"sourceItemIds":[...],"needsUserInput":[]}
            REMINDER → {"app":"REMINDER","reminderTitle":"...","reminderBody":"...","dueTime":"2026-05-31T09:00:00+09:00","sourceItemIds":[...],"needsUserInput":[]}
            TODO → {"app":"INTERNAL_TODO","tasks":[{"title":"...","description":"...","sourceItemIds":[...]}],"needsUserInput":[]}
            SHARE_DRAFT → {"app":"SHARE","shareTitle":"...","shareText":"...","sourceItemIds":[...],"needsUserInput":[]}
        """.trimIndent()
    }
}
```

### 2-5. `data/ai/AgentJsonParser.kt`

**핵심**: Gemini 응답(`markdown fence 제거 → 순수 JSON → 파싱 → 검증`)을 `Result<AgentResult>`로 변환합니다.

```kotlin
package com.samsung.smartclipboard.data.ai

import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.AgentResult
import com.samsung.smartclipboard.domain.model.TopicActionType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentJsonParser @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun parse(
        topicId: Long,
        validItemIds: Set<Long>,
        rawResponse: String
    ): Result<AgentResult> {
        return try {
            // 1. markdown fence 제거
            val clean = rawResponse
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            // 2. JSON 파싱
            val root = json.decodeFromString<JsonObject>(clean)

            // 3. 필수 필드 추출
            val summary = root["summary"]?.jsonPrimitive?.content
                ?: return Result.failure(IllegalArgumentException("summary 필드 누락"))
            val keyPoints = root["keyPoints"]?.jsonArray?.mapNotNull {
                it.jsonPrimitive.contentOrNull
            } ?: emptyList()
            val sourceItemIds = root["sourceItemIds"]?.jsonArray?.mapNotNull {
                it.jsonPrimitive.longOrNull
            }?.filter { it in validItemIds } ?: emptyList()

            // 4. actions 파싱
            val actions = root["recommendedActions"]?.jsonArray?.mapNotNull { element ->
                parseAction(element, validItemIds)
            } ?: emptyList()

            Result.success(
                AgentResult(
                    topicId = topicId,
                    summary = summary,
                    keyPoints = keyPoints,
                    sourceItemIds = sourceItemIds,
                    actions = actions
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseAction(element: kotlinx.serialization.json.JsonElement, validItemIds: Set<Long>): AgentActionDraft? {
        return try {
            val obj = element.jsonObject
            val typeStr = obj["type"]?.jsonPrimitive?.content ?: return null
            val type = TopicActionType.entries.find { it.name == typeStr } ?: return null
            val title = obj["title"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() } ?: return null
            val body = obj["body"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() } ?: return null

            val confidence = obj["confidence"]?.jsonPrimitive?.floatOrNull
                ?.coerceIn(0f, 1f) ?: 0.5f

            val reason = obj["reason"]?.jsonPrimitive?.content ?: ""

            val sourceItemIds = obj["sourceItemIds"]?.jsonArray?.mapNotNull {
                it.jsonPrimitive.longOrNull
            }?.filter { it in validItemIds } ?: emptyList()

            val payload = obj["payload"]?.let { json.encodeToString(JsonObject.serializer(), it.jsonObject) }

            AgentActionDraft(
                type = type,
                confidence = confidence,
                reason = reason,
                title = title,
                body = body,
                payload = payload,
                sourceItemIds = sourceItemIds
            )
        } catch (e: Exception) {
            null // 단일 action 파싱 실패 → 건너뜀
        }
    }
}
```

### 2-6. `data/ai/FakeTopicAgent.kt`

Gemini 실패 시 fallback. 최소 `SUMMARY` + `TODO`를 생성합니다. 테스트 용도로도 사용합니다.

```kotlin
package com.samsung.smartclipboard.data.ai

import com.samsung.smartclipboard.domain.ai.TopicAgent
import com.samsung.smartclipboard.domain.model.AgentActionDraft
import com.samsung.smartclipboard.domain.model.AgentResult
import com.samsung.smartclipboard.domain.model.DataItem
import com.samsung.smartclipboard.domain.model.DataItemType
import com.samsung.smartclipboard.domain.model.Topic
import com.samsung.smartclipboard.domain.model.TopicActionType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeTopicAgent @Inject constructor() : TopicAgent {

    override suspend fun analyze(
        topic: Topic,
        items: List<DataItem>,
        userInstruction: String?
    ): Result<AgentResult> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("분석할 자료가 없습니다."))
        }

        val itemIds = items.map { it.id }
        val summary = "'${topic.title}'에 ${items.size}개 자료가 연결되어 있습니다. 자료를 검토하고 정리해보세요."

        val keyPoints = mutableListOf<String>()
        val links = items.count { it.type == DataItemType.LINK }
        val screenshots = items.count { it.type == DataItemType.SCREENSHOT }
        val texts = items.count { it.type == DataItemType.TEXT }

        if (links > 0) keyPoints += "링크 ${links}개 — 참고 자료 또는 리서치 대상입니다."
        if (screenshots > 0) keyPoints += "스크린샷 ${screenshots}개 — OCR로 텍스트를 추출하면 더 분석하기 좋습니다."
        if (texts > 0) keyPoints += "텍스트 ${texts}개 — 요약과 할 일 추출에 사용할 수 있습니다."
        if (keyPoints.isEmpty()) keyPoints += "자료를 요약하고 다음 작업 후보를 만들어보세요."

        val hasDateCandidate = items.any { item ->
            item.content.contains(Regex("\\d{1,2}:\\d{2}|\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}|AM|PM|오전|오후", RegexOption.IGNORE_CASE))
        }

        val actions = mutableListOf(
            AgentActionDraft(
                type = TopicActionType.SUMMARY,
                confidence = 0.8f,
                reason = "여러 자료를 하나의 정리 노트로 묶을 수 있습니다.",
                title = "${topic.title} — 자료 요약",
                body = summary,
                payload = null,
                sourceItemIds = itemIds
            ),
            AgentActionDraft(
                type = TopicActionType.TODO,
                confidence = 0.5f,
                reason = "자료를 검토하고 실행할 작업을 확정하세요.",
                title = "다음 할 일 정리",
                body = "연결된 자료를 하나씩 확인하고 필요한 작업을 정리하세요.",
                payload = null,
                sourceItemIds = itemIds
            )
        )

        if (hasDateCandidate) {
            actions.add(
                AgentActionDraft(
                    type = TopicActionType.CALENDAR,
                    confidence = 0.3f,
                    reason = "자료에서 날짜/시간 후보가 발견되었습니다.",
                    title = "캘린더 일정 후보",
                    body = "자료에 날짜/시간이 포함되어 있습니다. 제목과 시간을 확인하세요.",
                    payload = null,
                    sourceItemIds = itemIds
                )
            )
        }

        return Result.success(
            AgentResult(
                topicId = topic.id,
                summary = summary,
                keyPoints = keyPoints,
                sourceItemIds = itemIds,
                actions = actions
            )
        )
    }
}
```

### 2-7. DI 등록

`di/AiModule.kt`에 아래 바인딩을 추가합니다:

```kotlin
@Binds @Singleton
abstract fun bindTopicAgent(impl: GeminiTopicAgent): TopicAgent

@Binds @Singleton
abstract fun bindFakeTopicAgent(impl: FakeTopicAgent): TopicAgent  // fallback용
```

> **참고**: `TopicAgent` 인터페이스에 대해 두 구현체가 필요하면 `@Named` qualifier를 사용하거나, `GeminiTopicAgent` 내부에서 실패 시 `FakeTopicAgent`로 fallback하는 구조가 더 깔끔합니다. 후자를 추천합니다: `GeminiTopicAgent`는 `GeminiTopicAgent` 자신이 `TopicAgent`로 바인딩되고, `FakeTopicAgent`를 필드로 주입받아 Gemini 실패 시 fallback 호출.

---

## 3. 작업 2: `DataRepositoryImpl.runTopicAnalysis()` 교체

### 3-1. 현재 코드 위치

`app/src/main/java/com/samsung/smartclipboard/data/repository/DataRepositoryImpl.kt` 171~197행

```kotlin
override suspend fun runTopicAnalysis(topicId: Long) {
    val items = topicDao.observeItemsForTopic(topicId).first().map { it.toDomain() }
    if (items.isEmpty()) return

    val now = System.currentTimeMillis()
    val summary = buildTopicSummary(items)          // ← 휴리스틱
    val keyPoints = buildTopicKeyPoints(items)       // ← 휴리스틱
    // ... TopicAnalysisEntity 저장
    val actions = buildTopicActions(...)             // ← 휴리스틱
    // ... TopicActionEntity 저장
}
```

### 3-2. 교체 방법

`DataRepositoryImpl`의 생성자에 `TopicAgent`를 주입받고, `runTopicAnalysis()`를 아래처럼 바꿉니다.

```kotlin
class DataRepositoryImpl @Inject constructor(
    private val dataItemDao: DataItemDao,
    private val aiProposalDao: AiProposalDao,
    private val topicDao: TopicDao,
    private val aiProposalGenerator: AiProposalGenerator,
    private val topicAgent: TopicAgent  // ← 추가
) : DataRepository {

    // ... 기존 메서드들 그대로 유지 ...

    override suspend fun runTopicAnalysis(topicId: Long) {
        val items = topicDao.observeItemsForTopic(topicId).first().map { it.toDomain() }
        if (items.isEmpty()) return

        // Topic 정보 조회 (TopicDao에 getTopicById()가 필요하면 추가)
        val topicRow = topicDao.getTopicById(topicId) ?: return
        val topic = Topic(
            id = topicRow.id,
            title = topicRow.title,
            itemCount = items.size,
            createdAt = topicRow.createdAt,
            updatedAt = topicRow.updatedAt
        )

        val now = System.currentTimeMillis()

        // TopicAgent 호출
        val result = topicAgent.analyze(topic, items)

        result.onSuccess { agentResult ->
            // 1. TopicAnalysis 저장
            val analysisId = topicDao.insertAnalysis(
                TopicAnalysisEntity(
                    topicId = topicId,
                    summary = agentResult.summary,
                    keyPoints = agentResult.keyPoints.joinToString("\n"),
                    sourceItemIds = agentResult.sourceItemIds.joinToString(","),
                    createdAt = now
                )
            )

            // 2. TopicAction 저장
            val actionEntities = agentResult.actions.map { draft ->
                TopicActionEntity(
                    topicId = topicId,
                    analysisResultId = analysisId,
                    type = draft.type.name,
                    title = draft.title,
                    body = draft.body,
                    status = TopicActionStatus.DRAFT.name,
                    editablePayload = draft.payload,
                    createdAt = now,
                    updatedAt = now
                )
            }
            if (actionEntities.isNotEmpty()) {
                topicDao.insertActions(actionEntities)
            }
        }

        result.onFailure { exception ->
            // Gemini 실패 → 로깅 (향후 사용자에게 알림)
            // 원본 자료는 삭제하지 않음
            android.util.Log.e("TopicAgent", "분석 실패: topicId=$topicId", exception)
        }

        topicDao.updateTopicTimestamp(topicId, now)
    }
}
```

### 3-3. TopicDao에 필요한 메서드가 없으면 추가

`TopicDao`에 `getTopicById()`가 없으면 아래를 추가합니다:

```kotlin
// TopicDao.kt에 추가
@Query("SELECT id, title, itemCount, createdAt, updatedAt FROM topic_summary_view WHERE id = :topicId")
suspend fun getTopicById(topicId: Long): TopicSummaryRow?
```

---

## 4. 작업 3: Handoff Tool 정리

### 4-1. 기존 Handoff 코드 (이미 존재)

| 파일 | 내용 |
|------|------|
| `presentation/handoff/HandoffDraftFormatter.kt` | `TopicAction.editablePayload`를 파싱해 표시용 텍스트로 변환 |
| `presentation/handoff/HandoffLauncher.kt` | `Intent.ACTION_SEND` / `Intent.ACTION_INSERT` 실행 |

### 4-2. Agent가 만든 payload를 HandoffLauncher가 처리할 수 있도록 정리

`HandoffLauncher`가 `editablePayload` JSON을 파싱해서:

- `app=NOTES` → `Intent.ACTION_SEND` + `text/plain` MIME으로 본문 전달
- `app=CALENDAR` → `Intent.ACTION_INSERT` + `CalendarContract.Events.CONTENT_URI`
- `app=REMINDER` → MVP에서는 외부 앱 열지 않고 UI에서 텍스트 표시만
- `app=SHARE` → `Intent.ACTION_SEND` + chooser
- `app=INTERNAL_TODO` → UI에서 TODO 리스트로 표시

**구체적 작업**:
1. `HandoffLauncher`의 `launch()` 메서드에 `app` 필드 분기를 추가
2. `editablePayload` 파싱 시 `version` 필드가 있으면 해당 버전으로 처리, 없으면 기본값 사용
3. payload에 `needsUserInput`이 비어 있지 않으면, 부족한 필드를 사용자에게 보여주고 입력받은 후 실행

### 4-3. 수정할 파일

- `presentation/handoff/HandoffLauncher.kt`
- `presentation/handoff/HandoffDraftFormatter.kt`

> `domain/model/TopicAction.kt`와 `presentation/handoff/HandoffDraftFormatter.kt`는 공통 파일 보호 대상입니다. 수정 시 프로젝트 오너 승인이 필요합니다.

---

## 5. DI 등록 전체 요약

`di/AiModule.kt` 최종 모습에 `TopicAgent` 바인딩을 추가합니다:

```kotlin
// 기존 바인딩은 그대로 두고 아래만 추가
@Binds @Singleton
abstract fun bindTopicAgent(impl: GeminiTopicAgent): TopicAgent
```

`DataRepositoryImpl`에 `TopicAgent` 주입을 추가합니다. (`AppModule.kt`의 `provideDataRepository()`는 이미 `DataRepositoryImpl`을 생성하므로, `DataRepositoryImpl` 생성자에 `topicAgent` 파라미터만 추가하면 Hilt가 자동 주입합니다.)

---

## 6. Gemini JSON 프롬프트 디버깅 가이드

### Gemini 응답 로깅

개발 중에는 `GeminiTopicAgent.analyze()`에서 raw response를 로깅하세요:

```kotlin
val rawResponse = geminiManager.run(prompt)
android.util.Log.d("GeminiTopicAgent", "Raw response:\n$rawResponse")
```

### 자주 발생하는 파싱 오류

| 오류 | 원인 | 해결 |
|------|------|------|
| `summary` 필드 누락 | Gemini가 스키마 무시 | 프롬프트 앞부분에 "summary 필드는 반드시 포함하세요" 강조 |
| `sourceItemIds`에 엉뚱한 id | Gemini가 hallucination | `validItemIds`로 필터링 (이미 구현됨) |
| markdown fence 미제거 | Gemini가 \`\`\`json으로 감쌈 | `removePrefix("```json")` 처리 (이미 구현됨) |
| `confidence` 범위 초과 | Gemini가 1.5 같은 값 반환 | `coerceIn(0f, 1f)` (이미 구현됨) |

### 테스트 프롬프트 (curl)

```bash
curl -X POST "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"contents":[{"parts":[{"text":"{\"summary\":\"test\",\"keyPoints\":[\"p1\"],\"sourceItemIds\":[1],\"recommendedActions\":[{\"type\":\"SUMMARY\",\"confidence\":0.9,\"reason\":\"test\",\"title\":\"test\",\"body\":\"test body\",\"payload\":{},\"sourceItemIds\":[1]}]}"}]}]}'
```

---

## 7. 작업 순서 (추천)

| 순서 | 작업 | 예상 시간 |
|------|------|-----------|
| 1 | `domain/model/AgentResult.kt` 생성 | 5분 |
| 2 | `domain/ai/TopicAgent.kt` 생성 | 5분 |
| 3 | `data/ai/AgentJsonParser.kt` 생성 | 30분 |
| 4 | `data/ai/FakeTopicAgent.kt` 생성 | 15분 |
| 5 | `data/ai/GeminiTopicAgent.kt` 생성 | 30분 |
| 6 | `di/AiModule.kt`에 TopicAgent 바인딩 추가 | 5분 |
| 7 | `DataRepositoryImpl`에 TopicAgent 주입 + runTopicAnalysis 교체 | 20분 |
| 8 | `HandoffLauncher`에 payload 파싱 분기 추가 | 20분 |
| 9 | `assembleDebug` 빌드 확인 | 5분 |
| 10 | FakeTopicAgent + AgentJsonParser 단위 테스트 작성 | 30분 |

**총 예상 시간**: 약 2시간 45분

---

## 8. 금지 사항

- `GeminiManager.run()` 호출 시 API 키를 코드에 하드코딩하지 마세요. 이미 DI로 주입됩니다.
- `DataRepositoryImpl`에 Gemini prompt 문자열을 직접 넣지 마세요. 프롬프트는 `GeminiTopicAgent.buildPrompt()`에만 존재해야 합니다.
- `TopicAction`의 `status`를 Agent가 직접 `EXECUTED`로 설정하지 마세요. 항상 `DRAFT`로 시작합니다.
- 사용자 확인 없이 Calendar/Notes/Reminder 앱을 직접 실행하지 마세요.
- `SmartClipboardDatabase` 스키마를 Agent 작업 때문에 변경하지 마세요. `editablePayload` 컬럼은 이미 존재합니다.

---

> **마지막 업데이트**: 2026-05-29
> **기반 코드 상태**: Gemini MVVM 재배치 완료 (`docs/GEMINI_MVVM_MIGRATION.md` 참고)