# Retrofit 네트워크 가이드 (Callback 버전)

> 이 문서는 현재 프로젝트의 **Retrofit(콜백 방식)** 세팅을 **사용하는 방법**과 **나중에 커스텀/확장**하는 방법을 정리한 사용 설명서입니다.
참고한 블로그: https://oscarstory.tistory.com/71
---

## 개요

- **역할**: HTTP 요청/응답을 쉽게 보내고 받기 위한 네트워크 클라이언트
- **방식**: `Call<T>.enqueue(...)` 콜백 기반 (코루틴 불필요)
- **컨버터**:
  - `ScalarsConverterFactory` – **문자열** 응답 처리
  - `GsonConverterFactory` – **JSON ↔ DTO** 변환
- **로그**: OkHttp `HttpLoggingInterceptor` (디버깅 편의)

---

## 디렉터리 구조

```
app/src/main/java/com/runningcity/
├─ network/
│  ├─ RetrofitClient.kt         # Retrofit 싱글톤 & OkHttp 설정
│  └─ RetrofitInterface.kt      # API 인터페이스(엔드포인트 정의)
└─ model/
   └─ Data.kt                   # 응답 모델(예시)
```

> 패키지 경로와 실제 폴더 경로가 **1:1 일치**해야 합니다.

---

## Gradle 의존성

`app/build.gradle.kts`

```kotlin
dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
```

루트 `settings.gradle.kts`(또는 루트 빌드): `google()`, `mavenCentral()` 필수.

---

## Manifest (HTTP만 해당)

```xml
<uses-permission android:name="android.permission.INTERNET"/>

<application
    android:name=".App"
    android:usesCleartextTraffic="true">   <!-- http 개발 주소 쓸 때만 -->
</application>
```

---

## 기본 사용법

### 1) Retrofit 클라이언트 가져오기

```kotlin
val retrofit = RetrofitClient.instance
val api = retrofit.create(RetrofitInterface::class.java)
```

### 2) GET 요청 호출 (예시)

```kotlin
val call = api.getData(name = "Oscar", age = "9살")
call.enqueue(object : Callback<Data> {
    override fun onResponse(call: Call<Data>, response: Response<Data>) {
        if (response.isSuccessful) {
            val body = response.body()
            // TODO: 성공 처리
        } else {
            // TODO: 서버 오류 처리 (4xx/5xx)
        }
    }

    override fun onFailure(call: Call<Data>, t: Throwable) {
        // TODO: 네트워크 장애/타임아웃/파싱 오류 등
    }
})
```

---

## 현재 포함된 코드 요약

### `RetrofitClient.kt`

- `BASE_URL` 지정 (`http://10.0.2.2:8080/` – 에뮬레이터→로컬PC)
- OkHttp + 로그 인터셉터
- 컨버터 2개 장착(문자 → JSON 순서 중요)

```kotlin
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val gson = GsonBuilder().setLenient().create()

    private val okHttp: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttp)
            .build()
    }
}
```

### `RetrofitInterface.kt` (예시)

```kotlin
interface RetrofitInterface {
    @GET("API_Test.php")
    fun getData(
        @Query("name") name: String,
        @Query("age") age: String
    ): Call<Data>
}
```

### `Data.kt` (예시)

```kotlin
data class Data(
    @Expose @SerializedName("name") val name: String,
    @Expose @SerializedName("age")  val age:  String
)
```

---

## 커스터마이징 가이드

### 1) 기본 URL(환경 분리)

- **개발/운영**을 나누고 싶다면 상수로 분기하거나 `BuildConfig.DEBUG`을 활용:

```kotlin
private const val DEV = "http://10.0.2.2:8080/"
private const val PROD = "https://api.your-domain.com/"

val baseUrl = if (BuildConfig.DEBUG) DEV else PROD
Retrofit.Builder().baseUrl(baseUrl) ...
```

> 팀 정책상 Gradle `buildTypes` 수정이 부담되면 **코드 상수**로 유지해도 OK.

---

### 2) 엔드포인트 추가

#### GET + 쿼리 파라미터

```kotlin
@GET("users")
fun listUsers(
    @Query("page") page: Int,
    @Query("size") size: Int
): Call<UserListResponse>
```

#### POST (Form-UrlEncoded)

```kotlin
@FormUrlEncoded
@POST("login")
fun login(
    @Field("id") id: String,
    @Field("password") pw: String
): Call<LoginResponse>
```

#### POST (JSON Body)

```kotlin
@POST("api/v1/running-data")
fun uploadRunningData(
    @Body body: RunningUploadRequest
): Call<RunningUploadResponse>
```

> **중요**: 서버 응답 JSON과 DTO 필드명이 일치하도록 `@SerializedName`을 맞춰 주세요.

---

### 3) 문자열 응답 vs JSON 응답

- 서버가 **그냥 문자열**(`"OK"`, `"pong"`)을 줄 때:  
  → `ScalarsConverterFactory`가 처리 → 반환 타입을 `Call<String>` 으로 만들면 됨.
- **JSON**이면 DTO 만들어 `GsonConverterFactory`가 처리 → `Call<MyDto>`.

```kotlin
@GET("ping")
fun ping(): Call<String>  // "pong"
```

---

### 4) 인터셉터(헤더/토큰/공통 파라미터)

- 인증 토큰이 필요하면 OkHttp **인터셉터**로 공통 헤더를 부여:

```kotlin
val auth = Interceptor { chain ->
    val token = /* TODO: 토큰 로드 */
    val req = chain.request().newBuilder().apply {
        if (!token.isNullOrBlank()) addHeader("Authorization", "Bearer $token")
    }.build()
    chain.proceed(req)
}

val okHttp = OkHttpClient.Builder()
    .addInterceptor(auth)
    .addInterceptor(logging) // logging은 디버그일 때만 권장
    .build()
```

---

### 5) 타임아웃/재시도

```kotlin
OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
    .build()
```

---

### 6) 에러 처리 패턴

콜백 안에서 분기:

```kotlin
call.enqueue(object : Callback<MyDto> {
    override fun onResponse(call: Call<MyDto>, response: Response<MyDto>) {
        if (response.isSuccessful) {
            val data = response.body()
            // 성공 처리
        } else {
            val code = response.code()
            val errorBody = response.errorBody()?.string()
            // 서버 에러 처리(로그/토스트/리포트 등)
        }
    }

    override fun onFailure(call: Call<MyDto>, t: Throwable) {
        // 네트워크 장애/타임아웃/파싱 실패
    }
})
```

> 일관된 처리를 원하면 **헬퍼 함수**를 만들어 공통화하세요.

---

### 7) WorkManager와 연동(백그라운드 업로드)

- 콜백 방식 그대로 `doWork()` 안에서 `enqueue` 호출 가능.
- **네트워크 제약/백오프** 설정으로 안정적 재시도.

```kotlin
val request = OneTimeWorkRequestBuilder<UploadWorker>()
    .setConstraints(Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build())
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.ofMinutes(5))
    .build()
WorkManager.getInstance(context).enqueue(request)
```

---

## 자주 겪는 문제 & 체크리스트

- [ ] `BASE_URL`은 **반드시 슬래시(`/`)로 끝남** (`http://.../`)
- [ ] `@GET/@POST` 경로에는 **상대 경로**만 적기 (`"api/..."`)
- [ ] 컨버터 **순서**: `Scalars` → `Gson`
- [ ] Gradle 의존성/저장소 추가 후 **Sync + Clean/Rebuild**
- [ ] 패키지 경로와 폴더 경로 **1:1 일치**
- [ ] HTTP 사용 시 `usesCleartextTraffic="true"`
- [ ] VS Code 인덱싱 꼬이면 터미널로 `./gradlew clean assembleDebug`

---

## 확장 템플릿 모음

### 공통 응답 래퍼(선택)

```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T): ApiResult<T>()
    data class Failure(val code: Int?, val message: String?, val cause: Throwable? = null): ApiResult<Nothing>()
}
```

### 공통 호출 함수(콜백 → 래핑)

```kotlin
fun <T> Call<T>.enqueueResult(onResult: (ApiResult<T>) -> Unit) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, res: Response<T>) {
            if (res.isSuccessful) onResult(ApiResult.Success(res.body()!!))
            else onResult(ApiResult.Failure(res.code(), res.errorBody()?.string(), null))
        }
        override fun onFailure(call: Call<T>, t: Throwable) {
            onResult(ApiResult.Failure(null, t.message, t))
        }
    })
}
```
