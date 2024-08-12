package kr.hs.emirim.evie.testmateloginpage.subject

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import kr.hs.emirim.evie.testmateloginpage.R
import kr.hs.emirim.evie.testmateloginpage.login.CurrentUser
import kr.hs.emirim.evie.testmateloginpage.subject.data.SubjectRequest

const val SUBJECT_NAME = "새로운 과목"
const val BOOK_TAG = "이미지 주소" // R.drawable.book_red

class AddSubjectActivity : AppCompatActivity() {
    private lateinit var addSubjectName: EditText
    lateinit var finishBtn : androidx.appcompat.widget.AppCompatImageButton
    lateinit var bookImg : ImageView
    var bookImgPath : String? = "img_book_red"

    private val subjectViewModel by viewModels<SubjectViewModel> {
        SubjectViewModelFactory(this)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_subjects)
        supportActionBar?.hide()

        addSubjectName = findViewById(R.id.subjectName)

        finishBtn = findViewById(R.id.finish_edit_subject)
        finishBtn.setOnClickListener{
            finish()
            overridePendingTransition(0, 0)
        }

        findViewById<Button>(R.id.subjectAddBtn).setOnClickListener {
            val grade = CurrentUser.selectGrade
            val subjectName = addSubjectName.text.toString()
            Log.d("retrofit", grade.toString())
            Log.d("retrofit", subjectName)
            Log.d("retrofit", bookImgPath!!)

            if (subjectName.isNotEmpty()) {
                val newSubject = SubjectRequest(grade, subjectName, bookImgPath)
                subjectViewModel.createSubject(newSubject)
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Log.d("AddSubjectActivity", "Subject name cannot be empty")
            }
        }

        bookImg = findViewById(R.id.book_img)
        findViewById<ImageButton>(R.id.red).setOnClickListener {
            bookImg.setImageResource(R.drawable.img_book_red)
            bookImgPath = "img_book_red"
        }
        findViewById<ImageButton>(R.id.green).setOnClickListener {
            bookImg.setImageResource(R.drawable.img_book_green)
            bookImgPath = "img_book_green"
        }
        findViewById<ImageButton>(R.id.purple).setOnClickListener {
            bookImg.setImageResource(R.drawable.img_book_purple)
            bookImgPath = "img_book_purple"
        }
        findViewById<ImageButton>(R.id.blue).setOnClickListener {
            bookImg.setImageResource(R.drawable.img_book_blue)
            bookImgPath = "img_book_blue"
        }
        findViewById<ImageButton>(R.id.yellow).setOnClickListener {
            bookImg.setImageResource(R.drawable.img_book_yellow)
            bookImgPath = "img_book_yellow"
        }
        findViewById<ImageButton>(R.id.black).setOnClickListener {
            bookImg.setImageResource(R.drawable.img_book_black)
            bookImgPath = "img_book_black"
        }
    }
}