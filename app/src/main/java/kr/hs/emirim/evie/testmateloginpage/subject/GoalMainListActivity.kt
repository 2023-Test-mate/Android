package kr.hs.emirim.evie.testmateloginpage.subject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.hs.emirim.evie.testmateloginpage.GoalSelectSemesterActivity
import kr.hs.emirim.evie.testmateloginpage.calendar.Calendar
import kr.hs.emirim.evie.testmateloginpage.R
import kr.hs.emirim.evie.testmateloginpage.ReadWrongAnswerNoteActivity
import kr.hs.emirim.evie.testmateloginpage.wrong_answer_note.WrongAnswerListActivity
import kr.hs.emirim.evie.testmateloginpage.goalList.GoalListActivity
import kr.hs.emirim.evie.testmateloginpage.home.HomeActivity
import kr.hs.emirim.evie.testmateloginpage.login.CurrentUser
import kr.hs.emirim.evie.testmateloginpage.subject.data.SubjectRequest
import kr.hs.emirim.evie.testmateloginpage.subject.data.SubjectResponse
import kr.hs.emirim.evie.testmateloginpage.util.SpinnerUtil

class GoalMainListActivity : AppCompatActivity() {
    lateinit var spinner: Spinner

    private lateinit var navHome: ImageButton
    private lateinit var navWrong: ImageButton
    private lateinit var navGoal: ImageButton
    private lateinit var navCal: ImageButton
    private lateinit var recyclerView: RecyclerView

    // header
    private lateinit var userName : TextView

    private val subjectViewModel by viewModels<SubjectViewModel> {
        SubjectViewModelFactory(this)
    }

    private lateinit var subjectAdapter: GoalSubjectTabAdapter

    var selectedPosition : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.goal_main_page)
        supportActionBar?.hide()

        navHome = findViewById(R.id.nav_home)
        navWrong = findViewById(R.id.nav_wrong)
        navGoal = findViewById(R.id.nav_goal)
        navCal = findViewById(R.id.nav_cal)
        recyclerView = findViewById(R.id.goalMainRecyclerView)

        userName = findViewById(R.id.user_name)
        userName.text = "${CurrentUser.userDetails?.name ?: "___"} 님"

        // 학년 spiner api 연동
        spinner = SpinnerUtil.gradeSpinner(this, R.id.spinnerWrong)
//        Log.d("GoalMainListActivity", CurrentUser.selectGrade!!.toString())
        spinner.setSelection((CurrentUser.selectGrade ?: 1) - 1)
        selectedPosition = spinner.selectedItemPosition// grade 인덱스 (ex. 3)
//        var selectedItem = spinner.getItemAtPosition(selectedPosition!!).toString() // grade 문자열 (ex. 고등학교 2학년)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 선택된 항목의 위치(position)를 이용하여 해당 항목의 값을 가져옴
                selectedPosition = position + 1
                CurrentUser.selectGrade = spinner.selectedItemPosition + 1
                subjectViewModel.readSubjectList(selectedPosition!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무 것도 선택되지 않았을 때 처리할 작업
            }
        }

        setupListeners()

        // 버튼 recyclerView
        subjectAdapter = GoalSubjectTabAdapter { subject, position -> subjectAdapterOnClick(subject, position) }
        val subjectRecyclerView: RecyclerView = findViewById(R.id.goalMainRecyclerView)

        subjectRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) // 수직 레이아웃 방향 설정
        subjectRecyclerView.adapter = subjectAdapter

        subjectViewModel.readSubjectList(CurrentUser.userDetails?.grade?.toInt() ?: 1) // list 가져오기
        subjectViewModel.subjectListData.observe(
            // observer : 어떤 이벤트가 일어난 순간, 이벤트를 관찰하던 관찰자들이 바로 반응하는 패턴
            this
        ) { map ->
            map?.let {
                val subjectsForSelectedGrade = it[selectedPosition]
                subjectsForSelectedGrade?.let { subjects ->
                    subjectAdapter.submitList(subjects as MutableList<SubjectResponse>) // 어댑터 내의 데이터를 새 리스트로 업데이트하는 데 사용
                }
            }
        }
    }

    private fun setupListeners() {
        // 클릭 리스너 설정
        navHome.setOnClickListener { navigateTo(HomeActivity::class.java) }
        navWrong.setOnClickListener { navigateTo(WrongAnswerListActivity::class.java) }
        navGoal.setOnClickListener { /* 현재 액티비티에 남아있도록*/ }
        navCal.setOnClickListener { navigateTo(Calendar::class.java) }
    }

    private fun subjectAdapterOnClick(subject: SubjectResponse, position: Int) {
        subjectAdapter.updateSelectedPosition(position)
        val intent = Intent(this, GoalSelectSemesterActivity::class.java)
        intent.putExtra("selectedNote", subject)
        intent.putExtra("selectedPosition", position)

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent)
    }

//    fun fetchSubjectsByGrade(grade: Int) {
//
//        apiService.getSubjectsByGrade(grade).enqueue(object : Callback<List<Subject>> {
//            override fun onResponse(call: Call<List<Subject>>, response: Response<List<Subject>>) {
//                if (response.isSuccessful) {
//                    val subjectList = response.body()
//                    subjectList?.let {
//                        for (subject in it) {
////                            Log.d("Subject", "ID: ${subject.subjectId}, Name: ${subject.subjectName}, Image: ${subject.img}")
//                        }
//                    }
//                } else {
//                    Log.e("API Error", "Error: ${response.code()}")
//                }
//            }
//
//            override fun onFailure(call: Call<List<Subject>>, t: Throwable) {
//                Log.e("Network Error", "Error: ${t.message}")
//            }
//        })
//    }


//    private fun updateRecyclerView(subjectList: List<Subject>) {
//        // RecyclerView 업데이트
//        val adapter = GoalSubjectTabAdapter { adapterOnClick(it) }
//        recyclerView.adapter = adapter
//        adapter.submitList(subjectList)
//    }

    private fun adapterOnClick(subject: SubjectResponse) {
        // 과목 클릭 시 액티비티 이동
        navigateTo(GoalListActivity::class.java)
    }

    private fun navigateTo(destination: Class<*>) {
        // 다른 액티비티로 이동하는 함수
        val intent = Intent(this, destination)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    private fun handleErrorResponse(responseCode: Int) {
        // 에러 응답 코드 처리
        when (responseCode) {
            // 특정한 에러 코드 처리 또는 일반적인 에러 메시지 표시
            else -> Log.e("APIError", "Error: $responseCode")
        }
    }
}