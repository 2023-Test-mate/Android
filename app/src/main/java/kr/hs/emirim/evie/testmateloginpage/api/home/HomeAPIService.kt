package kr.hs.emirim.evie.testmateloginpage.api.home

import kr.hs.emirim.evie.testmateloginpage.api.MessageResponse
import kr.hs.emirim.evie.testmateloginpage.home.data.HomeSubjectInfoResponse
import kr.hs.emirim.evie.testmateloginpage.home.data.HomeSubjectTop3RangeResponse
import kr.hs.emirim.evie.testmateloginpage.subject.data.SubjectUpdateRequest
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PATCH

interface HomeAPIService {

    // 홈 -> 과목 정보(시험 점수 리스트, 시험날짜, 난이도, 점수, 실패요소) 불러오기
    @GET("/api/subject/home/{subjectId}")
    fun getHomeSubjectInfo(@Path("subjectId") subjectId: Int): Call<HomeSubjectInfoResponse>

    // 홈 -> 문제가 잘 나오는 TOP3
    @GET("/api/note/top3ranges/{subjectId}")
    fun getTop3ranges(@Path("subjectId") subjectId: Int): Call<HomeSubjectTop3RangeResponse>

    // 홈 -> 오답 실수 TOP3 퍼센트
    @GET("/api/note/top3reasons/{subjectId}")
    fun getTop3reasons(@Path("subjectId") subjectId: Int): Call<List<List<Any>>>

    // 홈 -> 시험 기록
    @PATCH("/api/subject/record/{subjectId}")
    fun updateSubjectRecord(
        @Path("subjectId") subjectId: Int,
        @Body updateRequest: SubjectUpdateRequest
    ): Call<MessageResponse>

    // 홈 -> 공부 외 실패요소
    @PATCH("api/subject/fail/{subjectId}")
    fun updateFailure(@Path("subjectId") subjectId: Int, @Body body: Map<String, Int>): Call<Void>
}