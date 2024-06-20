package kr.hs.emirim.evie.testmateloginpage

import android.content.Intent
import android.graphics.drawable.Drawable
import android.icu.lang.UCharacter.VerticalOrientation
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.coroutines.launch
import kr.hs.emirim.evie.testmateloginpage.api.WrongAnswerRepository
import kr.hs.emirim.evie.testmateloginpage.wrong_answer_note.WrongAnswerNoteImagesActivity
import kr.hs.emirim.evie.testmateloginpage.wrong_answer_note.data.WrongAnswerNoteResponse

class ReadWrongAnswerNoteActivity : AppCompatActivity() {
    private lateinit var before: ImageView

    private lateinit var noteTitle: TextView
    private lateinit var noteGrade: TextView
    private lateinit var noteContent: TextView

    private lateinit var imgContainer : LinearLayout
    private lateinit var noteShowImages: TextView

    private lateinit var mistakeBtn: Button
    private lateinit var timeoutBtn: Button
    private lateinit var lackConceptBtn: Button

    private lateinit var scopeBtn1: Button
    private lateinit var scopeBtn2: Button
    private lateinit var scopeBtn3: Button
    private lateinit var scopeBtn4: Button

    private lateinit var deleteBtn: Button
    private lateinit var editBtn: Button

    private lateinit var imgList : List<String>


    val gradeStringList = arrayOf("중학교 1학년", "중학교 2학년", "중학교 3학년", "고등학교 1학년", "고등학교 2학년", "고등학교 3학년")

    private val wrongAnswerRepository by lazy {
        WrongAnswerRepository.getDataSource(resources)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wrong_answer_note_detail)

        val selectedNote = intent.getSerializableExtra("selectedNote") as? WrongAnswerNoteResponse
        val selectedPosition = intent.getIntExtra("selectedPosition", -1)

        // view findViewById
        before = findViewById(R.id.backBtn)
        noteTitle = findViewById(R.id.note_title)
        noteGrade = findViewById(R.id.note_grade)
        noteContent = findViewById(R.id.note_content)
        imgContainer = findViewById(R.id.img_container)
        noteShowImages = findViewById(R.id.all_imgs)
        mistakeBtn = findViewById(R.id.mistake_btn)
        timeoutBtn = findViewById(R.id.timeout_btn)
        lackConceptBtn = findViewById(R.id.lack_concept_btn)
        scopeBtn1 = findViewById(R.id.scope_btn1)
        scopeBtn2 = findViewById(R.id.scope_btn2)
        scopeBtn3 = findViewById(R.id.scope_btn3)
        scopeBtn4 = findViewById(R.id.scope_btn4)
        deleteBtn = findViewById(R.id.deleteBtn)
        editBtn = findViewById(R.id.editBtn)

        before.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }


        // 오답노트 detail api 연동
        selectedNote?.let {
            lifecycleScope.launch {
                try {
                    val noteDetail = wrongAnswerRepository.getNoteDetail(it.noteId)
                    noteDetail?.let {
                        noteTitle.text = noteDetail.title
                        noteGrade.text = gradeStringList[noteDetail.grade - 1]
                        noteContent.text = noteDetail.styles

                        imgList = parseImageListUrl(noteDetail.imgs)

                        imgList?.let {
                            val maxImages = 3
                            val limitedImageList = it.take(maxImages)
                            val imageMargin = resources.getDimensionPixelSize(R.dimen.dp5)

                            for ((index, url) in limitedImageList.withIndex()) {
                                val imageView = ImageView(this@ReadWrongAnswerNoteActivity)

                                // 이미지 뷰의 레이아웃 파라미터 설정
                                val layoutParams = LinearLayout.LayoutParams(
                                    resources.getDimensionPixelSize(R.dimen.image_size_104dp),
                                    resources.getDimensionPixelSize(R.dimen.image_size_104dp)
                                )

                                // 마지막 이미지를 제외하고 마진을 추가
                                if (index < limitedImageList.size - 1) {
                                    layoutParams.rightMargin = imageMargin
                                }

                                imageView.layoutParams = layoutParams
                                imageView.scaleType = ImageView.ScaleType.CENTER_CROP // 이미지를 뷰에 맞춰 크롭

                                Glide.with(this@ReadWrongAnswerNoteActivity)
                                    .load(url)
                                    .placeholder(R.drawable.placeholder) // 로딩 중 표시할 이미지
                                    .error(R.drawable.img_error) // 에러 시 표시할 이미지
                                    .into(imageView)

                                imgContainer.addView(imageView)
                            }
                        }

                        reasonBtns(noteDetail.reason)
                        rangeBtns(noteDetail.range)
                    }
                } catch (e: Exception) {
                    Log.e("ReadWrongAnswerNote", "Error fetching note detail", e)
                }
            }
        }


        noteShowImages.setOnClickListener {
            val intent = Intent(this@ReadWrongAnswerNoteActivity, WrongAnswerNoteImagesActivity::class.java)
            intent.putStringArrayListExtra("imageList", ArrayList(imgList!!))

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent)
        }
    }

    // 이미지 리스트 생성 메서드
    private fun parseImageListUrl(imageUrlString: String): List<String> {
        // 문자열에서 대괄호와 쉼표를 제거합니다.
        val cleanedString = imageUrlString
            .replace("[", "")
            .replace("]", "")
            .replace(",", "")

        // 공백을 기준으로 잘라서 배열로 만듭니다.
        val imageUrlArray = cleanedString.split(" ")

        // 결과를 반환합니다.
        return imageUrlArray
    }

    fun reasonBtns(reason : String?) {
        when (reason) {
            mistakeBtn.text -> {
                mistakeBtn.setBackgroundResource(R.drawable.bg_green_view)
                mistakeBtn.setTextColor(ContextCompat.getColor(this, R.color.white))
                timeoutBtn.setBackgroundResource(R.drawable.bg_white_view)
                timeoutBtn.setTextColor(ContextCompat.getColor(this, R.color.black_300))
                lackConceptBtn.setBackgroundResource(R.drawable.bg_white_view)
                lackConceptBtn.setTextColor(ContextCompat.getColor(this, R.color.black_300))
            }
            timeoutBtn.text -> {
                mistakeBtn.setBackgroundResource(R.drawable.bg_white_view)
                mistakeBtn.setTextColor(ContextCompat.getColor(this, R.color.black_300))
                timeoutBtn.setBackgroundResource(R.drawable.bg_green_view)
                timeoutBtn.setTextColor(ContextCompat.getColor(this, R.color.white))
                lackConceptBtn.setBackgroundResource(R.drawable.bg_white_view)
                lackConceptBtn.setTextColor(ContextCompat.getColor(this, R.color.black_300))
            }
            else -> {
                mistakeBtn.setBackgroundResource(R.drawable.bg_white_view)
                mistakeBtn.setTextColor(ContextCompat.getColor(this, R.color.black_300))
                timeoutBtn.setBackgroundResource(R.drawable.bg_white_view)
                timeoutBtn.setTextColor(ContextCompat.getColor(this, R.color.black_300))
                lackConceptBtn.setBackgroundResource(R.drawable.bg_green_view)
                lackConceptBtn.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }
    }

    fun rangeBtns(range : String?) {
        when (range) {
            scopeBtn1.text -> {
                scopeBtn1.setBackgroundResource(R.drawable.bg_green_view)
                scopeBtn1.setTextColor(ContextCompat.getColor(this, R.color.white))
                scopeBtn2.setBackgroundResource(R.drawable.bg_white_view)
                scopeBtn2.setTextColor(ContextCompat.getColor(this, R.color.black_300))
                scopeBtn3.setBackgroundResource(R.drawable.bg_white_view)
                scopeBtn3.setTextColor(ContextCompat.getColor(this, R.color.black_300))
                scopeBtn4.setBackgroundResource(R.drawable.bg_white_view)
                scopeBtn4.setTextColor(ContextCompat.getColor(this, R.color.black_300))
            }
            scopeBtn2.text -> {
                scopeBtn1.setBackgroundResource(R.drawable.bg_white_view)
                scopeBtn1.setTextColor(ContextCompat.getColor(this, R.color.black_300))
                scopeBtn2.setBackgroundResource(R.drawable.bg_green_view)
                scopeBtn2.setTextColor(ContextCompat.getColor(this, R.color.white))
                scopeBtn3.setBackgroundResource(R.drawable.bg_white_view)
                scopeBtn3.setTextColor(ContextCompat.getColor(this, R.color.black_300))
                scopeBtn4.setBackgroundResource(R.drawable.bg_white_view)
                scopeBtn4.setTextColor(ContextCompat.getColor(this, R.color.black_300))
            }
            scopeBtn3.text -> {
                scopeBtn1.setBackgroundResource(R.drawable.bg_white_view)
                scopeBtn1.setTextColor(ContextCompat.getColor(this, R.color.black_300))
                scopeBtn2.setBackgroundResource(R.drawable.bg_white_view)
                scopeBtn2.setTextColor(ContextCompat.getColor(this, R.color.black_300))
                scopeBtn3.setBackgroundResource(R.drawable.bg_green_view)
                scopeBtn3.setTextColor(ContextCompat.getColor(this, R.color.white))
                scopeBtn4.setBackgroundResource(R.drawable.bg_white_view)
                scopeBtn4.setTextColor(ContextCompat.getColor(this, R.color.black_300))
            }
            else -> {
                scopeBtn1.setBackgroundResource(R.drawable.bg_white_view)
                scopeBtn1.setTextColor(ContextCompat.getColor(this, R.color.black_300))
                scopeBtn2.setBackgroundResource(R.drawable.bg_white_view)
                scopeBtn2.setTextColor(ContextCompat.getColor(this, R.color.black_300))
                scopeBtn3.setBackgroundResource(R.drawable.bg_white_view)
                scopeBtn3.setTextColor(ContextCompat.getColor(this, R.color.black_300))
                scopeBtn4.setBackgroundResource(R.drawable.bg_green_view)
                scopeBtn4.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }
    }

}