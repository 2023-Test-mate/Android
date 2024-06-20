package kr.hs.emirim.evie.testmateloginpage.home

import android.content.Context
import android.graphics.Canvas
import android.widget.HorizontalScrollView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import kr.hs.emirim.evie.testmateloginpage.R
import kr.hs.emirim.evie.testmateloginpage.home.data.HomeSubjectInfoResponse

class ScoreChart(
    private val lineChart: LineChart,
    private val scrollView: HorizontalScrollView,
    private val context: Context
) {

    fun setupChart(testRecordDataList: List<HomeSubjectInfoResponse.Exam>) {
        val xAxis: XAxis = lineChart.xAxis
        val yAxisLeft = lineChart.axisLeft
        val screenWidth = context.resources.displayMetrics.widthPixels
        val params = lineChart.layoutParams

        val entries: MutableList<Entry> = mutableListOf()
        for (i in testRecordDataList.indices) {
            entries.add(Entry(i.toFloat(), testRecordDataList[i].examScore.toFloat()))
        }
        val lineDataSet = LineDataSet(entries, "entries")

        xAxis.gridColor = ContextCompat.getColor(context, R.color.black_100)
        yAxisLeft.gridColor = ContextCompat.getColor(context, R.color.black_100)

        lineDataSet.apply {
            color = ContextCompat.getColor(context, R.color.green_500)
            circleRadius = 5f
            lineWidth = 3f
            setCircleColor(ContextCompat.getColor(context, R.color.green_500))
            circleHoleColor = ContextCompat.getColor(context, R.color.green_500)
            setDrawHighlightIndicators(false)
            setDrawValues(true)
            valueTextColor = ContextCompat.getColor(context, R.color.black)
            valueFormatter = DefaultValueFormatter(1)
            valueTextSize = 10f
        }

        lineChart.apply {
            params.width = (screenWidth * 1.2).toInt()
            layoutParams = params
            axisRight.isEnabled = false
            axisLeft.isEnabled = true
            legend.isEnabled = false
            description.isEnabled = false
            isDragXEnabled = true
            isScaleYEnabled = false
            isScaleXEnabled = false

            axisLeft.apply {
                axisMinimum = 20f
                axisMaximum = 100f
                setLabelCount(5, true)
                textSize = 10f
                textColor = ContextCompat.getColor(context, R.color.black)
                textColor = ContextCompat.getColor(context, R.color.black_300)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}"
                    }
                }
                axisLineColor = ContextCompat.getColor(context, R.color.black_300)
            }
        }

        xAxis.apply {
            setDrawGridLines(false)
            setDrawAxisLine(true)
            setDrawLabels(true)
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = XAxisCustomFormatter(changeTestDateText(testRecordDataList))
            textColor = ContextCompat.getColor(context, R.color.black)
            textSize = 10f
            labelRotationAngle = 0f
            yOffset = +4f
            setLabelCount(testRecordDataList.size, true)
            axisLineColor = ContextCompat.getColor(context, R.color.black_300)
            spaceMin = 0.05f
        }

        // 커스텀 XAxisRenderer 설정
        lineChart.setXAxisRenderer(
            XAxisRendererCustom(
                lineChart.viewPortHandler,
                lineChart.xAxis,
                lineChart.getTransformer(YAxis.AxisDependency.LEFT),
                15f // 오른쪽으로 이동할 픽셀 수
            )
        )

        scrollView.post {
            scrollView.scrollTo(lineChart.width, 0)
        }

        lineChart.apply {
            data = LineData(lineDataSet)
            notifyDataSetChanged()
            invalidate()
        }
    }

    private fun changeTestDateText(dataList: List<HomeSubjectInfoResponse.Exam>): List<String> {
        val dataTextList = ArrayList<String>()
        for (data in dataList) {
            dataTextList.add(data.examName)
        }
        return dataTextList
    }

    private inner class XAxisCustomFormatter(private val xAxisData: List<String>) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return xAxisData.getOrNull(value.toInt()) ?: ""
        }
    }
}

class XAxisRendererCustom(
    viewPortHandler: ViewPortHandler,
    xAxis: XAxis,
    trans: Transformer,
    private val shift: Float
) : XAxisRenderer(viewPortHandler, xAxis, trans) {

    override fun drawLabels(canvas: Canvas, pos: Float, anchor: com.github.mikephil.charting.utils.MPPointF) {
        val labelRotationAngleDegrees = mXAxis.labelRotationAngle
        val centeringEnabled = mXAxis.isCenterAxisLabelsEnabled
        val positions = FloatArray(mXAxis.mEntryCount * 2)

        for (i in positions.indices step 2) {
            if (centeringEnabled) {
                positions[i] = mXAxis.mCenteredEntries[i / 2]
            } else {
                positions[i] = mXAxis.mEntries[i / 2]
            }
        }

        mTrans.pointValuesToPixel(positions)

        for (i in positions.indices step 2) {
            var x = positions[i]

            if (mViewPortHandler.isInBoundsX(x)) {
                val label = mXAxis.valueFormatter?.getFormattedValue(mXAxis.mEntries[i / 2]) ?: mXAxis.mEntries[i / 2].toString()
                x += shift // 오른쪽으로 이동할 픽셀 수를 추가

                if (mXAxis.isAvoidFirstLastClippingEnabled) {
                    if (i == mXAxis.mEntryCount - 1 && mXAxis.mEntryCount > 1) {
                        val width = Utils.calcTextWidth(mAxisLabelPaint, label).toFloat()
                        if (width > mViewPortHandler.offsetRight() * 2 && x + width > mViewPortHandler.chartWidth) x -= width / 2
                    } else if (i == 0) {
                        val width = Utils.calcTextWidth(mAxisLabelPaint, label).toFloat()
                        x += width / 2
                    }
                }

                drawLabel(canvas, label, x, pos, anchor, labelRotationAngleDegrees)
            }
        }
    }
}
