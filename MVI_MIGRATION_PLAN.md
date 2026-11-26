# FeedPluse — MVVM to MVI Migration Plan

> **Scope:** Migrate the existing MVVM architecture (Phases 1–9) to **MVI (Model–View–Intent)** with Unidirectional Data Flow (UDF).
> **Note:** Phases 10 (Article Detail) and 11 (OpenAI GPT Summarization) are kept for later and will plug directly into the MVI `Effect` and `Intent` system once this migration is complete.

---

## Why Migrate from MVVM to MVI?

| Aspect | Current MVVM | Target MVI |
|---|---|---|
| **UI State** | `StateFlow<Resource>` (requires `as List<...>` cast in UI) | Strongly typed, immutable `data class` (`ArticleUiState`, `SourceUiState`) |
| **User Actions** | Direct ViewModel method calls (`fetchArticles()`) | Explicit `Intent` sealed interface routed through `onIntent(intent)` |
| **Pull-to-Refresh State** | Local `remember { mutableStateOf(false) }` in Composable | Managed cleanly inside `UiState.isRefreshing` |
| **One-Time Events** | Not yet separated from state | `Channel` → `Flow<Effect>` (navigation, snackbars) that never replay on rotation |

---

## MVI Data Flow

```
┌──────────────────────────────────────────────────────────┐
│                     Compose Screen                       │
│   observes StateFlow<UiState>  &  collects Flow<Effect>  │
└──────────────┬───────────────────────────▲───────────────┘
               │ dispatches Intent         │ emits State / Effect
               ▼                           │
┌──────────────────────────────────────────┴───────────────┐
│                       ViewModel                          │
│  fun onIntent(intent: Intent)                            │
│  updates _uiState.update { it.copy(...) }                │
│  sends one-off events to _effect.send(...)               │
└──────────────┬───────────────────────────────────────────┘
               │ calls
               ▼
┌──────────────────────────────────────────────────────────┐
│                Domain & Data Layers                      │
│  (GetArticlesUseCase, GetSourcesUseCase, Repositories)   │
│  — Unchanged! Clean Architecture stays intact.           │
└──────────────────────────────────────────────────────────┘
```

---

## Migration Phases (Step by Step)

### Phase M1 — Define Feature Contracts (`State`, `Intent`, `Effect`)

**Goal:** Replace untyped `Resource` UI state with explicit, type-safe MVI contracts for each feature.

#### Step M1.1: Create `ArticleContract.kt`
- **File:** `feature/articles/presentation/ArticleContract.kt`
- Contains:
  - `data class ArticleUiState`:
    ```kotlin
    data class ArticleUiState(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val articles: List<ArticleData> = emptyList(),
        val errorMessage: String? = null
    )
    ```
  - `sealed interface ArticleIntent`:
    - `data object LoadArticles : ArticleIntent`
    - `data object RefreshArticles : ArticleIntent`
    - `data object RetryArticles : ArticleIntent`
    - `data class ArticleClicked(val article: ArticleData) : ArticleIntent`
  - `sealed interface ArticleEffect`:
    - `data class NavigateToDetail(val article: ArticleData) : ArticleEffect` *(ready for Phase 10)*
    - `data class ShowError(val message: String) : ArticleEffect`

#### Step M1.2: Create `SourceContract.kt`
- **File:** `feature/sources/presentation/SourceContract.kt`
- Mirrors `ArticleContract.kt`:
  - `data class SourceUiState(isLoading, isRefreshing, sources, errorMessage)`
  - `sealed interface SourceIntent` (`LoadSources`, `RefreshSources`, `RetrySources`)
  - `sealed interface SourceEffect` (`ShowError`)

**✅ Done when:** Both contract files compile with zero dependencies on Android UI.

---

### Phase M2 — Migrate `ArticleViewModel` & `ArticleScreen`

**Goal:** Convert the Articles presentation layer to pure Unidirectional Data Flow.

#### Step M2.1: Update `ArticleViewModel.kt`
- Replace `MutableStateFlow<Resource>` with `MutableStateFlow(ArticleUiState())`.
- Add `Channel<ArticleEffect>(Channel.BUFFERED)` + `val effect = _effect.receiveAsFlow()`.
- Replace public `fetchArticles()` with single entry point:
  ```kotlin
  fun onIntent(intent: ArticleIntent) {
      when (intent) {
          ArticleIntent.LoadArticles,
          ArticleIntent.RetryArticles -> loadArticles(isRefresh = false)
          ArticleIntent.RefreshArticles -> loadArticles(isRefresh = true)
          is ArticleIntent.ArticleClicked -> emitEffect(ArticleEffect.NavigateToDetail(intent.article))
      }
  }
  ```
- Update state using `_uiState.update { it.copy(...) }` (now `.copy()` works natively!).

#### Step M2.2: Update `ArticleScreen.kt`
- Remove local `var isRefreshing by remember { mutableStateOf(false) }` — read `uiState.isRefreshing` directly.
- Remove `when (uiState)` over `Resource` and `as? List<ArticleData>` casting.
- Render directly from `uiState.isLoading`, `uiState.errorMessage`, and `uiState.articles`.
- Dispatch `viewModel.onIntent(ArticleIntent.RefreshArticles)`, `viewModel.onIntent(ArticleIntent.RetryArticles)`, and `viewModel.onIntent(ArticleIntent.ArticleClicked(article))`.
- Collect `viewModel.effect` inside `LaunchedEffect(Unit)` for one-off navigation/snackbar events.

**✅ Done when:** Articles screen behaves identically on emulator/device using `ArticleUiState` + `ArticleIntent`.

---

### Phase M3 — Migrate `SourceViewModel` & `SourceScreen`

**Goal:** Apply the exact same MVI pattern to the Sources feature.

#### Step M3.1: Update `SourceViewModel.kt`
- Replace `MutableStateFlow<Resource>` with `MutableStateFlow(SourceUiState())`.
- Add `_effect` `Channel<SourceEffect>` and `onIntent(intent: SourceIntent)`.

#### Step M3.2: Update `SourceScreen.kt`
- Remove local `isRefreshing` state and `Resource` casting.
- Read `uiState.isLoading`, `uiState.isRefreshing`, `uiState.errorMessage`, and `uiState.sources`.
- Dispatch `SourceIntent.RefreshSources` and `SourceIntent.RetrySources`.

**✅ Done when:** Sources screen works end-to-end with MVI.

---

### Phase M4 — Optional Domain/Core Cleanup

**Goal:** Decide whether `Resource` is still needed in the Domain/Data layers.

- **Option A (Keep `Resource` in Data/Domain):** Minimal diff — `Repository` and `UseCase` keep returning `Resource`, and the ViewModel maps `Resource.Success` / `Resource.Error` into `UiState`.
- **Option B (Type-safe `Result<T>` in Data/Domain):** Replace `Resource` with Kotlin's built-in `Result<List<ArticleData>>` / `Result<List<SourceData>>` so there is zero `Any` casting anywhere in the app.

---

### Phase M5 — Update Unit Tests for MVI

**Goal:** Update ViewModel tests to assert `UiState` properties and `Effect` emissions.

#### Step M5.1: Update `ArticleViewModelTest.kt`
- Verify initial `LoadArticles` transitions `isLoading = true` → `articles = sampleArticles, isLoading = false`.
- Verify `RetryArticles` recovers from `errorMessage` to populated `articles`.
- Verify `RefreshArticles` toggles `isRefreshing` without wiping existing `articles`.
- Verify `ArticleClicked` emits `ArticleEffect.NavigateToDetail`.

#### Step M5.2: Update `SourceViewModelTest.kt`
- Verify `LoadSources`, `RefreshSources`, and `RetrySources` state transitions.

**✅ Done when:** `./gradlew :app:testDebugUnitTest` passes with all MVI tests green.

---

## Future Phases (Deferred)

- **Phase 10 — Article Detail Screen:** Triggered cleanly via `ArticleEffect.NavigateToDetail`.
- **Phase 11 — AI Summarization (OpenAI GPT):** Adds `SummarizeIntent` + `summary` field to `DetailUiState`.
