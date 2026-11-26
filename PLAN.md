# FeedPluse — MVVM Clean Architecture Implementation Plan

> **Branch:** MVVM implementation branch (already created)
> **Architecture:** MVVM + Clean Architecture (Data → Domain → Presentation)
> **Tech Stack:** Jetpack Compose · Hilt · Retrofit · OkHttp · Coil · Navigation Compose · Coroutines/Flow

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ ArticleScreen │  │ SourceScreen │  │  (Future: Detail  │  │
│  │  (Composable) │  │ (Composable) │  │   + AI Summary)  │  │
│  └──────┬───────┘  └──────┬───────┘  └──────────────────┘  │
│         │                  │                                 │
│  ┌──────┴───────┐  ┌──────┴───────┐                        │
│  │ArticleViewModel│ │SourceViewModel│                       │
│  └──────┬───────┘  └──────┬───────┘                        │
├─────────┼──────────────────┼────────────────────────────────┤
│         │    Domain Layer   │                                │
│  ┌──────┴───────┐  ┌──────┴───────┐                        │
│  │GetArticlesUseCase│ │GetSourcesUseCase│                   │
│  └──────┬───────┘  └──────┴───────┘                        │
│         │                  │                                 │
│  ┌──────┴───────┐  ┌──────┴───────┐                        │
│  │ArticleRepository│ │SourceRepository│  ← Interfaces      │
│  └──────┬───────┘  └──────┬───────┘                        │
├─────────┼──────────────────┼────────────────────────────────┤
│         │     Data Layer    │                                │
│  ┌──────┴───────┐  ┌──────┴───────┐                        │
│  │ArticleRepoImpl│ │SourceRepoImpl │  ← Implementations    │
│  └──────┬───────┘  └──────┬───────┘                        │
│         │                  │                                 │
│  ┌──────┴───────┐  ┌──────┴───────┐                        │
│  │  ArticleApi  │  │  SourceApi   │   ← Retrofit           │
│  └──────────────┘  └──────────────┘                        │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  core/network  →  Retrofit + OkHttp (Hilt Module)   │   │
│  │  core/di       →  Hilt bindings for repos           │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Package Structure (Target)

```
android.ai.feedpluse/
├── FeedPluseApp.kt                          ← @HiltAndroidApp
├── MainActivity.kt                          ← @AndroidEntryPoint, hosts NavHost + BottomBar
│
├── core/
│   ├── di/
│   │   ├── NetworkModule.kt                 ← Provides Retrofit, OkHttp, ArticleApi, SourceApi
│   │   └── RepositoryModule.kt              ← Binds repo interfaces → implementations
│   ├── network/
│   │   └── RetrofitClient.kt                ← (optional) base URL, interceptors
│   └── util/
│       └── Resource.kt                      ← sealed class: Success / Error / Loading
│
├── feature/
│   ├── articles/
│   │   ├── data/
│   │   │   ├── model/
│   │   │   │   ├── ArticleDto.kt            ← Raw API response model (@SerializedName)
│   │   │   │   └── ArticleResponse.kt       ← Wrapper: status, totalResults, articles[]
│   │   │   └── repository/
│   │   │       └── ArticleRepositoryImpl.kt  ← Implements ArticleRepository
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   └── ArticleData.kt           ← Clean domain model (imageUrl, date)
│   │   │   ├── repository/
│   │   │   │   └── ArticleRepository.kt     ← Interface
│   │   │   └── usecase/
│   │   │       └── GetArticlesUseCase.kt     ← Returns Flow<Resource<List<ArticleData>>>
│   │   └── presentation/
│   │       ├── ArticleViewModel.kt
│   │       ├── ArticleScreen.kt
│   │       └── ArticleListItem.kt
│   │
│   └── sources/
│       ├── data/
│       │   ├── model/
│       │   │   ├── SourceDto.kt
│       │   │   └── SourceResponse.kt
│       │   └── repository/
│       │       └── SourceRepositoryImpl.kt
│       ├── domain/
│       │   ├── model/
│       │   │   └── SourceData.kt
│       │   ├── repository/
│       │   │   └── SourceRepository.kt
│       │   └── usecase/
│       │       └── GetSourcesUseCase.kt
│       └── presentation/
│           ├── SourceViewModel.kt
│           ├── SourceScreen.kt
│           └── SourceListItem.kt
│
├── navigation/
│   ├── Screen.kt                            ← Sealed class / enum for routes
│   └── AppNavGraph.kt                       ← NavHost + bottom nav wiring
│
└── ui/theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

---

## API Reference

### Articles Endpoint

```
GET https://newsapi.org/v2/top-headlines?country=us&apiKey={API_KEY}
```

**Response:**
```json
{
  "status": "ok",
  "totalResults": 35,
  "articles": [
    {
      "source": { "id": "cnn", "name": "CNN" },
      "author": "...",
      "title": "...",
      "description": "...",
      "url": "https://...",
      "urlToImage": "https://...",       ← map to imageUrl
      "publishedAt": "2026-02-04T...",   ← map to date
      "content": "..."
    }
  ]
}
```

### Sources Endpoint

```
GET https://newsapi.org/v2/top-headlines/sources?apiKey={API_KEY}
```

**Response:**
```json
{
  "status": "ok",
  "sources": [
    {
      "id": "abc-news",
      "name": "ABC News",
      "description": "Your trusted source...",
      "url": "https://abcnews.go.com",
      "category": "general",
      "language": "en",
      "country": "us"
    }
  ]
}
```

---

## Implementation Phases

---

### Phase 1 — Project Foundation & Hilt Setup

**Goal:** Wire up Hilt dependency injection and create the base `Application` class so all subsequent phases can inject dependencies.

#### Step 1.1: Create `FeedPluseApp.kt`
- **File:** `android.ai.feedpluse.FeedPluseApp`
- Annotate with `@HiltAndroidApp`
- This is an empty `Application` subclass — Hilt generates the DI component from it

```kotlin
@HiltAndroidApp
class FeedPluseApp : Application()
```

#### Step 1.2: Register in AndroidManifest.xml
- Add `android:name=".FeedPluseApp"` to the `<application>` tag

#### Step 1.3: Annotate MainActivity
- Add `@AndroidEntryPoint` to `MainActivity`

#### Step 1.4: Create `Resource.kt` utility
- **File:** `core/util/Resource.kt`
- Sealed class with three states:
  - `Resource.Loading`
  - `Resource.Success<T>(data: T)`
  - `Resource.Error(message: String, data: T? = null)`
- This will be used by all features for consistent state handling

#### Step 1.5: Add API key via Secrets Gradle Plugin
- Create `local.properties` entry: `NEWS_API_KEY=your_key_here`
- The `secrets-gradle-plugin` (already applied) exposes it as `BuildConfig.NEWS_API_KEY`

**✅ Done when:** App compiles with Hilt, `BuildConfig.NEWS_API_KEY` resolves, `Resource` sealed class exists.

---

### Phase 2 — Core Networking (Retrofit + Hilt Module)

**Goal:** Set up a single Retrofit instance provided by Hilt that all feature APIs share.

#### Step 2.1: Create `NetworkModule.kt`
- **File:** `core/di/NetworkModule.kt`
- Hilt `@Module` with `@InstallIn(SingletonComponent::class)`
- Provides:
  - `OkHttpClient` with `HttpLoggingInterceptor` (DEBUG level in debug builds)
  - `Retrofit` instance with base URL `https://newsapi.org/v2/` and `GsonConverterFactory`
  - `ArticleApi` (via `retrofit.create(ArticleApi::class.java)`)
  - `SourceApi` (via `retrofit.create(SourceApi::class.java)`)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient { ... }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit { ... }

    @Provides
    @Singleton
    fun provideArticleApi(retrofit: Retrofit): ArticleApi { ... }

    @Provides
    @Singleton
    fun provideSourceApi(retrofit: Retrofit): SourceApi { ... }
}
```

**✅ Done when:** `NetworkModule` compiles and Hilt can resolve `ArticleApi` and `SourceApi`.

---

### Phase 3 — Articles Feature (Data Layer)

**Goal:** Define the API interface, DTOs, domain model, repository interface, and repository implementation for articles.

#### Step 3.1: Create `ArticleApi.kt`
- **File:** `feature/articles/data/model/` (or alongside repository — your preference)
- Retrofit interface:

```kotlin
interface ArticleApi {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY
    ): ArticleResponse
}
```

#### Step 3.2: Create API response DTOs
- **File:** `feature/articles/data/model/ArticleResponse.kt`

```kotlin
data class ArticleResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<ArticleDto>
)
```

- **File:** `feature/articles/data/model/ArticleDto.kt`

```kotlin
data class ArticleDto(
    val source: SourceDto?,
    val author: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    @SerializedName("urlToImage") val imageUrl: String?,
    @SerializedName("publishedAt") val date: String?,
    val content: String?
) {
    // Nested source object in article response
    data class SourceDto(
        val id: String?,
        val name: String?
    )
}
```

#### Step 3.3: Create domain model `ArticleData.kt`
- **File:** `feature/articles/domain/model/ArticleData.kt`
- Clean model with renamed fields (`imageUrl`, `date`):

```kotlin
data class ArticleData(
    val sourceId: String?,
    val sourceName: String?,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String?,
    val imageUrl: String?,
    val date: String?,
    val content: String?
)
```

#### Step 3.4: Create mapper extension
- In `ArticleDto.kt` (or separate mapper file):

```kotlin
fun ArticleDto.toArticleData(): ArticleData = ArticleData(
    sourceId = source?.id,
    sourceName = source?.name,
    author = author,
    title = title ?: "No Title",
    description = description,
    url = url,
    imageUrl = imageUrl,
    date = date,
    content = content
)
```

#### Step 3.5: Create `ArticleRepository` interface
- **File:** `feature/articles/domain/repository/ArticleRepository.kt`

```kotlin
interface ArticleRepository {
    suspend fun getArticles(): Resource<List<ArticleData>>
}
```

#### Step 3.6: Create `ArticleRepositoryImpl`
- **File:** `feature/articles/data/repository/ArticleRepositoryImpl.kt`
- Injected with `ArticleApi`
- Wraps API call in try/catch, returns `Resource.Success` or `Resource.Error`

```kotlin
class ArticleRepositoryImpl @Inject constructor(
    private val articleApi: ArticleApi
) : ArticleRepository {

    override suspend fun getArticles(): Resource<List<ArticleData>> {
        return try {
            val response = articleApi.getTopHeadlines()
            Resource.Success(response.articles.map { it.toArticleData() })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}
```

#### Step 3.7: Bind repository in Hilt
- **File:** `core/di/RepositoryModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindArticleRepository(
        impl: ArticleRepositoryImpl
    ): ArticleRepository
}
```

**✅ Done when:** `ArticleRepositoryImpl` compiles and Hilt can inject it.

---

### Phase 4 — Articles Feature (Domain Layer — Use Case)

**Goal:** Create a use case that the ViewModel calls, keeping the ViewModel thin.

#### Step 4.1: Create `GetArticlesUseCase.kt`
- **File:** `feature/articles/domain/usecase/GetArticlesUseCase.kt`

```kotlin
class GetArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(): Resource<List<ArticleData>> {
        return repository.getArticles()
    }
}
```

**Why a use case for a simple pass-through?**
- Future-proofing: filtering, sorting, combining with local cache, or merging AI summaries will all go here without touching the ViewModel or repository.

**✅ Done when:** `GetArticlesUseCase` compiles and is injectable.

---

### Phase 5 — Articles Feature (Presentation Layer)

**Goal:** Build the ViewModel and Compose UI for the articles list screen.

#### Step 5.1: Create `ArticleViewModel.kt`
- **File:** `feature/articles/presentation/ArticleViewModel.kt`

```kotlin
@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val getArticlesUseCase: GetArticlesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<Resource<List<ArticleData>>>(Resource.Loading)
    val uiState: StateFlow<Resource<List<ArticleData>>> = _uiState.asStateFlow()

    init {
        fetchArticles()
    }

    fun fetchArticles() {
        viewModelScope.launch {
            _uiState.value = Resource.Loading
            _uiState.value = getArticlesUseCase()
        }
    }
}
```

#### Step 5.2: Create `ArticleListItem.kt`
- **File:** `feature/articles/presentation/ArticleListItem.kt`
- Composable that displays one article card:
  - Article image (Coil `AsyncImage`) at the top
  - Title (bold, max 2 lines)
  - Source name + date on a row below
  - Description (max 3 lines, muted color)
- Receives `ArticleData` and `onClick: () -> Unit` (for future detail navigation)

#### Step 5.3: Create `ArticleScreen.kt`
- **File:** `feature/articles/presentation/ArticleScreen.kt`
- Composable that:
  - Takes `ArticleViewModel` via `hiltViewModel()`
  - Collects `uiState` with `collectAsStateWithLifecycle()`
  - Shows `CircularProgressIndicator` on `Loading`
  - Shows error message + retry button on `Error`
  - Shows `LazyColumn` of `ArticleListItem` on `Success`

**✅ Done when:** Articles screen shows a loading spinner, then a list of articles from the API, with error handling.

---

### Phase 6 — Sources Feature (Full Stack — Mirror of Articles)

**Goal:** Implement the entire sources feature using the same layers, patterns, and error handling as articles.

> **Principle:** Identical structure — swap "Article" for "Source" in every class name.

#### Step 6.1: Data Layer

- **`SourceApi.kt`** — Retrofit interface:
  ```kotlin
  @GET("top-headlines/sources")
  suspend fun getSources(
      @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY
  ): SourceResponse
  ```

- **`SourceResponse.kt`** — API wrapper:
  ```kotlin
  data class SourceResponse(
      val status: String,
      val sources: List<SourceDto>
  )
  ```

- **`SourceDto.kt`** — Raw API model:
  ```kotlin
  data class SourceDto(
      val id: String?,
      val name: String?,
      val description: String?,
      val url: String?,
      val category: String?,
      val language: String?,
      val country: String?
  )
  ```

- **`SourceData.kt`** — Domain model:
  ```kotlin
  data class SourceData(
      val id: String,
      val name: String,
      val description: String
  )
  ```

- **Mapper:** `SourceDto.toSourceData()`

- **`SourceRepository.kt`** — Interface:
  ```kotlin
  interface SourceRepository {
      suspend fun getSources(): Resource<List<SourceData>>
  }
  ```

- **`SourceRepositoryImpl.kt`** — Implementation (same try/catch pattern as articles)

#### Step 6.2: Domain Layer

- **`GetSourcesUseCase.kt`** — Same pattern as `GetArticlesUseCase`

#### Step 6.3: Bind in Hilt

- Add `SourceApi` provider to `NetworkModule`
- Add `bindSourceRepository` to `RepositoryModule`

#### Step 6.4: Presentation Layer

- **`SourceViewModel.kt`** — Same structure as `ArticleViewModel`
- **`SourceListItem.kt`** — Displays source name (bold) on top, description below
- **`SourceScreen.kt`** — "Sources" title at top, then `LazyColumn` of `SourceListItem`; loading/error states match articles

**✅ Done when:** Sources screen displays a list of sources with name and description, with identical error handling to articles.

---

### Phase 7 — Navigation & Bottom Bar

**Goal:** Wire both screens together with Jetpack Navigation Compose and a Material 3 bottom navigation bar.

#### Step 7.1: Create `Screen.kt`
- **File:** `navigation/Screen.kt`

```kotlin
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Articles : Screen("articles", "Articles", Icons.Default.Article)
    data object Sources  : Screen("sources",  "Sources",  Icons.Default.Source)
}
```

#### Step 7.2: Create `AppNavGraph.kt`
- **File:** `navigation/AppNavGraph.kt`
- Contains the `NavHost` with two composable destinations
- Each destination calls its respective screen composable

#### Step 7.3: Update `MainActivity.kt`
- Replace the template `Scaffold` + `Greeting` with:
  - `Scaffold` with a `NavigationBar` (Material 3) in `bottomBar`
  - Two `NavigationBarItem`s: Articles, Sources
  - `NavHost` as the scaffold content
  - Track `currentRoute` to highlight the active tab

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FeedPluseTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomNavigationBar(navController) }
                ) { innerPadding ->
                    AppNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
```

**✅ Done when:** App launches with bottom nav, tapping "Articles" shows the articles list, tapping "Sources" shows the sources list. Back stack is handled correctly.

---

### Phase 8 — Polish & Error Handling

**Goal:** Harden the app for production-quality UX.

#### Step 8.1: Pull-to-refresh
- Add `material3` pull-to-refresh to both screens
- Calls `viewModel.fetchArticles()` / `viewModel.fetchSources()`

#### Step 8.2: Empty state
- Handle the case where API returns `articles: []` or `sources: []`
- Show a friendly "No articles found" / "No sources found" message

#### Step 8.3: Network error UX
- Show a `Snackbar` or inline error card with a **Retry** button
- Distinguish between no-network and server errors if needed

#### Step 8.4: Shimmer / placeholder loading
- Replace plain `CircularProgressIndicator` with shimmer placeholder cards for a polished feel (optional for MVP)

**✅ Done when:** Error, empty, and loading states all look intentional and are user-friendly.

---

### Phase 9 — Unit Tests

**Goal:** Validate each layer independently.

#### Step 9.1: Repository tests
- Mock `ArticleApi` / `SourceApi` with fake responses
- Assert `Resource.Success` with correct mapped data
- Assert `Resource.Error` when API throws

#### Step 9.2: Use case tests
- Mock repository
- Verify use case returns repository result

#### Step 9.3: ViewModel tests
- Use `kotlinx-coroutines-test` with `TestDispatcher`
- Mock use case
- Assert `uiState` transitions: Loading → Success / Error

**✅ Done when:** All unit tests pass, covering happy path and error path for both features.

---

### Future Phases (Out of MVP Scope)

#### Phase 10 — Article Detail Screen
- Navigate from article list item tap to a detail screen
- Display full article content, image, source, date
- Deep link support

#### Phase 11 — AI Article Summarization
- Integrate OpenAI GPT API
- Add "Summarize" button on the article detail screen
- Show AI-generated summary in an expandable card
- New classes: `SummaryApi`, `SummaryRepository`, `GetSummaryUseCase`

#### Phase 12 — Offline Caching (Room)
- Add Room database
- Cache articles and sources locally
- Repository becomes single source of truth (network + local)

#### Phase 13 — Search & Filtering
- Search articles by keyword
- Filter sources by category/language

---

## File Creation Checklist

| # | File | Phase |
|---|------|-------|
| 1 | `FeedPluseApp.kt` | 1 |
| 2 | `core/util/Resource.kt` | 1 |
| 3 | `core/di/NetworkModule.kt` | 2 |
| 4 | `core/di/RepositoryModule.kt` | 3 |
| 5 | `feature/articles/data/model/ArticleDto.kt` | 3 |
| 6 | `feature/articles/data/model/ArticleResponse.kt` | 3 |
| 7 | `feature/articles/data/model/ArticleApi.kt` | 3 |
| 8 | `feature/articles/domain/model/ArticleData.kt` | 3 |
| 9 | `feature/articles/domain/repository/ArticleRepository.kt` | 3 |
| 10 | `feature/articles/data/repository/ArticleRepositoryImpl.kt` | 3 |
| 11 | `feature/articles/domain/usecase/GetArticlesUseCase.kt` | 4 |
| 12 | `feature/articles/presentation/ArticleViewModel.kt` | 5 |
| 13 | `feature/articles/presentation/ArticleScreen.kt` | 5 |
| 14 | `feature/articles/presentation/ArticleListItem.kt` | 5 |
| 15 | `feature/sources/data/model/SourceDto.kt` | 6 |
| 16 | `feature/sources/data/model/SourceResponse.kt` | 6 |
| 17 | `feature/sources/data/model/SourceApi.kt` | 6 |
| 18 | `feature/sources/domain/model/SourceData.kt` | 6 |
| 19 | `feature/sources/domain/repository/SourceRepository.kt` | 6 |
| 20 | `feature/sources/data/repository/SourceRepositoryImpl.kt` | 6 |
| 21 | `feature/sources/domain/usecase/GetSourcesUseCase.kt` | 6 |
| 22 | `feature/sources/presentation/SourceViewModel.kt` | 6 |
| 23 | `feature/sources/presentation/SourceScreen.kt` | 6 |
| 24 | `feature/sources/presentation/SourceListItem.kt` | 6 |
| 25 | `navigation/Screen.kt` | 7 |
| 26 | `navigation/AppNavGraph.kt` | 7 |
| 27 | Update `MainActivity.kt` | 7 |
| 28 | Update `AndroidManifest.xml` | 1 |

---

## Key Decisions Log

| Decision | Choice | Rationale |
|----------|--------|-----------|
| State wrapper | `Resource<T>` sealed class | Simple, explicit loading/success/error states |
| DI | Hilt | Already configured; standard for Android |
| Networking | Retrofit + Gson | Already in dependencies; well-suited for REST |
| Image loading | Coil Compose | Already in dependencies; lightweight |
| Navigation | Navigation Compose | Standard Jetpack; supports bottom nav |
| DTO ↔ Domain mapping | Extension functions | Keeps DTOs decoupled from domain models |
| Field renaming | `urlToImage → imageUrl`, `publishedAt → date` | Per requirements; `@SerializedName` on DTO |
| Use cases | One per feature (expandable) | Thin now, but ready for business logic later |
| AI summarization | Deferred to Phase 11 | MVP focuses on fetching & displaying |
