package kr.hs.emirim.evie.testmateloginpage.api

import kr.hs.emirim.evie.testmateloginpage.wrong_answer_note.data.WrongAnswerNoteRequest
import kr.hs.emirim.evie.testmateloginpage.wrong_answer_note.data.WrongAnswerNoteResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface WrongAnswerAPIService {

    @GET("/api/note/filter")
    fun getNoteListByGradeSubject(@Query("grade") grade: Int, @Query("subjectId") subjectId : Int) : Call<List<WrongAnswerNoteResponse>>

    @POST("/api/note")
    fun postNote(@Body wrongAnswerNoteRequest : WrongAnswerNoteRequest) : Call<MessageResponse>

    @GET("api/note/{noteId}")
    fun getNoteDetail(@Path("noteId") noteId: Long) : Call<WrongAnswerNoteResponse>

    @Multipart
    @POST("api/note")
    fun uploadWrongAnswerNote(
        @Part("subjectId") subjectId: RequestBody,
        @Part("grade") grade: RequestBody,
        @Part("title") title: RequestBody,
        @Part imgs: List<MultipartBody.Part>,
        @Part("styles") styles: RequestBody,
        @Part("reason") reason: RequestBody,
        @Part("range") range: RequestBody
    ): Call<Void>
}