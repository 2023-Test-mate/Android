package kr.hs.emirim.evie.testmateloginpage.goalList


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kr.hs.emirim.evie.testmateloginpage.calendar.Calendar
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.hs.emirim.evie.testmateloginpage.R
import kr.hs.emirim.evie.testmateloginpage.wrong_answer_note.WrongAnswerListActivity
import kr.hs.emirim.evie.testmateloginpage.goalList.data.Goal
import kr.hs.emirim.evie.testmateloginpage.goalList.data.GoalPatchRequest
import kr.hs.emirim.evie.testmateloginpage.goalList.data.GoalRequest
import kr.hs.emirim.evie.testmateloginpage.goalList.data.GoalResponse
import kr.hs.emirim.evie.testmateloginpage.home.HomeActivity
import kr.hs.emirim.evie.testmateloginpage.login.CurrentUser
import kr.hs.emirim.evie.testmateloginpage.subject.GoalMainListActivity
import kr.hs.emirim.evie.testmateloginpage.subject.data.SubjectResponse

class GoalListActivity : AppCompatActivity() {
    private lateinit var headerTitleTextView: TextView

    private lateinit var bottomSheetView: View
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private lateinit var textViewTitle : TextView
    private lateinit var goalGrade : TextView
    lateinit var goalDeleteBtn : android.widget.Button

    private lateinit var btnAddGoal : android.widget.Button

    private var clickedGoal: GoalResponse? = null

    private val goalsListViewModel by viewModels<GoalsListViewModel> {
        GoalsListViewModelFactory(this)
    }

    private lateinit var currentSubject : SubjectResponse
    private var currentSemester = 1

    val gradeStringList = arrayOf("중학교 1학년", "중학교 2학년", "중학교 3학년", "고등학교 1학년", "고등학교 2학년", "고등학교 3학년")
    val semesterStringList = arrayOf("1학기 중간고사", "1학기 기말고사", "2학기 중간고사", "2학기 기말고사")

    lateinit var goalsAdapter : GoalsAdapter
    lateinit var recyclerView :RecyclerView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.goal_list_page)
        supportActionBar?.hide()

        currentSubject = intent.getSerializableExtra("currentSubject") as SubjectResponse
        currentSemester = intent.getIntExtra("semester", -1)

        headerTitleTextView = findViewById(R.id.header_title)
        textViewTitle = findViewById(R.id.textViewTitle)
        goalGrade = findViewById(R.id.goal_grade)

        // 뒤로가기 버튼
        findViewById<ImageView>(R.id.before).setOnClickListener{
            finish()
            overridePendingTransition(0, 0)
        }

        headerTitleTextView.text = currentSubject.subjectName + " 학습목표"
        goalGrade.text = gradeStringList[CurrentUser.selectGrade!!.toInt() - 1] + " " + semesterStringList[currentSemester - 1]

        bottomSheetView = layoutInflater.inflate(R.layout.goal_bottom_sheet, null)
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)

        goalsAdapter = GoalsAdapter({ goal -> adapterOnClick(goal) }, { goalId, updatedGoal -> updateGoal(goalId, updatedGoal) })
        recyclerView = findViewById(R.id.goalRecyclerView)
        recyclerView.adapter = goalsAdapter

        goalDeleteBtn = bottomSheetView.findViewById<Button>(R.id.bsv_delete_btn)

        goalsListViewModel.readGoalList(currentSubject.subjectId, currentSemester)
        goalsListViewModel.goalsLiveData.observe(
            // observer : 어떤 이벤트가 일어난 순간, 이벤트를 관찰하던 관찰자들이 바로 반응하는 패턴
            this,
        ) {
            it?.let { // goalsLiveData의 값이 null이 아닐 때 중괄호 코드 실행
                goalsAdapter.submitList(it as MutableList<GoalResponse>) // 어댑터 내의 데이터를 새 리스트로 업데이트하는 데 사용
            }
        }

        // 목표 추가 버튼
        val modifyButton: Button = findViewById(R.id.buttonModify)
        modifyButton.setOnClickListener {
            btnModifyOnClick()
        }

        val listView = findViewById<RecyclerView>(R.id.goalRecyclerView)
        listView.setHasFixedSize(true)

        // navgation
        var navHome : ImageButton = findViewById(R.id.nav_home)
        var navWrong : ImageButton = findViewById(R.id.nav_wrong)
        var navGoal : ImageButton = findViewById(R.id.nav_goal)
        var navCal : ImageButton = findViewById(R.id.nav_cal)

        navHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent)
        }
        navWrong.setOnClickListener {
            val intent = Intent(this, WrongAnswerListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent)
        }
        navGoal.setOnClickListener {
            val intent = Intent(this, GoalMainListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent)
        }
        navCal.setOnClickListener {
            val intent = Intent(this, Calendar::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent)
        }

    }

    private fun updateGoal(goalId : Int, updatedGoal: GoalPatchRequest) {
        val goalPatchRequest = GoalPatchRequest(
            goal = updatedGoal.goal,
            completed = updatedGoal.completed
        )

        goalsListViewModel.updateGoal(goalId, goalPatchRequest)
    }

    private fun adapterOnClick(goal: GoalResponse) {
        clickedGoal = goal

//        val goalDescription = findViewById<EditText>(R.id.goal_description)

        // TODO : goal(현재 클릭된)에 focus가도록
        if(goal.goal != "")bottomSheetView.findViewById<TextView>(R.id.bsv_title).setText(goal.goal.toString())
        else bottomSheetView.findViewById<TextView>(R.id.bsv_title).setText("목표를 수정하세요")

        bottomSheetDialog.show()

        goalDeleteBtn.setOnClickListener {
            goalsListViewModel.removeGoal(goal.goalId) { isSuccess ->
                if (isSuccess) {
                    goalsListViewModel.readGoalList(currentSubject.subjectId, currentSemester)
                } else {
                }
            }

            bottomSheetDialog.dismiss()
        }
    }

    private fun btnModifyOnClick() {
        val newGoal = GoalRequest(
            // 필요한 필드 값 설정
            goal = "", // 예시 값
            subjectId = currentSubject.subjectId,
            semester = currentSemester,
            completed = false
        )

        goalsAdapter.notifyItemInserted(goalsListViewModel.length() - 1)
        recyclerView.scrollToPosition(goalsListViewModel.length() - 1)

        // ViewModel에 새로운 목표 추가 요청
        goalsListViewModel.createGoal(newGoal)

        // RecyclerView에 새로운 목표 추가
        goalsListViewModel.readGoalList(currentSubject.subjectId, currentSemester)
    }

}