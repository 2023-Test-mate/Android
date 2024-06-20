package kr.hs.emirim.evie.testmateloginpage.api

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kr.hs.emirim.evie.testmateloginpage.comm.RetrofitClient
import kr.hs.emirim.evie.testmateloginpage.goalList.data.Goal
import kr.hs.emirim.evie.testmateloginpage.goalList.data.GoalPatchRequest
import kr.hs.emirim.evie.testmateloginpage.goalList.data.GoalRequest
import kr.hs.emirim.evie.testmateloginpage.goalList.data.GoalResponse
import kr.hs.emirim.evie.testmateloginpage.subject.data.SubjectRequest
import kr.hs.emirim.evie.testmateloginpage.subject.data.SubjectResponse
import kr.hs.emirim.evie.testmateloginpage.wrong_answer_note.data.WrongAnswerNoteResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GoalRepository(resources: Resources, context: Context) {
    var goalListData = MutableLiveData<List<GoalResponse>>()

    val goalListService : GoalAPIService

    init {
        goalListService = RetrofitClient.create(GoalAPIService::class.java, context)
    }

    // 리스트 길이
    fun getGoalListSize(): Int {
        return goalListData.value?.size ?: 0
    }

    fun getGoalList(): MutableLiveData<List<GoalResponse>> {
        return goalListData
    }

    fun getGoalListBySemester(subjectId: Int, semester : Int) {
        val call = goalListService.getGoalListBySemester(subjectId, semester)
        call.enqueue(object : Callback<List<GoalResponse>> {
            override fun onResponse(call: Call<List<GoalResponse>>, response: Response<List<GoalResponse>>) {
                if (response.isSuccessful) {
                    val goalList = response.body()
                    goalList?.let {
                        goalListData.postValue(it) // 받은 과목 정보를 MutableLiveData에 저장
                    }
                } else {
                    Log.e("API Error", "Failed to fetch subject info. Error code: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<GoalResponse>>, t: Throwable) {
                println("Failed to get notes. Error message: ${t.message}")
            }
        })
    }

    fun postGoal(goal : GoalRequest, callback: (Boolean) -> Unit) {
        val call = goalListService.postGoal(goal)
        call.enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                callback(false)
                println("Failed to get notes. Error message: ${t.message}")
            }
        })
    }

    fun patchGoal(goalId : Int, goal: GoalPatchRequest, callback: (Boolean, GoalResponse) -> Unit) {
        val call = goalListService.patchGoal(goalId, goal)
        call.enqueue(object : Callback<GoalResponse> {
            override fun onResponse(call: Call<GoalResponse>, response: Response<GoalResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { callback(true, it) }
                } else {
                    response.body()?.let { callback(false, it) }
                }
            }

            override fun onFailure(call: Call<GoalResponse>, t: Throwable) {

            }
        })
    }

    fun deleteGoal(goalId: Int, callback: (Boolean) -> Unit) {
        val call = goalListService.deleteGoal(goalId)
        call.enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                callback(false)
            }
        })
    }




    companion object { // 데이터를 관리하고 제공하는 데 사용되는 클래스의 인스턴스를 만들지 않고도 호출하게 함
        private var INSTANCE: GoalRepository? = null

        fun getDataSource(resources: Resources, context: Context): GoalRepository {
            return synchronized(GoalRepository::class) {
                val newInstance = INSTANCE ?: GoalRepository(resources, context)
                INSTANCE = newInstance
                newInstance
            }
        }
    }
}