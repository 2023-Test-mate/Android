package kr.hs.emirim.evie.testmateloginpage

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kr.hs.emirim.evie.testmateloginpage.databinding.ListItemDayBinding
import java.util.Calendar
import java.util.Date

class AdapterDay(private val tempMonth: Int, private val dayList: MutableList<Date>) :
    RecyclerView.Adapter<AdapterDay.DayView>() {
    private val ROW = 6

    inner class DayView(val binding: ListItemDayBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayView {
        val binding = ListItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayView(binding)
    }

    override fun onBindViewHolder(holder: DayView, position: Int) {
        val date = dayList[position]
        val context = holder.itemView.context
        val binding = holder.binding

        binding.itemDayLayout.setOnClickListener {
            Toast.makeText(context, "${date}", Toast.LENGTH_SHORT).show()
        }

        binding.itemDayText.text = date.date.toString()

        val today = Calendar.getInstance()
        if (today.get(Calendar.YEAR) == date.year &&
            today.get(Calendar.MONTH) == date.month &&
            today.get(Calendar.DAY_OF_MONTH) == date.date) {
            binding.itemDayText.setTextColor(Color.WHITE)
            binding.itemDayText.setBackgroundResource(R.drawable.icon_today_background)
        } else {
            // 다른 날짜의 스타일 설정
            binding.itemDayText.setTextColor(
                when (position % 7) {
                    0 -> Color.RED
                    6 -> Color.BLUE
                    else -> Color.BLACK
                }
            )

            if (tempMonth != date.month) {
                binding.itemDayText.alpha = 0.4f
            }
        }
    }

    override fun getItemCount(): Int {
        return ROW * 7
    }
}