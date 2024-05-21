package kr.hs.emirim.evie.testmateloginpage.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kr.hs.emirim.evie.testmateloginpage.R
import kr.hs.emirim.evie.testmateloginpage.home.data.TestData

class EditTestRecordActivity : AppCompatActivity() {
    // 시험기록 데이터 생성
    val testRecordDataList: List<TestData> = listOf(
        TestData("1학년 2학기 기말",75),
        TestData("1학년 2학기 중간",60),
        TestData("1학년 1학기 기말",80),
        TestData("1학년 1학기 중간",100),
        TestData("@학년 @학기 중간",89),
        TestData("@학년 @학기 중간",91),
        TestData("@학년 @학기 중간",78),
        TestData("@학년 @학기 중간",96)
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_test_record)

        // 이전 페이지 버튼
        var beforeBtn = findViewById<ImageView>(R.id.backBtn)

        beforeBtn.setOnClickListener{
            onBackPressed();
        }


        // 성적 그래프
        val linechart = findViewById<LineChart>(R.id.test_record_chart)
        val horizontalScrollView = findViewById<HorizontalScrollView>(R.id.scroll_view_graph)
        val scoreChart = ScoreChart(linechart, horizontalScrollView, this) // MPAndroidChart 커스텀 클래스
        scoreChart.setupChart(testRecordDataList)


        // 시험 날짜 설정 버튼
        val BtnSetTestDate = findViewById<Button>(R.id.btn_test_date)

        BtnSetTestDate.setOnClickListener {
            val newFragment: DialogFragment = DatePickerFragment()
            newFragment.show(supportFragmentManager, "datePicker")
        }

        // 시험 난이도 RatingBar
        val RatingBarTestDifficulty = findViewById<RatingBar>(R.id.ratingTestDifficulty)
        RatingBarTestDifficulty.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            // 등급을 로그에 출력
            Log.d("Rating", "Current rating: $rating")
        }

        // 저장 버튼
        val BtnSave = findViewById<Button>(R.id.buttonSave)

        BtnSave.setOnClickListener {

        }
    }

    fun processDatePickerResult(year: Int, month: Int, day: Int) {
        val monthString = (month + 1).toString()
        val dayString = day.toString()
        val yearString = year.toString()
        val dateMessage = "$monthString/$dayString/$yearString"

        Toast.makeText(this, "Date: $dateMessage", Toast.LENGTH_SHORT).show()
    }
}