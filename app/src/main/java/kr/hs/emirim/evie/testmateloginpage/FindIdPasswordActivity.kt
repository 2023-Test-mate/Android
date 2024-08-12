package kr.hs.emirim.evie.testmateloginpage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

class FindIdPasswordActivity : AppCompatActivity() {
    private var isVisibleBtn = false
    private var isVisibleCheckBtn = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_id_password)

        val btnFindId: Button = findViewById(R.id.btn_find_id)
        val btnFindPw: Button = findViewById(R.id.btn_find_pw)

        val btnVisible1: ImageButton = findViewById(R.id.visible_btn1)
        val btnVisible2: ImageButton = findViewById(R.id.visible_btn2)
        val editPw: EditText = findViewById(R.id.edit_pw)
        val editPwCheck: EditText = findViewById(R.id.edit_pw_check)

        // 초기 상태 설정 (아이디 찾기 활성화)
        setButtonBackground(btnFindId, true)
        setButtonBackground(btnFindPw, false)

        // 버튼1 클릭 시
        btnFindId.setOnClickListener {
            setIdPwLayout("Id")
            setButtonBackground(btnFindId, true)
            setButtonBackground(btnFindPw, false)
        }

        // 버튼2 클릭 시
        btnFindPw.setOnClickListener {
            setIdPwLayout("Pw")
            setButtonBackground(btnFindId, false)
            setButtonBackground(btnFindPw, true)
        }

        btnVisible1.setOnClickListener {
            isVisibleBtn = !isVisibleBtn
            if (isVisibleBtn) {
                btnVisible1.setImageResource(R.drawable.icon_invisible)
                editPw.inputType = InputType.TYPE_CLASS_TEXT
            } else {
                btnVisible1.setImageResource(R.drawable.icon_visible)
                editPw.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        btnVisible2.setOnClickListener {
            isVisibleCheckBtn = !isVisibleCheckBtn
            if (isVisibleCheckBtn) {
                btnVisible2.setImageResource(R.drawable.icon_invisible)
                editPwCheck.inputType = InputType.TYPE_CLASS_TEXT
            } else {
                btnVisible2.setImageResource(R.drawable.icon_visible)
                editPwCheck.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
    }

    private fun setButtonBackground(button: Button, isSelected: Boolean) {
        val drawableResId = if (isSelected) R.drawable.btn_border_bottom_green else R.drawable.btn_border_bottom_gray
        val drawable = ContextCompat.getDrawable(this, drawableResId)
        button.background = drawable
    }

    private fun setIdPwLayout(button : String) {
        val layoutId : LinearLayout = findViewById(R.id.layoutId)
        val layoutPw : LinearLayout = findViewById(R.id.layoutPw)

        if(button == "Id") {
            layoutId.visibility = View.VISIBLE
            layoutPw.visibility = View.GONE
        } else {
            layoutPw.visibility = View.VISIBLE
            layoutId.visibility = View.GONE
        }
    }
}