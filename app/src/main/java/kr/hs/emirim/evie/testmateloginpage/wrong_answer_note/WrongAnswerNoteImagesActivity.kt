package kr.hs.emirim.evie.testmateloginpage.wrong_answer_note

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import kr.hs.emirim.evie.testmateloginpage.R
import kr.hs.emirim.evie.testmateloginpage.api.WrongAnswerRepository

class WrongAnswerNoteImagesActivity : AppCompatActivity() {
    private lateinit var before: ImageView

    private lateinit var imgContainer : LinearLayout

    private val wrongAnswerRepository by lazy {
        WrongAnswerRepository.getDataSource(resources)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wrong_answer_note_images)

        before = findViewById(R.id.backBtn)
        before.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        imgContainer = findViewById(R.id.image_container)

        val imageList = intent.getSerializableExtra("imageList") as? List<String>

        imageList?.let {
            for (url in it) {
                val photoView = PhotoView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also {
                        it.bottomMargin = resources.getDimensionPixelSize(R.dimen.M10)
                    }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    adjustViewBounds = true
                }

                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.img_placeholder) // 로딩 중 표시할 이미지
                    .error(R.drawable.img_error) // 에러 시 표시할 이미지
                    .into(photoView)

                imgContainer.addView(photoView)
            }
        }


        val imageContainer = findViewById<LinearLayout>(R.id.image_container)


    }
}