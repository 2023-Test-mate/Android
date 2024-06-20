package kr.hs.emirim.evie.testmateloginpage.wrong_answer_note

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kr.hs.emirim.evie.testmateloginpage.NavigationButtons
import kr.hs.emirim.evie.testmateloginpage.R
import kr.hs.emirim.evie.testmateloginpage.ReadWrongAnswerNoteActivity
import kr.hs.emirim.evie.testmateloginpage.login.CurrentUser
import kr.hs.emirim.evie.testmateloginpage.subject.SubjectViewModel
import kr.hs.emirim.evie.testmateloginpage.subject.SubjectViewModelFactory
import kr.hs.emirim.evie.testmateloginpage.subject.WrongAnswerSubjectAdapter
import kr.hs.emirim.evie.testmateloginpage.subject.data.SubjectResponse
import kr.hs.emirim.evie.testmateloginpage.util.SpinnerUtil.Companion.gradeSpinner
import kr.hs.emirim.evie.testmateloginpage.wrong_answer_note.data.WrongAnswerNoteResponse

class WrongAnswerListActivity : AppCompatActivity() {

    lateinit var addPage : FloatingActionButton

    lateinit var spinner: Spinner

    var currentSubject = 1

    private val subjectViewModel by viewModels<SubjectViewModel> {
        SubjectViewModelFactory(this)
    }

    private val listViewModel by viewModels<WrongAnswerListViewModel> {
        WrongAnswerListViewModelFactory(this)
    }

    var selectedPosition : Int? = null

    private lateinit var navigationButtons: NavigationButtons
    private lateinit var subjectAdapter: WrongAnswerSubjectAdapter
    private lateinit var listAdapter: WrongAnswerListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wrong_answer_note)

        // 학년 spiner api 연동
        spinner = gradeSpinner(this, R.id.spinnerWrong)
        spinner.setSelection(CurrentUser.selectGrade!! - 1)
        selectedPosition = spinner.selectedItemPosition// grade 인덱스 (ex. 3)
        var selectedItem = spinner.getItemAtPosition(selectedPosition!!).toString() // grade 문자열 (ex. 고등학교 2학년)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 선택된 항목의 위치(position)를 이용하여 해당 항목의 값을 가져옴
                selectedPosition = position + 1
                CurrentUser.selectGrade = spinner.selectedItemPosition + 1
                subjectViewModel.readSubjectList(selectedPosition!!)
                listViewModel.clearList(selectedPosition!!)
                listViewModel.readNoteList(CurrentUser.selectGrade!!, currentSubject)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무 것도 선택되지 않았을 때 처리할 작업
            }
        }

        // 오답노트 추가 버튼
        addPage = findViewById(R.id.addBtn)



        // 과목 버튼 recyclerView
        subjectAdapter = WrongAnswerSubjectAdapter(object : (SubjectResponse, Int) -> Unit {
            override fun invoke(subject: SubjectResponse, position: Int) {
                currentSubject = subject.subjectId
                subjectAdapterOnClick(subject, position)
            }
        })
        val subjectRecyclerView: RecyclerView = findViewById(R.id.wrongAnswerSubjectRecyclerView)

        subjectRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) // 수평 레이아웃 방향 설정
        subjectRecyclerView.adapter = subjectAdapter

        subjectViewModel.readSubjectList(CurrentUser.userDetails!!.grade.toInt()) // list 가져오기
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

        // 오답노트 recyclerView
        listAdapter = WrongAnswerListAdapter { list, position -> noteAdapterOnClick(list, position) }
        val listRecyclerView: RecyclerView = findViewById(R.id.wrongAnswerObjectRecyclerView)

        listRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) // 수평 레이아웃 방향 설정
        listRecyclerView.adapter = listAdapter

        listViewModel.wrongAnswerListData.observe(
            // observer : 어떤 이벤트가 일어난 순간, 이벤트를 관찰하던 관찰자들이 바로 반응하는 패턴
            this
        ) {
            it?.let { // goalsLiveData의 값이 null이 아닐 때 중괄호 코드 실행
                listAdapter.submitList(it as MutableList<WrongAnswerNoteResponse>) // 어댑터 내의 데이터를 새 리스트로 업데이트하는 데 사용
            }
        }

        addPage.setOnClickListener {
            val intent = Intent(this, AddWrongAnswerNoteActivity::class.java).apply {
                putExtra("currentSubjectId", currentSubject)
                putExtra("selectedGrade", selectedPosition)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }

        // 네비게이션
        navigationButtons = NavigationButtons(this)
        navigationButtons.initialize(this)
    }

    private fun subjectAdapterOnClick(subject: SubjectResponse, position: Int) {
        subjectAdapter.updateSelectedPosition(position)
        listViewModel.readNoteList(CurrentUser.selectGrade!!, subject.subjectId)
        Log.d("subjectAdapterOnClick", subject.subjectId.toString())
    }

    private fun noteAdapterOnClick(list : WrongAnswerNoteResponse, position: Int) {
        listAdapter.updateSelectedPosition(position)
        val intent = Intent(this, ReadWrongAnswerNoteActivity::class.java)
        intent.putExtra("selectedNote", list)
        intent.putExtra("selectedPosition", position)

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent)
    }
}