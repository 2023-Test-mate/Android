package kr.hs.emirim.evie.testmateloginpage.wrong_answer_note

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.github.chrisbanes.photoview.PhotoView
import kr.hs.emirim.evie.testmateloginpage.R
import kr.hs.emirim.evie.testmateloginpage.api.WrongAnswerAPIService
import kr.hs.emirim.evie.testmateloginpage.calendar.Calendar
import kr.hs.emirim.evie.testmateloginpage.comm.RetrofitClient
import kr.hs.emirim.evie.testmateloginpage.home.HomeActivity
import kr.hs.emirim.evie.testmateloginpage.subject.GoalMainListActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException

class AddWrongAnswerNoteActivity : AppCompatActivity() {

    // 학년 배열
    private var arrGrade = arrayOf("중학교 1학년", "중학교 2학년", "중학교 3학년", "고등학교 1학년", "고등학교 2학년", "고등학교 3학년")

    // 버튼
    private lateinit var gradeTextView: TextView
    private lateinit var addBtn: Button

    // 네이버바
    private lateinit var navHome: ImageButton
    private lateinit var navWrong: ImageButton
    private lateinit var navGoal: ImageButton
    private lateinit var navCal: ImageButton

    // 오답노트 값들
    private lateinit var selectedReason: String // 틀린 이유
    private var selectedGradeIndex: Int = 1 // 선택한 학년 배열의 인덱스를 저장할 변수
    private lateinit var selectedScope: String // 문제 범위
    private lateinit var noteTitle: EditText // 오답노트 제목
    private lateinit var testStyle: EditText // 시험 스타일
    private lateinit var uploadLayout: LinearLayout // 업로드할 이미지
    private lateinit var imageLayout: LinearLayout
    private lateinit var imageView: ImageView
    private lateinit var uploadBtnFirstLayout: Button

    // 추가된 변수 선언
    private var currentSubjectId: Int = 1
    private var selectedGrade: Int = 1

    // 이미지 관련 변수
    private val PICK_IMAGES_REQUEST = 2 // 이미지 선택 요청 코드
    private val selectedImages = mutableListOf<Bitmap>() // 선택한 이미지를 저장할 리스트

    // Retrofit 서비스 인스턴스
    private lateinit var wrongNoteAPIService: WrongAnswerAPIService
    private lateinit var pre: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wrong_answer_note_add)

        // RetrofitClient를 사용하여 homeAPIService 초기화
        wrongNoteAPIService = RetrofitClient.create(WrongAnswerAPIService::class.java, this)

        val beforeBtn = findViewById<ImageView>(R.id.before)
        beforeBtn.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        // TextView 초기화
        gradeTextView = findViewById(R.id.signup_grade)
        pre = getSharedPreferences("UserInfo", MODE_PRIVATE)
        val editor = pre.edit()

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

        // Intent로부터 값 가져오기
        currentSubjectId = intent.getIntExtra("currentSubjectId", 1)
        selectedGrade = intent.getIntExtra("selectedGrade", 1)
        selectedGradeIndex = selectedGrade // 인덱스 바로 설정
        gradeTextView.text = arrGrade[selectedGrade - 1] // 학년 정보 TextView에 표시

        // 오답노트에 넣을 값들(문제 제목, 학년정보, 시험스타일, 문제&풀이 이미지, 오답이유, 문제 범위)
        selectedReason = "실수"
        selectedScope = "추가자료"
        noteTitle = findViewById(R.id.note_title)
        testStyle = findViewById(R.id.test_style)
        uploadLayout = findViewById(R.id.upload_layout)
        imageLayout = findViewById(R.id.image_layout)
        uploadBtnFirstLayout = findViewById(R.id.upload_btn_first_layout)

        uploadBtnFirstLayout.setOnClickListener {
            openImageChooser()
        }

        addBtn.setOnClickListener {
            // TODO : 선택한 과목의 subjectId와 grade 가져오기
            val subjectId = currentSubjectId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val grade = selectedGradeIndex.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val title = noteTitle.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val styles = testStyle.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val reason = selectedReason.toRequestBody("text/plain".toMediaTypeOrNull())
            val range = selectedScope.toRequestBody("text/plain".toMediaTypeOrNull())

            val imageParts = prepareImageParts(selectedImages)

            val call = wrongNoteAPIService.uploadWrongAnswerNote(
                subjectId,
                grade,
                title,
                imageParts,
                styles,
                reason,
                range
            )

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddWrongAnswerNoteActivity, "Upload successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@AddWrongAnswerNoteActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(this@AddWrongAnswerNoteActivity, "Upload error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
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

    private fun prepareImageParts(images: List<Bitmap>): List<MultipartBody.Part> {
        val imageParts = mutableListOf<MultipartBody.Part>()
        images.forEachIndexed { index, bitmap ->
            val file = File(cacheDir, "image_$index.jpg")
            val outputStream = file.outputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()

            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("imgs", file.name, requestBody)
            imageParts.add(part)
        }
        return imageParts
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
                        selectedImages.add(bitmap)
                        showImage(bitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else if (data?.data != null) {
                val imageUri = data.data!!
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    selectedImages.add(bitmap)
                    showImage(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showImage(bitmap: Bitmap) {
        // PhotoView와 닫기 버튼을 포함할 FrameLayout 생성
        val frameLayout = FrameLayout(this)

        // 이미지를 정사각형으로 자르기
        val squareBitmap = cropToSquare(bitmap)

        val photoView = PhotoView(this)
        photoView.setImageBitmap(squareBitmap)

        // custom drawable을 사용하여 PhotoView의 모서리를 둥글게 설정
        val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, squareBitmap)
        roundedDrawable.cornerRadius = resources.getDimension(R.dimen.image_corner_radius)
        photoView.setImageDrawable(roundedDrawable)

        // PhotoView의 레이아웃 파라미터 설정
        val imageLayoutParams = FrameLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.image_width), // dimens에서 너비 설정
            resources.getDimensionPixelSize(R.dimen.image_height) // dimens에서 높이 설정
        )
        photoView.layoutParams = imageLayoutParams

        // 닫기 버튼 생성
        val closeButton = ImageButton(this)
        closeButton.setImageResource(R.drawable.round_x_icon_black) // 닫기 아이콘 설정
        val closeButtonParams = FrameLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.close_button_size), // dimens에서 너비 설정
            resources.getDimensionPixelSize(R.dimen.close_button_size)  // dimens에서 높이 설정
        )
        closeButtonParams.gravity = Gravity.END // 오른쪽 상단 정렬
        closeButtonParams.marginEnd = resources.getDimensionPixelSize(R.dimen.close_button_padding)
        closeButtonParams.topMargin = resources.getDimensionPixelSize(R.dimen.close_button_padding)
        closeButton.layoutParams = closeButtonParams
        closeButton.setOnClickListener {
            imageLayout.removeView(frameLayout) // ImageView와 닫기 버튼을 포함하는 FrameLayout 제거
        }

        // frameLayout에 photoView와 closeButton 추가
        frameLayout.addView(photoView)
        frameLayout.addView(closeButton)

        // frameLayout의 레이아웃 파라미터 설정
        val frameLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        frameLayoutParams.marginEnd = resources.getDimensionPixelSize(R.dimen.image_margin)
        frameLayoutParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.image_margin)
        frameLayout.layoutParams = frameLayoutParams

        imageLayout.addView(frameLayout) // frameLayout을 imageLayout에 추가

        // 업로드된 이미지가 포함된 부모 레이아웃을 표시
        uploadLayout.visibility = View.GONE
        findViewById<LinearLayout>(R.id.image_layout_parent).visibility = View.VISIBLE
    }

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val dimension = Math.min(bitmap.width, bitmap.height)
        return Bitmap.createBitmap(bitmap, 0, 0, dimension, dimension)
    }
}
