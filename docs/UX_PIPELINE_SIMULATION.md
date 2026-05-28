# SmartClipboardAI 사용자 경험 시뮬레이션 (처음부터 끝까지)

> **기준 코드**: 2026-05-29 정리 완료. FakeTopicAgent 제거, AiProposal 제거, Gemini MVVM 재배치 완료 상태
> **목적**: 앱을 처음 설치하고 실행하는 순간부터 데이터 수집 → Topic 생성 → AI 분석 → 결과 확인까지 실제 코드로 동작하는 완전한 시나리오

---

## 사전 조건

- 사용자: "김개발", Android 14
- 앱 최초 설치 (DB 비어 있음, `smart_clipboard.db` 없음 → v5로 새로 생성)
- Quick Settings Tile: 설정에서 SmartClipboardAI Tile을 빠른 설정 패널에 추가 완료
- MediaStore 권한: 아직 허용 안 함

---

## 0. 앱 아이콘을 탭한다 — "처음 열어본 앱"

### 화면

```
┌──────────────────────────────────────┐
│  Smart Clipboard AI                  │
├──────────────────────────────────────┤
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 무엇을 정리할까요?            │  │
│  │ [                        ]    │  │
│  │ [AI가 찾아주기] [직접 고르기] │  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 오늘 수집                     │  │
│  │ 0개 수집됨                    │  │
│  │ 링크 0 · 메모 0 · 스크린샷 0  │  │
│  │ "오늘 모인 데이터가 생기면..." │  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 시작하기                       │  │
│  │ "데이터가 조금 더 모이면       │  │
│  │  Topic 생성과 AI 분석을        │  │
│  │  시작할 수 있어요."           │  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 스크린샷 가져오기             │  │
│  │ "이미지 접근을 허용하면..."   │  │
│  │ [권한 허용]                   │  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 최근 수집                     │  │
│  │ "공유 메뉴나 클립보드 타일로  │  │
│  │  데이터를 모아보세요."        │  │
│  └────────────────────────────────┘  │
│                                      │
├──────────────────────────────────────┤
│   🏠홈     📋데이터     ✅작업      │
└──────────────────────────────────────┘
```

### 코드 흐름

```
MainActivity.onCreate()
  → setContent { SmartClipboardTheme { MainScreen(...) } }
    → Hilt: SmartClipboardDatabase v5 생성 (knowledge_table + 기존 5개 테이블)
    → MainViewModel.init()
      → observeItems() → DataItemDao.observeAll() → 빈 리스트
      → observeTopics() → TopicDao.observeTopicSummaries() → 빈 리스트
      → observeSelectedTopicDetails() → selectedTopicId == null → 빈 리스트
    → uiState = MainUiState(isLoading = false, items = [], topics = [])
    → 화면 렌더링
```

### DB 상태
- `smart_clipboard.db` 생성됨 (room_master_table만 있음)
- 테이블: data_items(0), topics(0), topic_item_cross_refs(0), topic_analysis_results(0), topic_actions(0), knowledge_table(0)

### ✅ 잘 되는 것
- 첫 화면이 비어 있지만 깔끔함
- "데이터를 모아보세요" 메시지가 자연스러움

### ❌ 부족한 것
- 화면이 심심함. 온보딩이나 튜토리얼 없음
- "권한 허용" 버튼은 있는데, 권한 거부 시 대체 경로(SAF picker)가 없음

---

## 1. 링크를 공유해 본다 — 첫 데이터 수집

### 사용자 행동
크롬에서 블로그 글 "2026년 스마트워치 트렌드"를 읽다가, 공유 버튼 → Share Sheet에서 SmartClipboardAI 선택

### 화면 전환
```
크롬 → Share Sheet → SmartClipboardAI (투명 Activity)
                                     ↓
                              ┌─────────────┐
                              │ "저장됨"     │  ← Toast, 2초
                              └─────────────┘
                                     ↓
                              자동으로 꺼짐 (finish)
```

사용자는 다시 크롬으로 돌아옴. 앱이 자동으로 열리지는 않음.

### 코드 흐름

```
ShareReceiverActivity.onCreate()
  → intent.action == ACTION_SEND
  → AndroidShareContentHandler.handleShare(intent)
    → text/plain + url 패턴 감지
    → dataRepository.addLink(
        url = "https://.../smartwatch-trends-2026",
        title = "2026년 스마트워치 트렌드",
        source = "share"
      )
      → DataItemEntity(type="LINK", content=url, title=title, source="share", createdAt=now)
      → dataItemDao.insert(entity)   // id=1
  → Toast "저장되었습니다"
  → finish()
```

### DB 상태
| data_items |
|---|
| id=1, type=LINK, content="https://...", title="2026년 스마트워치 트렌드", source="share" |

### ✅ 잘 되는 것
- Flow 기반 observeItems() 때문에, 앱을 다시 열면 자동으로 새 데이터가 보임
- source="share"로 구분 가능

### ❌ 부족한 것
- Toast가 금방 사라짐 → "뭐가 저장된 거지?" 놓치기 쉬움
- 앱이 자동으로 열리지 않음 → 사용자가 수동으로 앱을 열어야 함
- 저장된 링크의 OG 태그(제목/설명/이미지)를 추출하는 WebExtractor가 아직 없음 → 링크 제목만 표시됨

---

## 2. 문자를 복사하고 Tile을 누른다 — 두 번째 데이터

### 사용자 행동
카카오톡에서 "다음 주 화요일 오후 2시, 스마트워치 리뷰 미팅" 메시지를 본다.
쓸모 있다고 생각 → 텍스트를 복사 → 상태바 내려서 Quick Settings Tile 탭

### 화면 전환

```
메신저 → 복사 → 알림창 내림 → Tile 탭
                                    ↓
                           (0.3초 깜빡, 아무 메시지 없음)
                                    ↓
                           원래 앱으로 복귀
```

**아무 피드백이 없다.** 저장됐는지 실패했는지 알 수 없음.

### 코드 흐름

```
ClipboardCaptureTileService.onClick()
  → startActivity(ClipboardCaptureActivity)

ClipboardCaptureActivity.onCreate()
  → clipboardManager.getPrimaryClip().getItemAt(0).getText()
  → text = "다음 주 화요일 오후 2시, 스마트워치 리뷰 미팅"
  → dataRepository.addText(text, source="clipboard_tile")
    → DataItemEntity(type="TEXT", content=text, source="clipboard_tile", createdAt=now)
    → dataItemDao.insert(entity)   // id=2
  → finish()
```

### DB 상태
| data_items |
|---|
| id=1, type=LINK, title="2026년 스마트워치 트렌드" |
| id=2, type=TEXT, content="다음 주 화요일 오후 2시, 스마트워치 리뷰 미팅" |

### ✅ 잘 되는 것
- 복사 → Tile 탭 → 저장이 빠름
- source="clipboard_tile"로 구분 가능

### ❌ 부족한 것
- 저장 피드백이 **전혀** 없음. 성공/실패/중복 여부 불명
- Tile 누른 후 자동으로 앱이 열리지 않음

---

## 3. 앱을 다시 연다 — 데이터가 2개 쌓였다

### 사용자 행동
Tile로 저장한 게 잘 됐는지 궁금해서 앱을 다시 연다

### 화면

```
┌──────────────────────────────────────┐
│  Smart Clipboard AI                  │
├──────────────────────────────────────┤
│  ┌────────────────────────────────┐  │
│  │ 무엇을 정리할까요?            │  │
│  │ [                        ]    │  │
│  │ [AI가 찾아주기] [직접 고르기] │  │  ← 둘 다 활성화됨!
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 오늘 수집                     │  │
│  │ 2개 수집됨                    │  │
│  │ 링크 1 · 메모 1 · 스크린샷 0  │  │
│  │ "필요한 데이터를 직접 고르거나 │  │
│  │  주제를 입력해 AI 후보 선택을  │  │
│  │  시작하세요."                  │  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 시작하기                       │  │
│  │ "데이터를 선택해 Topic으로    │  │
│  │  만들고, AI가 분석과 액션     │  │
│  │  초안을 생성합니다."          │  │
│  │ [데이터 선택 → Topic 만들기]  │  │  ← CTA 등장!
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 최근 수집                     │  │
│  │  메모 │ "다음 주 화요일..."   │  │
│  │        │ 클립보드 · 오후 1:23 │  │
│  │  링크 │ 2026년 스마트워치...  │  │
│  │        │ 공유 · 오후 1:20     │  │
│  └────────────────────────────────┘  │
└──────────────────────────────────────┘
```

### 코드 흐름
```
MainActivity.onResume()
  → ViewModel scope 살아있음
  → observeItems() Flow가 자동으로 새 데이터 방출
  → uiState 갱신: items = [id=2 TEXT, id=1 LINK]
```

### ✅ 잘 되는 것
- 데이터가 쌓이자 CTA가 바뀌었다: "데이터 선택 → Topic 만들기"
- 출처가 "공유"/"클립보드"로 표시돼서 어디서 왔는지 알 수 있음

### ❌ 부족한 것
- 아이템 정렬이 `createdAt DESC`(최신순) → 방금 저장한 게 위에 올라와서 자연스러움
- 하지만 아직 Cluster 정보가 없어서 "스마트워치"라는 공통 키워드로 묶어주지 못함

---

## 4. "직접 고르기" → 데이터 선택 → Topic 생성

### 사용자 행동
"데이터 선택 → Topic 만들기" 버튼을 누름 → DATA 화면으로 전환 → 자동으로 선택 모드 진입

### 화면

```
┌──────────────────────────────────────┐
│  데이터 선택                   취소 │
├──────────────────────────────────────┤
│  2개 전체 · 텍스트 1 · 링크 1 · ... │
│                                      │
│  [전체] [메모] [링크] [이미지] ...   │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 선택한 데이터 0개             │  │
│  │ [보이는 것 선택]              │  │
│  └────────────────────────────────┘  │
│                                      │
│  ☐ 메모 │ 다음 주 화요일 오후 2시...│  │
│          │ 클립보드 · 오후 1:23      │  │
│                                      │
│  ☐ 링크 │ 2026년 스마트워치 트렌드  │  │
│          │ 공유 · 오후 1:20          │  │
│                                      │
├──────────────────────────────────────┤
│  선택 0개            [취소]  [다음]  │
└──────────────────────────────────────┘
```

사용자가 둘 다 체크 → "선택 2개" → "다음" 클릭

### 화면 (BottomSheet)

```
┌──────────────────────────────────────┐
│  주제로 묶고 분석 시작              │
│                                      │
│  2개 데이터를 하나의 주제에 붙이고  │
│  AI Agent 분석 파이프라인으로        │
│  넘깁니다.                           │
│                                      │
│  주제명                              │
│  ┌────────────────────────────────┐  │
│  │ 스마트워치 트렌드 조사         │
│  └────────────────────────────────┘  │
│                                      │
│  [주제 생성 + 분석 시작]             │
│  [주제에만 추가]                     │
│  [닫기]                              │
└──────────────────────────────────────┘
```

사용자가 주제명을 "스마트워치 트렌드 조사"로 수정 → "주제 생성 + 분석 시작" 클릭

### 코드 흐름

```
BottomSheet → AddSelectionToTopicAndAnalyze
  → isRunningTopicAnalysis = true

  → dataRepository.addItemsToTopic("스마트워치 트렌드 조사", [1, 2], "USER")
    → TopicEntity(id=1, title="스마트워치 트렌드 조사", createdAt/updatedAt=now)
    → TopicItemCrossRefEntity(topicId=1, itemId=1), (topicId=1, itemId=2)
    → topicId = 1

  → dataRepository.runTopicAnalysis(topicId = 1)
    → TopicDao.observeItemsForTopic(1) → [DataItem(id=1, LINK), DataItem(id=2, TEXT)]
    → TopicDao.getTopicById(1) → TopicSummaryRow(1, "스마트워치 트렌드 조사", 2, ...)
    → topicAgent.analyze(topic, items, userInstruction=null)
      → GeminiTopicAgent.buildPrompt(...)
      → geminiManager.run(prompt)
        → okhttp POST Gemini API
```

### ✅ 잘 되는 것
- 선택 → 주제명 입력 → 분석까지 한 번에 연결
- BottomSheet가 화면을 가리지 않음

### ❌ 부족한 것
- **userInstruction이 전달되지 않음** — UI에 입력창이 없음. 예: "요약 노트만 만들어줘"
- 주제명이 "링크 + 메모 정리" 같은 임의 생성 제목으로 미리 채워짐. 좀 더 똑똑하게 만들 수 있음

---

## 5. AI 분석 중 — 로딩 상태

### 화면

Topic 생성 직후, 분석이 실행되는 동안:

```
┌──────────────────────────────────────┐
│  ← 작업    스마트워치 트렌드 조사  │  ← TOPIC_DETAIL 모드로 전환
├──────────────────────────────────────┤
│  스마트워치 트렌드 조사             │
│  자료 2개 · 분석 0개 · 작업 0개     │
│  [데이터 추가]  [분석 실행]         │
│                                      │
│  [자료] [분석] [작업]               │
├──────────────────────────────────────┤
│                                      │
│  ┌────────────────────────────────┐  │
│  │ ⟳ AI Agent가 선택한 자료를    │  │
│  │   분석하고 있어요.             │  │
│  └────────────────────────────────┘  │
│                                      │
└──────────────────────────────────────┘
```

### 코드 흐름

```
runTopicAnalysis() 실행 중...
  → Gemini API 호출 (2~5초)
  → isRunningTopicAnalysis = true → 로딩 스피너 표시

성공 시:
  → AgentJsonParser.parse() → AgentResult(
      topicId = 1,
      summary = "스마트워치 트렌드 관련 링크와 미팅 일정이 포함된 자료입니다.",
      keyPoints = ["스마트워치 트렌드 블로그", "화요일 오후 2시 미팅 일정", "리뷰 미팅 관련"],
      sourceItemIds = [1, 2],
      actions = [
        AgentActionDraft(SUMMARY, 0.9, "자료 정리 노트", "스마트워치 트렌드 조사 요약", "...", "{app:NOTES,...}", [1,2]),
        AgentActionDraft(CALENDAR, 0.7, "미팅 일정 발견", "스마트워치 리뷰 미팅", "...", "{app:CALENDAR,...}", [2]),
        AgentActionDraft(TODO, 0.5, "추가 조사 필요", "트렌드 자료 더 찾아보기", "...", null, [1])
      ]
    )
  → TopicAnalysisEntity 저장 (id=1)
  → TopicActionEntity 3개 저장 (SUMMARY, CALENDAR, TODO)
  → isRunningTopicAnalysis = false
  → selectedTopicTab = ANALYSIS
```

### ❌ 부족한 것
- Gemini API 호출이 2~5초 걸리는데, 중간 취소 UI 없음
- "프롬프트 생성 중..." → "API 요청 중..." → "결과 파싱 중..." 같은 진행 상태 없음

---

## 6. 분석 결과 확인 — ANALYSIS 탭

### 화면

```
┌──────────────────────────────────────┐
│  ← 작업    스마트워치 트렌드 조사   │
├──────────────────────────────────────┤
│  스마트워치 트렌드 조사             │
│  자료 2개 · 분석 1개 · 작업 3개     │
│  [데이터 추가]  [분석 실행]         │
│                                      │
│  [자료] [분석] [작업]               │
├──────────────────────────────────────┤
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 분석 결과              5월 29일│  │
│  │                                │  │
│  │ 스마트워치 트렌드 관련 링크와 │  │
│  │ 미팅 일정이 포함된 자료입니다. │  │
│  │                                │  │
│  │ • 스마트워치 트렌드 블로그     │  │
│  │ • 화요일 오후 2시 미팅 일정    │  │
│  │ • 리뷰 미팅 관련               │  │
│  └────────────────────────────────┘  │
│                                      │
└──────────────────────────────────────┘
```

### ✅ 잘 되는 것
- 요약 + 키포인트가 한눈에 들어옴
- AI가 생성한 자연스러운 문장이 표시됨 (Gemini 진짜 응답)

---

## 7. 작업(ACTION) 탭 — AI가 제안한 액션 확인

### 화면

사용자가 "작업" 탭을 누름

```
┌──────────────────────────────────────┐
│  ← 작업    스마트워치 트렌드 조사   │
├──────────────────────────────────────┤
│  [자료] [분석] [작업]               │
├──────────────────────────────────────┤
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 요약          초안             │  │
│  │ 작업명                         │  │
│  │ [스마트워치 트렌드 조사 요약]  │  │
│  │ 초안                           │  │
│  │ [AI가 생성한 요약 본문...]     │  │
│  │ [공유 초안]                    │  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 일정          초안             │  │
│  │ 작업명                         │  │
│  │ [스마트워치 리뷰 미팅]         │  │
│  │ 초안                           │  │
│  │ [미팅 일정 설명...]            │  │
│  │ [캘린더 열기]                  │  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 할 일         초안             │  │
│  │ 작업명                         │  │
│  │ [트렌드 자료 더 찾아보기]     │  │
│  │ 초안                           │  │
│  │ [추가 조사 설명...]            │  │
│  │ [실행 준비 중] (비활성)        │  │
│  └────────────────────────────────┘  │
│                                      │
└──────────────────────────────────────┘
```

사용자는 SUMMARY 액션의 "공유 초안"을 탭 → Android Share Sheet 열림 → Samsung Notes 선택 → Notes에 초안 전달됨

사용자는 CALENDAR 액션의 "캘린더 열기"를 탭 → Calendar 앱의 일정 작성 화면 열림 → 사용자가 시간을 직접 설정 후 저장

### 코드 흐름 (공유 초안)

```
"공유 초안" 클릭
  → MainActivity의 onShareDraft 람다 호출
    → Intent.ACTION_SEND + text/plain
    → startActivity(Intent.createChooser(...))
  → HandoffActionCompleted("공유 초안을 열었어요")
  → snackbarMessage 표시
```

### 코드 흐름 (캘린더)

```
"캘린더 열기" 클릭
  → MainActivity의 onCreateCalendarDraft 람다 호출
    → Intent.ACTION_INSERT + Events.CONTENT_URI
    → putExtra(Events.TITLE, action.title)
    → putExtra(Events.DESCRIPTION, action.body)
    → startActivity(intent)
  → HandoffActionCompleted("캘린더 초안을 열었어요")
```

### ✅ 잘 되는 것
- 각 액션 타입에 맞는 외부 앱 연동이 구현되어 있음
- 사용자 검토 후 수동으로 수정하고 전달 가능
- 액션 제목/본문을 바로 편집할 수 있음

### ❌ 부족한 것
- Calendar의 `editablePayload`에 `startTime`, `endTime`, `location`이 있는데, 이걸 Intent에 채우지 못함
- payload JSON 파싱은 HandoffDraftFormatter에 일부만 되어 있음
- REMINDER/TODO는 버튼이 비활성화되어 있어 "이건 뭐지?" 하는 느낌

---

## 8. Gemini API 실패 시 — 오류 처리

### 시나리오

와이파이가 끊겼거나, API 키가 만료됨 → "분석 실행" 누름

### 화면

```
┌──────────────────────────────────────┐
│  ← 작업     스마트워치 트렌드 조사  │
├──────────────────────────────────────┤
│  [자료] [분석] [작업]               │
├──────────────────────────────────────┤
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 아직 분석 결과가 없어요        │  │
│  │ 선택한 데이터로 요약과 액션    │  │
│  │ 초안을 생성할 수 있어요.      │  │
│  │ [분석 시작]                    │  │
│  └────────────────────────────────┘  │
│                                      │
└──────────────────────────────────────┘
```

### 코드 흐름

```
GeminiTopicAgent.analyze() → Result.failure(exception)
  → DataRepositoryImpl.runTopicAnalysis()
    → result.onFailure { Log.e(...) }   // 로그만 찍고 끝!
    → topicDao.updateTopicTimestamp()
  → isRunningTopicAnalysis = false
  → snackbarMessage = "분석 결과를 업데이트했어요"  ← 성공 메시지가 그대로 나옴!
```

### ❌ 심각한 문제
- 분석 실패 시 `snackbarMessage = "분석 결과를 업데이트했어요"`가 그대로 나옴
- 실패 여부를 확인하지 않고 항상 성공 메시지를 표시함
- `onFailure` 블록에서 `Log.e`만 하고 UI에 실패를 알리지 않음

**실제 코드 (`MainViewModel.kt` 511~533행)**:
```kotlin
is MainIntent.RunSelectedTopicAnalysis -> {
    val topicId = _uiState.value.selectedTopicId ?: return
    viewModelScope.launch {
        _uiState.update { it.copy(isRunningTopicAnalysis = true) }
        try {
            dataRepository.runTopicAnalysis(topicId)  // ← 실패해도 예외를 던지지 않음
            _uiState.update {
                it.copy(
                    selectedTopicTab = TopicDetailTab.ANALYSIS,
                    isRunningTopicAnalysis = false,
                    snackbarMessage = "분석 결과를 업데이트했어요"  // ← 항상 이 메시지
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isRunningTopicAnalysis = false,
                    snackbarMessage = "분석을 실행하지 못했어요"
                )
            }
        }
    }
}
```

---

## 전체 시나리오 요약: 잘 되는 것과 안 되는 것

### ✅ 잘 되는 것

| 단계 | 설명 |
|------|------|
| 데이터 수집 | Share + Tile 두 경로 모두 동작 |
| 실시간 갱신 | Flow 기반 → 새 데이터가 자동으로 UI에 반영됨 |
| Topic 생성 | 데이터 선택 → 주제 입력 → 분석까지 원클릭 흐름 |
| Gemini 분석 | 실제 API 호출, JSON 파싱, Hallucination 방어 |
| 액션 출력 | SUMMARY/CALENDAR/TODO 분류, 초안 편집 가능 |
| 외부 앱 연동 | Calendar/Share Intent로 초안 전달 가능 |

### ❌ 부족한 것

| 문제 | 심각도 | 영향 |
|------|--------|------|
| **분석 실패 시 "성공" 메시지** | 🔴 치명 | Gemini 오류 시 사용자는 실패를 모름. "분석 결과를 업데이트했어요"라고 거짓말 |
| **저장 피드백 없음** (Tile) | 🔴 치명 | Tile로 저장 시 아무 피드백도 없음 |
| **userInstruction 미구현** | 🟡 UX | 사용자가 "어떻게 분석할지" 지시 불가 |
| **앱 자동 열림 없음** | 🟡 UX | Share/Tile 후 수동으로 앱을 열어야 함 |
| **분석 취소 불가** | 🟡 UX | 2~5초 기다리는 동안 아무것도 못 함 |
| **Calendar payload 미사용** | 🟡 기능 | AI가 startTime/endTime을 생성해도 Intent에 안 채움 |
| **Cluster 미구현** | 🟡 기능 | 비슷한 데이터끼리 자동 묶어주지 못함 |
| **OG/OCR 미구현** | 🟡 기능 | 링크 제목만 표시, 이미지 텍스트 추출 안 됨 |
| **REMINDER 버튼 비활성** | 🟡 기능 | AI가 REMINDER를 생성해도 실행 방법이 없음 |
| **온보딩 없음** | 🟢 UX | 첫 실행 시 뭘 해야 하는지 모름 |