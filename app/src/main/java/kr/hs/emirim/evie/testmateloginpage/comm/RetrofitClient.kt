package kr.hs.emirim.evie.testmateloginpage.comm

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// RetrofitClient 클래스 정의
class RetrofitClient {
    companion object { // RetrofitClient 클래스에 대한 동반 객체 정의

        // 기본 URL 상수 정의
        private const val BASE_URL = "http://43.203.247.131"

        // create() 메서드 정의
        fun <T> create(service: Class<T>, context: Context? = null): T {
            val clientBuilder = OkHttpClient.Builder() // OkHttpClient.Builder 인스턴스 생성


            // context가 null이 아닌 경우에만 세션 ID를 추가
            context?.let { // context가 null이 아닌 경우 실행
                val sessionId = SessionManager.getSessionId(it) // SessionManager를 사용하여 세션 ID 가져오기
                sessionId?.let { id -> // sessionId가 null이 아닌 경우 실행
                    // OkHttpClient에 인터셉터 추가: 요청 헤더에 세션 ID 추가
                    clientBuilder.addInterceptor { chain ->
                        val originalRequest = chain.request() // 원본 요청 가져오기
                        val requestWithCookie = originalRequest.newBuilder() // 요청에 쿠키 헤더 추가
                            .addHeader("Cookie", "JSESSIONID=$id") // "JSESSIONID=세션ID" 형식으로 쿠키 헤더 추가
                            .build() // 변경된 요청 빌드
                        chain.proceed(requestWithCookie) // 변경된 요청 실행 및 응답 반환
                    }
                }
            }

            val client = clientBuilder.build() // OkHttpClient 빌드

            val retrofit = Retrofit.Builder() // Retrofit.Builder 인스턴스 생성
                .baseUrl(BASE_URL) // 기본 URL 설정
                .addConverterFactory(GsonConverterFactory.create()) // Gson 변환기 팩토리 추가
                .client(client) // OkHttpClient 설정
                .build() // Retrofit 인스턴스 빌드

            return retrofit.create(service) // Retrofit 서비스 인터페이스 구현체 반환
        }
    }
}
