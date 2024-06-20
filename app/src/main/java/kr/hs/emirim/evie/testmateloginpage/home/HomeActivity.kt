package kr.hs.emirim.evie.testmateloginpage.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import kr.hs.emirim.evie.testmateloginpage.R
import kr.hs.emirim.evie.testmateloginpage.alarm.AlarmActivity
import kr.hs.emirim.evie.testmateloginpage.api.home.HomeAPIService
import kr.hs.emirim.evie.testmateloginpage.calendar.Calendar
import kr.hs.emirim.evie.testmateloginpage.comm.RetrofitClient
import kr.hs.emirim.evie.testmateloginpage.databinding.ActivityHomeBinding
import kr.hs.emirim.evie.testmateloginpage.home.data.HomeSubjectInfoResponse
import kr.hs.emirim.evie.testmateloginpage.home.data.HomeSubjectTop3RangeResponse
import kr.hs.emirim.evie.testmateloginpage.login.CurrentUser
import kr.hs.emirim.evie.testmateloginpage.subject.AddSubjectActivity
import kr.hs.emirim.evie.testmateloginpage.subject.GoalMainListActivity
import kr.hs.emirim.evie.testmateloginpage.subject.SubjectHomeAdapter
import kr.hs.emirim.evie.testmateloginpage.subject.SubjectViewModel
import kr.hs.emirim.evie.testmateloginpage.subject.SubjectViewModelFactory
import kr.hs.emirim.evie.testmateloginpage.subject.data.SubjectResponse
import kr.hs.emirim.evie.testmateloginpage.util.SpinnerUtil
import kr.hs.emirim.evie.testmateloginpage.wrong_answer_note.WrongAnswerListActivity
import kr.hs.emirim.evie.testmateloginpage.wrong_answer_note.WrongAnswerListViewModel
import kr.hs.emirim.evie.testmateloginpage.wrong_answer_note.WrongAnswerListViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HomeActivity : AppCompatActivity() {

    // 시험기록 데이터 생성 : 홈 과목 정보 안에 있는 Exam으로 설정
    var testRecordDataList: MutableList<HomeSubjectInfoResponse.Exam> = mutableListOf()

    // 알람
    private lateinit var bellBtn: ImageButton

    // 홈 -> 과목 정보(시험 점수 리스트, 시험날짜, 난이도, 점수, 실패요소)
    private lateinit var subjectIdTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var levelTextView: RatingBar
    private lateinit var goalScoreTextView: TextView
    private lateinit var gradeSubject: TextView

    // 홈 -> 문제가 잘 나오는 곳 TOP3
    private lateinit var top1range: TextView
    private lateinit var top2range: TextView
    private lateinit var top3range: TextView

    // 홈 -> 오답 실수 퍼센트 TOP3
    private lateinit var top1reason: TextView
    private lateinit var top2reason: TextView
    private lateinit var top3reason: TextView
    private lateinit var top1reasonPercent: ProgressBar
    private lateinit var top2reasonPercent: ProgressBar
    private lateinit var top3reasonPercent: ProgressBar

    lateinit var navHome: ImageButton
    lateinit var navGoal: ImageButton
    lateinit var navCal: ImageButton
    lateinit var navWrong: ImageButton

    lateinit var addSubjectBtn: ImageButton
    lateinit var editTestRecordBtn: ImageButton
    lateinit var userGrade: TextView
    lateinit var spinner: Spinner
    lateinit var toggle: ImageButton
    lateinit var subjectsAdapter: SubjectHomeAdapter

    // header
    private lateinit var drawerLayout: LinearLayout
    private lateinit var toggleButton: ImageButton
    private lateinit var userName: TextView

    private val newSubjectActivityRequestCode = 1

    private val subjectsListViewModel by viewModels<SubjectViewModel> {
        SubjectViewModelFactory(this)
    }

    private val listViewModel by viewModels<WrongAnswerListViewModel> {
        WrongAnswerListViewModelFactory(this)
    }

    var selectedPosition: Int? = null

    private lateinit var binding: ActivityHomeBinding

    // Retrofit 서비스 인스턴스
    private lateinit var homeAPIService: HomeAPIService

    var subjectId = 2
    var selectedText = ""

    private lateinit var failure1: Button
    private lateinit var failure2: Button
    private lateinit var failure3: Button
    private lateinit var failure4: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // RetrofitClient를 사용하여 homeAPIService 초기화
        homeAPIService = RetrofitClient.create(HomeAPIService::class.java, this)

        userName = findViewById(R.id.user_name)
        userName.text = "${CurrentUser.userDetails?.name ?: "___"} 님"

        // findViewById를 사용하여 레이아웃 파일에서 뷰를 가져와 변수에 할당
        dateTextView = findViewById(R.id.dday)
        levelTextView = findViewById(R.id.level)
        goalScoreTextView = findViewById(R.id.goal_score)
        gradeSubject = findViewById(R.id.userGrade)

        top1range = findViewById(R.id.top1)
        top2range = findViewById(R.id.top2)
        top3range = findViewById(R.id.top3)

        top1reason = findViewById(R.id.reason1)
        top2reason = findViewById(R.id.reason2)
        top3reason = findViewById(R.id.reason3)
        top1reasonPercent = findViewById(R.id.reason1Percentage)
        top2reasonPercent = findViewById(R.id.reason2Percentage)
        top3reasonPercent = findViewById(R.id.reason3Percentage)

        // TODO : 실제로는 이 값을 동적으로 설정해야 함 -> 스피너에 있는 subjectId
        fetchSubjectData(subjectId) // 과목 정보(시험 점수 리스트, 시험날짜, 난이도, 점수, 실패요소)
        fetchTop3RangeData(subjectId) // 문제가 잘 나오는 곳 TOP3
        fetchTop3ReasonData(subjectId) // 오답 실수 TOP3 퍼센트

        Log.d("homeLog", CurrentUser.selectGrade.toString())

        spinner = SpinnerUtil.gradeSpinner(this, R.id.spinnerWrong)
        spinner.setSelection((CurrentUser.selectGrade ?: 1) - 1)
        selectedPosition = spinner.selectedItemPosition
        var selectedItem = spinner.getItemAtPosition(selectedPosition!!).toString()
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPosition = position + 1
                CurrentUser.selectGrade = spinner.selectedItemPosition + 1
                subjectsListViewModel.readSubjectList(selectedPosition!!)
                selectedText = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무 것도 선택되지 않았을 때 처리할 작업
            }
        }

        subjectsAdapter = SubjectHomeAdapter { subject -> adapterOnClick(subject) }
        val recyclerView: RecyclerView = findViewById(R.id.subjectRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = subjectsAdapter

        subjectsListViewModel.subjectListData.observe(this) { map ->
            map?.let {
                val subjectsForSelectedGrade = it[selectedPosition]
                subjectsForSelectedGrade?.let { subjects ->
                    subjectsAdapter.submitList(subjects as MutableList<SubjectResponse>)
                }
            }
        }

        bellBtn = findViewById(R.id.bell)
        bellBtn.setOnClickListener {
            Log.d("homeLog", "addSubjectBtn 클릭!")
            val intent = Intent(this@HomeActivity, AlarmActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivityForResult(intent, newSubjectActivityRequestCode)
        }

        addSubjectBtn = findViewById(R.id.addSubjectBtn)
        addSubjectBtn.setOnClickListener {
            Log.d("homeLog", "addSubjectBtn 클릭!")
            val intent = Intent(this@HomeActivity, AddSubjectActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivityForResult(intent, newSubjectActivityRequestCode)
        }

        editTestRecordBtn = findViewById(R.id.edit_test_record_btn)
        editTestRecordBtn.setOnClickListener {
            val intent = Intent(this@HomeActivity, EditTestRecordActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivityForResult(intent, newSubjectActivityRequestCode)
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        toggleButton = findViewById(R.id.toggle)
        val drawerCancel = findViewById<ImageButton>(R.id.drawer_cancel)

        toggleButton.setOnClickListener {
            if (drawerLayout.visibility == View.INVISIBLE) {
                drawerLayout.visibility = View.VISIBLE
                val layoutParams = drawerLayout.layoutParams
                layoutParams.height = resources.getDimensionPixelSize(R.dimen.drawer_height_visible)
                drawerLayout.layoutParams = layoutParams
            } else {
                drawerLayout.visibility = View.INVISIBLE
                val layoutParams = drawerLayout.layoutParams
                layoutParams.height = 0
                drawerLayout.layoutParams = layoutParams
            }
        }
        drawerCancel.setOnClickListener {
            drawerLayout.visibility = View.INVISIBLE
            val layoutParams = drawerLayout.layoutParams
            layoutParams.height = 0
            drawerLayout.layoutParams = layoutParams
        }

        failure1 = findViewById(R.id.failure1)
        failure2 = findViewById(R.id.failure2)
        failure3 = findViewById(R.id.failure3)
        failure4 = findViewById(R.id.failure4)

        val buttons = listOf(failure1, failure2, failure3, failure4)
        for (button in buttons) {
            button.setOnClickListener {
                updateButtonStyles(button)
            }
        }

        setNavListeners() // 네비게이션 바
    }

    private fun adapterOnClick(subject: SubjectResponse) {
        Log.d("adapterOnClick", subject.subjectId.toString())
        gradeSubject.text = "$selectedText ${subject.subjectName}"
        subjectId = subject.subjectId
        fetchSubjectData(subjectId) // 과목 정보(시험 점수 리스트, 시험날짜, 난이도, 점수, 실패요소)
        fetchTop3RangeData(subjectId) // 문제가 잘 나오는 곳 TOP3
        fetchTop3ReasonData(subjectId) // 오답 실수 TOP3 퍼센트
    }

    private fun fetchSubjectData(subjectId: Int) {
        try {
            val call = homeAPIService.getHomeSubjectInfo(subjectId)
            Log.d("fetchSubjectData", "Fetching data for subjectId: $subjectId")

            call.enqueue(object : Callback<HomeSubjectInfoResponse> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<HomeSubjectInfoResponse>, response: Response<HomeSubjectInfoResponse>) {
                    Log.d("fetchSubjectData", "API call successful, Response code: ${response.code()}")
                    if (response.isSuccessful) {
                        val subjectResponse = response.body()
                        Log.i("fetchSubjectData", "Response body: $subjectResponse")
                        subjectResponse?.let {
                            HomeSubjectInfoupdateUI(subjectResponse)
                        }
                    } else {
                        Log.e("fetchSubjectData", "Failed to get subject data. Error code: ${response.code()}")
                        Toast.makeText(this@HomeActivity, "과목 정보를 불러오는데 실패했습니다!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<HomeSubjectInfoResponse>, t: Throwable) {
                    Log.e("fetchSubjectData", "API call failed: ${t.message}", t)
                    Toast.makeText(this@HomeActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e("fetchSubjectData", "Exception during API call", e)
        }
    }

    private fun fetchTop3RangeData(subjectId: Int) {
        try {
            val call = homeAPIService.getTop3ranges(subjectId)
            Log.d("fetchSubjectData", "Fetching data for subjectId: $subjectId")

            call.enqueue(object : Callback<HomeSubjectTop3RangeResponse> {
                override fun onResponse(call: Call<HomeSubjectTop3RangeResponse>, response: Response<HomeSubjectTop3RangeResponse>) {
                    Log.d("fetchSubjectData", "API call successful, Response code: ${response.code()}")
                    if (response.isSuccessful) {
                        val top3range = response.body()
                        Log.i("fetchSubjectData", "Response body: $top3range")
                        top3range?.let {
                            HomeSubjectInfoupdateTopRangeUI(top3range)
                        }
                    } else {
                        Log.e("fetchSubjectData", "Failed to get subject data. Error code: ${response.code()}")
                        Toast.makeText(this@HomeActivity, "과목 정보를 불러오는데 실패했습니다!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<HomeSubjectTop3RangeResponse>, t: Throwable) {
                    Log.e("fetchSubjectData", "API call failed: ${t.message}", t)
                    Toast.makeText(this@HomeActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e("fetchSubjectData", "Exception during API call", e)
        }
    }

    private fun fetchTop3ReasonData(subjectId: Int) {
        try {
            val call = homeAPIService.getTop3reasons(subjectId)
            Log.d("fetchTop3ReasonData", "Fetching data for subjectId: $subjectId")

            call.enqueue(object : Callback<List<List<Any>>> {
                override fun onResponse(call: Call<List<List<Any>>>, response: Response<List<List<Any>>>) {
                    Log.d("fetchTop3ReasonData", "API call successful, Response code: ${response.code()}")
                    if (response.isSuccessful) {
                        val top3reasonList = response.body()
                        Log.i("fetchTop3ReasonData", "Response body: $top3reasonList")
                        top3reasonList?.let {
                            HomeSubjectInfoupdateTopReasonUI(top3reasonList)
                        }
                    } else {
                        Log.e("fetchTop3ReasonData", "Failed to get data. Error code: ${response.code()}")
                        Toast.makeText(this@HomeActivity, "데이터를 불러오는데 실패했습니다!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<List<Any>>>, t: Throwable) {
                    Log.e("fetchTop3ReasonData", "API call failed: ${t.message}", t)
                    Toast.makeText(this@HomeActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e("fetchTop3ReasonData", "Exception during API call", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun HomeSubjectInfoupdateUI(subjectResponse: HomeSubjectInfoResponse?) {
        subjectResponse?.let {
            if (it.date != null) {
                val date = calculateDday(it.date)
                dateTextView.text = date
            } else {
                dateTextView.text = "날짜 정보 없음"
            }
            levelTextView.rating = it.level.toFloat() // RatingBar는 float 타입으로 설정
            goalScoreTextView.text = "목표점수 ${it.goalScore}점"

            testRecordDataList.clear() // 기존 데이터를 지우고 새로운 데이터로 업데이트
            testRecordDataList.addAll(it.exams) // testRecordDataList에 exams 정보 넣기

            // 성적 그래프
            val linechart = findViewById<LineChart>(R.id.home_test_record_chart)
            val horizontalScrollView = findViewById<HorizontalScrollView>(R.id.home_scroll_view_graph)
            val scoreChart = ScoreChart(linechart, horizontalScrollView, this)
            scoreChart.setupChart(testRecordDataList)

            val failValue = it.fail ?: 0
            updateButtonStylesBasedOnFail(failValue)
        }
    }

    private fun HomeSubjectInfoupdateTopRangeUI(top3rangeResponse: List<String>?) {
        top3rangeResponse?.let {
            top1range.text = if (it.isNotEmpty()) it[0] else "없음"
            top2range.text = if (it.size > 1) it[1] else "없음"
            top3range.text = if (it.size > 2) it[2] else "없음"
        }
    }

    private fun HomeSubjectInfoupdateTopReasonUI(top3reasonList: List<List<Any>>?) {
        top3reasonList?.let {
            if (it.isNotEmpty()) {
                top1reason.text = "${it[0][0] as String} ${(it[0][1] as Double).toInt()}%"
                top1reasonPercent.progress = (it[0][1] as Double).toInt()
            } else {
                top1reason.text = "없음"
                top1reasonPercent.progress = 0
            }

            if (it.size > 1) {
                top2reason.text = "${it[1][0] as String} ${(it[1][1] as Double).toInt()}%"
                top2reasonPercent.progress = (it[1][1] as Double).toInt()
            } else {
                top2reason.text = "없음"
                top2reasonPercent.progress = 0
            }

            if (it.size > 2) {
                top3reason.text = "${it[2][0] as String} ${(it[2][1] as Double).toInt()}%"
                top3reasonPercent.progress = (it[2][1] as Double).toInt()
            } else {
                top3reason.text = "없음"
                top3reasonPercent.progress = 0
            }
        }
    }

    private fun updateButtonStylesBasedOnFail(failValue: Int) {
        val buttons = listOf(failure1, failure2, failure3, failure4)
        val selectedButton = when (failValue) {
            1 -> failure1
            2 -> failure2
            3 -> failure3
            4 -> failure4
            else -> null
        }

        selectedButton?.let {
            updateButtonStyles(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateDday(targetDate: String): String {
        targetDate ?: return "날짜 정보 없음"

        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.parse(targetDate, formatter)
        val daysDifference = ChronoUnit.DAYS.between(currentDate, date)

        return when {
            daysDifference > 0 -> "D-$daysDifference"
            daysDifference < 0 -> "D+${-daysDifference}"
            else -> "D-day"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        if (requestCode == newSubjectActivityRequestCode && resultCode == Activity.RESULT_OK) {
            selectedPosition?.let {
                Log.d("homeRestart", "실행요미")
                subjectsListViewModel.readSubjectList(it)
            }
        } else {
            Log.d("homeRestart", "Request code or Result code mismatch")
        }
    }

    private fun setNavListeners() {
        navHome = findViewById(R.id.nav_home)
        navWrong = findViewById(R.id.nav_wrong)
        navGoal = findViewById(R.id.nav_goal)
        navCal = findViewById(R.id.nav_cal)

        navHome.setOnClickListener {
            startNewActivity(HomeActivity::class.java)
        }
        navWrong.setOnClickListener {
            startNewActivity(WrongAnswerListActivity::class.java)
        }
        navGoal.setOnClickListener {
            startNewActivity(GoalMainListActivity::class.java)
        }
        navCal.setOnClickListener {
            startNewActivity(Calendar::class.java)
        }
    }

    private fun startNewActivity(cls: Class<*>) {
        val intent = Intent(this, cls)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    private fun updateButtonStyles(clickedButton: Button) {
        val buttons = listOf(failure1, failure2, failure3, failure4)
        val failValue = when (clickedButton) {
            failure1 -> 1
            failure2 -> 2
            failure3 -> 3
            failure4 -> 4
            else -> 0
        }

        for (button in buttons) {
            if (button == clickedButton) {
                button.setBackgroundResource(R.drawable.popup_btn_round)
                button.setTextColor(ContextCompat.getColor(this, R.color.green_500))
            } else {
                button.setBackgroundResource(R.drawable.popup_btn_round_gray)
                button.setTextColor(ContextCompat.getColor(this, R.color.black_800))
            }
            button.setPadding(16.dpToPx(this), button.paddingTop, button.paddingRight, button.paddingBottom)
        }

        val body = mapOf("fail" to failValue)
        homeAPIService.updateFailure(subjectId, body).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Handle successful response
                } else {
                    // Handle unsuccessful response
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Handle failure
            }
        })
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}
