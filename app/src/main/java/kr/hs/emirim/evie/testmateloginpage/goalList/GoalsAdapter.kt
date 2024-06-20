package kr.hs.emirim.evie.testmateloginpage.goalList

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import kr.hs.emirim.evie.testmateloginpage.R
import kr.hs.emirim.evie.testmateloginpage.goalList.data.Goal
import kr.hs.emirim.evie.testmateloginpage.goalList.data.GoalPatchRequest
import kr.hs.emirim.evie.testmateloginpage.goalList.data.GoalResponse
import kr.hs.emirim.evie.testmateloginpage.goalList.data.toGoalPatchRequest


class GoalsAdapter(private val onClick: (GoalResponse) -> Unit, private val onUpdate: (Int, GoalPatchRequest) -> Unit) :
    ListAdapter<GoalResponse, GoalsAdapter.GoalViewHolder>(GoalDiffCallback) {

    inner class GoalViewHolder(itemView: View, val onClick: (GoalResponse) -> Unit, val onUpdate: (Int, GoalPatchRequest) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        val goalEditText: EditText = itemView.findViewById(R.id.goal_description)
        val goalCheckBox: AppCompatCheckBox = itemView.findViewById(R.id.goal_checked)
        val goalLayout: RelativeLayout = itemView.findViewById(R.id.goal_background)
        var currentGoal: GoalResponse? = null

        private lateinit var goalEditBtn: Button

        init {
            itemView.setOnClickListener {
                currentGoal?.let {
                    onClick(it)
                }
            }

            goalEditText.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    currentGoal?.let {
                        it.goal = goalEditText.text.toString()
                        val goalPatchRequest = it.toGoalPatchRequest()
                        onUpdate(it.goalId, goalPatchRequest)
                    }
                    goalEditText.clearFocus() // 포커스 없애기
                    closeKeyboard(goalEditText) // 키보드 닫기

                    return@setOnEditorActionListener true
                } else {
                    false
                }
            }

            goalCheckBox.setOnCheckedChangeListener { _, isChecked ->
                currentGoal?.let {
                    it.completed = isChecked
                    val goalPatchRequest = it.toGoalPatchRequest()
                    onUpdate(it.goalId, goalPatchRequest)
                    updateUI(it.completed)
                }
            }
        }

        /* UI에 정보 바인딩(넣는 메서드) */
        fun bind(goal: GoalResponse) {
            currentGoal = goal
            goalEditText.setText(goal.goal)
            goalCheckBox.isChecked = goal.completed
            updateUI(goal.completed)
        }

        private fun updateUI(completed: Boolean) {
            if (completed) {
                goalLayout.setBackgroundResource(R.drawable.bg_green_stroke_view)
                goalEditText.setTextColor(itemView.context.getColor(R.color.green_500))
            } else {
                goalLayout.setBackgroundResource(R.drawable.bg_white_view)
                goalEditText.setTextColor(itemView.context.getColor(R.color.black))
            }
        }

        private fun closeKeyboard(view: View) {
            val imm = itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.goal_layout, parent, false)

        return GoalViewHolder(view, onClick, onUpdate)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(getItem(position)) // getItem(position) : 현재 아이템
    }
}

object GoalDiffCallback : DiffUtil.ItemCallback<GoalResponse>() {
    override fun areItemsTheSame(oldItem: GoalResponse, newItem: GoalResponse): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: GoalResponse, newItem: GoalResponse): Boolean {
        return oldItem.goalId == newItem.goalId
    }
}