import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.hs.emirim.evie.testmateloginpage.R
import java.text.SimpleDateFormat
import java.util.*

class DividerItemDecoration(@ColorInt private val color: Int, private val dividerHeight: Int) :
    RecyclerView.ItemDecoration() {

    private val dividerPaint: Paint = Paint()

    init {
        dividerPaint.color = color
        dividerPaint.strokeWidth = dividerHeight.toFloat()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        for (i in 0 until parent.childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + dividerHeight
            c.drawLine(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), dividerPaint)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.bottom = dividerHeight
    }
}


class CalendarPagerAdapter : RecyclerView.Adapter<CalendarPagerAdapter.CalendarViewHolder>() {

    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy년 M월", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        calendar.time = Date()
        calendar.add(Calendar.MONTH, position - (Int.MAX_VALUE / 2))
        val formattedDate = dateFormat.format(calendar.time)
        holder.monthYearTextView.text = formattedDate

        val days = generateDaysForMonth(calendar)
        val adapter = DaysAdapter(days)
        holder.recyclerView.layoutManager = GridLayoutManager(holder.itemView.context, 7)
        holder.recyclerView.adapter = adapter

        // 아이템 데코레이션 추가
        val colorE6E6E6 = Color.parseColor("#E6E6E6")
        val decoration = DividerItemDecoration(colorE6E6E6, 1)
        holder.recyclerView.addItemDecoration(decoration)
    }

    private fun generateDaysForMonth(calendar: Calendar): List<String> {
        val days = mutableListOf<String>()
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dateFormat = SimpleDateFormat("d", Locale.getDefault())

        // Adding empty strings for the days of the week before the 1st of the month
        val firstDayOfMonth = calendar.clone() as Calendar
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        val dayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1
        for (i in 0 until dayOfWeek) {
            days.add("")
        }

        for (day in 1..maxDay) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            days.add(dateFormat.format(calendar.time))
        }
        return days
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val monthYearTextView: TextView = itemView.findViewById(R.id.monthYearTextView)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
    }

    class DaysAdapter(private val days: List<String>) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {

        private val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
            return DayViewHolder(view)
        }

        override fun onBindViewHolder(holder: DaysAdapter.DayViewHolder, position: Int) {
            val day = days[position]

            // 오늘 날짜 여부 확인
            val isToday = isToday(day.toIntOrNull()) // day를 Int로 변환하여 오늘 날짜와 비교

            if (isToday) {
                holder.dayTextView.setBackgroundResource(R.drawable.bg_circle_green) // 배경색 설정
                holder.dayTextView.setTextColor(Color.WHITE) // 텍스트 색상 설정
            } else {
                holder.dayTextView.setBackgroundResource(0) // 배경색 없음
                holder.dayTextView.setTextColor(Color.BLACK) // 기본 텍스트 색상
            }

            holder.dayTextView.text = day
        }

        // 오늘 날짜인지 여부를 판별하는 함수
        private fun isToday(day: Int?): Boolean {
            if (day == null) return false
            val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            return day == today
        }

        override fun getItemCount(): Int {
            return days.size
        }

        class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val dayTextView: TextView = itemView.findViewById(R.id.dayTextView)
        }
    }
}