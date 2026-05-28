# Gemini 파이프라인 MVVM 재배치 계획

> **목적**: 친구가 작성한 Gemini/OCR/Knowledge 관련 코드를 기존 MVVM 구조에 맞게 올바른 위치로 재배치하고, Hilt DI로 연결하며, 이중 DB 문제를 해결합니다.

---

## 1. 현황 진단

### 1-1. 현재 위치 (잘못된 곳에 있음)

```
com.samsung.smartclipboard/               ← 루트 패키지 (모든 게 날것으로 흩어짐)
├── AppDatabase.kt                        ← 별도 Room DB (세상에 DB가 2개)
│   ├── InputType enum                    ← enum이 DB 파일 안에...
│   ├── RoomConverters                    ← TypeConverter
│   ├── KnowledgeEntity                   ← @Entity (knowledge_table)
│   ├── KnowledgeDao                      ← @Dao
│   ├── AppDatabase                       ← @Database (version 1, entities = [KnowledgeEntity])
│   ├── LlmStructuredOutput               ← @Serializable 모델
│   └── normalizeGroupKey()               ← 유틸 함수
├── GeminiManager.kt                      ← okhttp로 Gemini API 직접 호출 (API 키 하드코딩)
├── GeminiClient.kt                       ← GeminiManager 감싸서 JSON 파싱
├── SourceExtractor.kt                    ← ML Kit OCR + Jsoup URL 추출
└── SmartClipboardApp.kt                  ← @HiltAndroidApp (정상)

presentation/main/
└── KnowledgeViewModel.kt                 ← Hilt 없음, AppDatabase 직접 생성
```

### 1-2. 문제점

| 문제 | 심각도 | 설명 |
|------|--------|------|
| DB 2개 | 🔴 치명 | `SmartClipboardDatabase`(smart_clipboard.db, v4) + `AppDatabase`(friends db, v1). 앱에 Room DB가 2개 공존 |
| DI 없음 | 🔴 치명 | `GeminiClient`, `GeminiManager`, `SourceExtractor`, `KnowledgeViewModel` 전부 Hilt 주입 안 됨. 그냥 `new`로 생성 |
| API 키 하드코딩 | 🔴 치명 | `GeminiManager.kt` 15행에 `API_KEY`가 그대로 노출. GitHub에 올라가면 토큰 탈취됨 |
| 패키지 위치 | 🟡 구조 | 전부 루트 패키지에 흩어짐. `domain/ai/`, `data/ai/`, `data/source/local/` 로 나뉘어야 함 |
| Room 버전 충돌 | 🟡 구조 | `SmartClipboardDatabase`는 version 4, `AppDatabase`는 version 1. migration 정리 필요 |

### 1-3. 기존 MVVM 구조에서 재사용할 것

| 기존 요소 | 위치 | 상태 |
|-----------|------|------|
| `di/AiModule.kt` | `di/` | `AiProposalGenerator`만 바인딩 중 → 여기에 Gemini 관련 바인딩 추가 |
| `di/AppModule.kt` | `di/` | `SmartClipboardDatabase` + DAO 제공 → KnowledgeDao 추가 |
| `SmartClipboardDatabase` | `data/source/local/` | version 4 → v5로 올려 KnowledgeEntity 통합 |
| `DataRepositoryImpl` | `data/repository/` | Gemini 분석을 언젠가 호출할 진입점 |

---

## 2. 목표 구조 (MVVM)

```
domain/
├── model/
│   ├── LlmStructuredOutput.kt            ← @Serializable 모델 (AppDatabase에서 이동)
│   └── InputType.kt                      ← enum (AppDatabase에서 분리)
├── ai/
│   ├── GeminiManager.kt                  ← INTERFACE (suspend fun run(prompt: String): String)
│   ├── GeminiClient.kt                   ← INTERFACE (suspend fun refineText(type, text): LlmStructuredOutput)
│   └── SourceExtractor.kt                ← INTERFACE (suspend fun extractFromOcr/Source/Url)
└── repository/
    └── KnowledgeRepository.kt            ← INTERFACE (suspend fun organize(): List<String>)

data/
├── model/
│   └── KnowledgeEntity.kt               ← @Entity (AppDatabase에서 이동)
├── ai/
│   ├── DefaultGeminiManager.kt           ← okhttp 구현
│   ├── DefaultGeminiClient.kt            ← GeminiClient 구현
│   └── DefaultSourceExtractor.kt         ← ML Kit OCR + Jsoup 구현
├── source/local/
│   ├── SmartClipboardDatabase.kt         ← version 4→5. KnowledgeEntity 추가, AppDatabase 제거
│   └── KnowledgeDao.kt                   ← AppDatabase에서 분리
└── repository/
    └── KnowledgeRepositoryImpl.kt        ← KnowledgeDao + GeminiClient + SourceExtractor 조합

di/
└── AiModule.kt                           ← 확장: GeminiManager, GeminiClient, SourceExtractor, KnowledgeRepository 바인딩 추가

presentation/main/
└── KnowledgeViewModel.kt                 ← @HiltViewModel, @Inject로 DI 받도록 수정
```

---

## 3. 단계별 실행 계획

### 3-1. 1단계: 도메인 모델 정리 (충돌 없는 순수 이동)

**작업 내용**: 루트 패키지에 흩어진 모델/enum/인터페이스를 도메인 계층으로 이동

**변경 파일**:

| 작업 | 파일 | 설명 |
|------|------|------|
| 새 파일 | `domain/model/LlmStructuredOutput.kt` | `AppDatabase.kt`에서 `LlmStructuredOutput` 이동 |
| 새 파일 | `domain/model/InputType.kt` | `AppDatabase.kt`에서 `InputType` enum 분리 |
| 새 파일 | `domain/ai/GeminiManager.kt` | 인터페이스: `suspend fun run(prompt: String): String` |
| 새 파일 | `domain/ai/GeminiClient.kt` | 인터페이스: `suspend fun refineText(type: InputType, text: String): LlmStructuredOutput` |
| 새 파일 | `domain/ai/SourceExtractor.kt` | 인터페이스: `suspend fun extractFromOcr(uri: String): String`, `suspend fun extractFromUrl(url: String): String` |
| 새 파일 | `domain/repository/KnowledgeRepository.kt` | 인터페이스: `suspend fun organize(): List<String>` |
| 수정 | `GeminiClient.kt` (루트) | import 도메인 인터페이스/모델로 변경 |
| 수정 | `GeminiManager.kt` (루트) | import 도메인 인터페이스로 변경 |
| 수정 | `SourceExtractor.kt` (루트) | import 도메인 인터페이스로 변경 |
| 수정 | `AppDatabase.kt` (루트) | `LlmStructuredOutput`, `InputType` 제거하고 import 도메인에서 |

**이 단계는 구현 코드 동작을 바꾸지 않고 import 경로만 정리합니다.**

---

### 3-2. 2단계: 데이터 계층 구현 + DB 통합

**작업 내용**: 
1. `KnowledgeEntity` + `KnowledgeDao`를 `SmartClipboardDatabase`(v4→v5)에 통합
2. `AppDatabase` (friends db) 제거
3. okhttp/OCR/Jsoup 구현체를 `data/ai/` 아래에 생성

**변경 파일**:

| 작업 | 파일 | 설명 |
|------|------|------|
| 새 파일 | `data/model/KnowledgeEntity.kt` | `AppDatabase.kt`에서 `KnowledgeEntity` 이동 (패키지 변경) |
| 새 파일 | `data/source/local/KnowledgeDao.kt` | `AppDatabase.kt`에서 `KnowledgeDao` 분리 |
| 수정 | `data/source/local/SmartClipboardDatabase.kt` | version 4→5, `KnowledgeEntity` 추가, `KnowledgeDao` 추가, `MIGRATION_4_5` 추가 (knowledge_table 생성) |
| 수정 | `di/AppModule.kt` | `provideKnowledgeDao()` 추가 |
| 새 파일 | `data/ai/DefaultGeminiManager.kt` | `GeminiManager.kt`(루트)를 impl로 이동, API 키는 BuildConfig/local.properties에서 주입 |
| 새 파일 | `data/ai/DefaultGeminiClient.kt` | `GeminiClient.kt`(루트)를 impl로 이동 |
| 새 파일 | `data/ai/DefaultSourceExtractor.kt` | `SourceExtractor.kt`(루트)를 impl로 이동 |
| 새 파일 | `data/repository/KnowledgeRepositoryImpl.kt` | `KnowledgeDao` + `GeminiClient` + `SourceExtractor` 조합 |
| 삭제 | `AppDatabase.kt` (루트) | 내용물이 모두 이동되었으므로 삭제 |
| 수정 | `GeminiManager.kt` (루트) | `DefaultGeminiManager`로 내용 이동 후 삭제 |
| 수정 | `GeminiClient.kt` (루트) | `DefaultGeminiClient`로 내용 이동 후 삭제 |
| 수정 | `SourceExtractor.kt` (루트) | `DefaultSourceExtractor`로 내용 이동 후 삭제 |

**API 키 처리 방안**:
```
// DefaultGeminiManager.kt
class DefaultGeminiManager @Inject constructor(
    @Named("gemini_api_key") private val apiKey: String
) : GeminiManager {
    // okhttp 호출 시 apiKey 사용
}
```
`di/AiModule.kt`에서 API 키 제공. MVP에서는 `BuildConfig` 필드로 임시 주입, 이후 CI/CD 구축 시 환경변수로 교체.

**MIGRATION_4_5**:
```sql
CREATE TABLE IF NOT EXISTS knowledge_table (
    id TEXT PRIMARY KEY NOT NULL,
    type TEXT NOT NULL,
    source TEXT NOT NULL,
    title TEXT NOT NULL,
    topic TEXT NOT NULL,
    purpose TEXT NOT NULL,
    summary TEXT NOT NULL,
    keywords TEXT NOT NULL,
    content TEXT NOT NULL,
    groupKey TEXT NOT NULL,
    groupReason TEXT NOT NULL,
    createdAt INTEGER NOT NULL
);
```

---

### 3-3. 3단계: DI 모듈 등록

**작업 내용**: Hilt가 모든 의존성을 주입할 수 있도록 모듈 확장

**변경 파일**:

| 작업 | 파일 | 설명 |
|------|------|------|
| 수정 | `di/AiModule.kt` | `@Binds` 추가: `GeminiManager`→`DefaultGeminiManager`, `GeminiClient`→`DefaultGeminiClient`, `SourceExtractor`→`DefaultSourceExtractor`, `KnowledgeRepository`→`KnowledgeRepositoryImpl` |
| 수정 | `di/AiModule.kt` | `@Provides` 추가: API 키 문자열 제공 |

**AiModule 최종 모습**:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds @Singleton
    abstract fun bindAiProposalGenerator(impl: HeuristicAiProposalGenerator): AiProposalGenerator

    @Binds @Singleton
    abstract fun bindGeminiManager(impl: DefaultGeminiManager): GeminiManager

    @Binds @Singleton
    abstract fun bindGeminiClient(impl: DefaultGeminiClient): GeminiClient

    @Binds @Singleton
    abstract fun bindSourceExtractor(impl: DefaultSourceExtractor): SourceExtractor

    @Binds @Singleton
    abstract fun bindKnowledgeRepository(impl: KnowledgeRepositoryImpl): KnowledgeRepository

    companion object {
        @Provides @Singleton @Named("gemini_api_key")
        fun provideGeminiApiKey(): String = BuildConfig.GEMINI_API_KEY
    }
}
```

---

### 3-4. 4단계: ViewModel 정리

**작업 내용**: `KnowledgeViewModel`을 Hilt로 DI 받도록 수정하고, `MainViewModel`과의 관계 정리

**변경 파일**:

| 작업 | 파일 | 설명 |
|------|------|------|
| 수정 | `presentation/main/KnowledgeViewModel.kt` | `@HiltViewModel`, `@Inject constructor`로 변경. `AppDatabase` 직접 생성 대신 `KnowledgeRepository` 주입 |
| 수정 (선택) | `presentation/main/MainContract.kt` | `MainUiState`에 `organizedKnowledge` 필드 추가 (KnowledgeViewModel 결과를 MainViewModel에서도 쓸 수 있게) |

**KnowledgeViewModel 최종 모습**:
```kotlin
@HiltViewModel
class KnowledgeViewModel @Inject constructor(
    private val knowledgeRepository: KnowledgeRepository
) : ViewModel() {

    private val _results = MutableStateFlow<List<String>>(emptyList())
    val results: StateFlow<List<String>> = _results

    fun organize() {
        viewModelScope.launch {
            _results.value = knowledgeRepository.organize()
        }
    }
}
```

---

## 4. 파일 변경 요약

| 유형 | 개수 | 파일 |
|------|------|------|
| 새 파일 | 11개 | 도메인 인터페이스/모델 5개, 데이터 구현 5개, Repository impl 1개 |
| 수정 파일 | 7개 | SmartClipboardDatabase, AppModule, AiModule, GeminiClient, GeminiManager, SourceExtractor, KnowledgeViewModel |
| 삭제 파일 | 1개 | AppDatabase.kt (내용물 모두 이동 후) |
| 기존 파일 건드리지 않음 | - | DataRepositoryImpl, MainScreen, MainContract, Manifest 등 |

---

## 5. 충돌 위험 분석

### 5-1. 공통 파일 보호 규칙 대비

| 보호 대상 파일 | 1~4단계에서 수정 여부 | 비고 |
|---------------|----------------------|------|
| `AndroidManifest.xml` | ❌ 안 건드림 | |
| `app/build.gradle.kts` | ❌ 안 건드림 | (단, BuildConfig 사용 시 gradle 수정 가능성 - 검토) |
| `gradle/libs.versions.toml` | ❌ 안 건드림 | |
| `DataRepository.kt` / `DataRepositoryImpl.kt` | ❌ 안 건드림 | |
| `MainScreen.kt` | ❌ 안 건드림 | |
| `MainViewModel.kt` | 🟡 선택적 (KnowledgeViewModel 통합 시) | 4단계에서만 |
| `SmartClipboardDatabase.kt` | 🔴 수정 | MIGRATION_4_5 + KnowledgeEntity 추가 |
| `di/AiModule.kt` | 🔴 수정 | Gemini 바인딩 추가 |
| `di/AppModule.kt` | 🔴 수정 | KnowledgeDao 제공 추가 |

### 5-2. 병목 체크

- `SmartClipboardDatabase.kt` 수정은 다른 DB 작업(T-140 cluster migration)과 충돌할 수 있음
- `T-140`이 아직 시작되지 않았으므로, 이번 작업에서 v4→v5 migration을 선점하는 게 좋음
- `di/AiModule.kt`는 현재 `AiProposalGenerator`만 바인딩 중 → 추가 바인딩은 충돌 없음

---

## 6. 사전 체크리스트

- [ ] `AGENTS.md` 공통 파일 보호 규칙 확인 완료
- [ ] 현재 브랜치 상태 확인 (`git status`, `git branch`)
- [ ] 작업 브랜치 생성 (예: `feat/gemini-mvvm-migration`)
- [ ] `assembleDebug` 빌드 확인 (PR 전)

---

## 7. 기대 효과

| 기대 효과 | 설명 |
|-----------|------|
| DB 단일화 | `SmartClipboardDatabase` 하나로 통합, migration 이력도 하나로 관리 |
| DI 연결 | Hilt가 모든 의존성을 관리 → 테스트 용이, ViewModel 교체 쉬움 |
| 테스트 가능 | 인터페이스 기반이므로 fake/mock 주입 가능 |
| API 키 보호 | `BuildConfig`로 분리 → `.gitignore`로 실제 키는 제외 가능 |
| 코드 위치 | 모든 코드가 `domain` / `data` / `presentation` / `di` 아래로 정리됨 |

---

> **작성일**: 2026-05-29
> **상태**: Draft (프로젝트 오너 승인 대기)
> **관련 코드**: `GeminiClient.kt`, `GeminiManager.kt`, `SourceExtractor.kt`, `AppDatabase.kt`, `KnowledgeViewModel.kt`