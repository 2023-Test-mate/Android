package kr.hs.emirim.evie.testmateloginpage.wrong_answer_note

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kr.hs.emirim.evie.testmateloginpage.calendar.Calendar
import kr.hs.emirim.evie.testmateloginpage.R
import kr.hs.emirim.evie.testmateloginpage.subject.GoalMainListActivity
import kr.hs.emirim.evie.testmateloginpage.home.HomeActivity
import android.graphics.Bitmap
import android.widget.*
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import java.io.IOException

class AddWrongAnswerNoteActivity : AppCompatActivity() {

    lateinit var btnGradeDialog: Button
    lateinit var addBtn: Button

    lateinit var navHome: ImageButton
    lateinit var navWrong: ImageButton
    lateinit var navGoal: ImageButton
    lateinit var navCal: ImageButton

    lateinit var pre: SharedPreferences

    var arrGrade = arrayOf("중학교 1학년", "중학교 2학년", "중학교 3학년", "고등학교 1학년", "고등학교 2학년", "고등학교 3학년")

    private lateinit var selectedReason: String
    private lateinit var selectedScope: String

    private lateinit var uploadLayout: LinearLayout
    private lateinit var imageLayout: LinearLayout
    private lateinit var imageView: ImageView
    private lateinit var uploadBtnFirstLayout: Button

    private val PICK_IMAGES_REQUEST = 2 // 이미지 선택 요청 코드

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wrong_answer_note_add)

        selectedReason = "실수"
        selectedScope = "추가자료"

        var beforeBtn = findViewById<ImageView>(R.id.before)
        beforeBtn.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        // spinner
        btnGradeDialog = findViewById(R.id.signup_grade)
        pre = getSharedPreferences("UserInfo", MODE_PRIVATE)
        val editor = pre.edit()

        btnGradeDialog.setOnClickListener {
            val dlg = AlertDialog.Builder(this@AddWrongAnswerNoteActivity, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
            dlg.setTitle("학년정보")
            dlg.setItems(arrGrade) { dialog, index ->
                btnGradeDialog.text = arrGrade[index]
                editor.putString("usergrade", arrGrade[index])
                editor.apply()
            }
            dlg.setNegativeButton("닫기") { dialog, which ->
                dialog.dismiss()
            }
            dlg.create().show()
        }

        // 오답이유 버튼들
        val reasonButtons = listOf(
            findViewById<Button>(R.id.mistake_btn),
            findViewById<Button>(R.id.timeout_btn),
            findViewById<Button>(R.id.lack_concept_btn)
        )

        // 문제 범위 버튼들
        val scopeButtons = listOf(
            findViewById<Button>(R.id.scope_btn1),
            findViewById<Button>(R.id.scope_btn2),
            findViewById<Button>(R.id.scope_btn3),
            findViewById<Button>(R.id.scope_btn4)
        )

        initializeButtonListeners(reasonButtons) { selectedButton ->
            selectedReason = selectedButton.text.toString()
        }

        initializeButtonListeners(scopeButtons) { selectedButton ->
            selectedScope = selectedButton.text.toString()
        }

        addBtn = findViewById(R.id.addBtn)
        addBtn.setOnClickListener {
            onBackPressed()
        }

        uploadLayout = findViewById(R.id.upload_layout)
        imageLayout = findViewById(R.id.image_layout)
        imageView = findViewById(R.id.imageView)
        uploadBtnFirstLayout = findViewById(R.id.upload_btn_first_layout)

        uploadBtnFirstLayout.setOnClickListener {
            openImageChooser()
        }

        // navgation
        navHome = findViewById(R.id.nav_home)
        navWrong = findViewById(R.id.nav_wrong)
        navGoal = findViewById(R.id.nav_goal)
        navCal = findViewById(R.id.nav_cal)

        navHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
        navWrong.setOnClickListener {
            val intent = Intent(this, WrongAnswerListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
        navGoal.setOnClickListener {
            val intent = Intent(this, GoalMainListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
        navCal.setOnClickListener {
            val intent = Intent(this, Calendar::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
        }
    }

    private fun initializeButtonListeners(buttons: List<Button>, onSelect: (Button) -> Unit) {
        buttons.forEach { button ->
            button.setOnClickListener {
                handleButtonClick(button, buttons, onSelect)
            }
        }
    }

    private fun handleButtonClick(selectedButton: Button, buttons: List<Button>, onSelect: (Button) -> Unit) {
        buttons.forEach { button ->
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.bg_green_view)
                button.setTextColor(ContextCompat.getColor(this, R.color.white))
                onSelect(button)
            } else {
                button.setBackgroundResource(R.drawable.bg_white_view)
                button.setTextColor(ContextCompat.getColor(this, R.color.black_300))
            }
        }
    }

    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // 여러 이미지 선택 가능하도록 설정
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "이미지 선택"), PICK_IMAGES_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data?.clipData != null) {
                val clipData = data.clipData!!
                for (i in 0 until clipData.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        showImage(bitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else if (data?.data != null) {
                val imageUri = data.data!!
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    showImage(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showImage(bitmap: Bitmap) {
        val photoView = PhotoView(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.M10)
        photoView.layoutParams = layoutParams

        Glide.with(this)
            .load(bitmap)
            .placeholder(R.drawable.placeholder) // 로딩 중 표시할 이미지
            .error(R.drawable.img_error) // 에러 시 표시할 이미지
            .into(photoView)

        imageLayout.addView(photoView) // 이미지를 보여줄 LinearLayout에 추가
        imageLayout.visibility = View.VISIBLE // 이미지 레이아웃을 보이도록 설정
        uploadLayout.visibility = View.GONE // 업로드 레이아웃은 숨김
    }
}