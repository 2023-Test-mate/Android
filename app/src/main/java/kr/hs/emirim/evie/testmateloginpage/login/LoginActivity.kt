package kr.hs.emirim.evie.testmateloginpage.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kr.hs.emirim.evie.testmateloginpage.LoginResponse
import kr.hs.emirim.evie.testmateloginpage.R
import kr.hs.emirim.evie.testmateloginpage.comm.RetrofitClient
import kr.hs.emirim.evie.testmateloginpage.userData.UserDetailsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kr.hs.emirim.evie.testmateloginpage.TMService
import kr.hs.emirim.evie.testmateloginpage.comm.SessionManager
import kr.hs.emirim.evie.testmateloginpage.home.HomeActivity
import kr.hs.emirim.evie.testmateloginpage.signup.Signup_privacy

object CurrentUser {
    var userDetails: UserDetailsResponse? = null
    var selectGrade : Int? = null
}

class LoginActivity : AppCompatActivity() {
    private lateinit var imageButton: ImageButton
    lateinit var editPass : EditText
    lateinit var editEmail : EditText
    private var isImageVisible = false
    lateinit var checkUser : TextView
    lateinit var signupBtn : TextView
    private lateinit var sharedPreferences: SharedPreferences

    lateinit var submitBtn : android.widget.Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)
        supportActionBar?.hide()

        val loginService = RetrofitClient.create(TMService::class.java)

        imageButton = findViewById(R.id.visible_btn)
        editPass = findViewById(R.id.edit_pw)
        editEmail = findViewById(R.id.edit_email)
        checkUser = findViewById(R.id.warning_message)

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        // 이전에 저장된 세션 정보가 있으면 자동 로그인 수행
        val savedSession = sharedPreferences.getString("sessionId", null)
        if (savedSession != null) {
            // TODO: 이전에 저장된 세션 정보로 자동 로그인 요청 수행
        }

        imageButton.setOnClickListener {
            isImageVisible = !isImageVisible
            if (isImageVisible) {
                imageButton.setImageResource(R.drawable.icon_invisible)
                editPass.inputType = InputType.TYPE_CLASS_TEXT
            } else {
                imageButton.setImageResource(R.drawable.icon_visible)
                editPass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        submitBtn = findViewById(R.id.login_btn)

        submitBtn.setOnClickListener{
            var txtEmail = editEmail.text.toString()
            var txtPass = editPass.text.toString()

            val loginData = LoginRequest(txtEmail, txtPass)
            val call = loginService.requestLogin(loginData)
            call.enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    val code = response.code()
                    if(code == 200) {
                        checkUser.visibility = View.INVISIBLE

                        // 로그인 성공 시 사용자 정보를 CurrentUser 객체에 저장
                        CurrentUser.userDetails = response.body()!!.userDetails
                        CurrentUser.selectGrade = response.body()!!.userDetails.grade

                        val setCookieHeader = response.headers().get("Set-Cookie")
                        val sessionId = setCookieHeader?.split(";")?.find { it.startsWith("JSESSIONID") }?.split("=")?.get(1)
                        sessionId?.let {
                            SessionManager.saveSessionId(this@LoginActivity, it)
                            Log.d("sessionId", sessionId)
                        }

                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent)

                        val result : LoginResponse = response.body() as LoginResponse
                        Log.d("logintag", response.code().toString() + " : 성공했습니다. " + result.message)
                    } else {
                        // 로그인 실패 시
                        checkUser.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    // 통신 실패 시
                    Log.d("logintag", "fail " + t.message)
                }
            })
        }

        signupBtn = findViewById(R.id.signup_btn)
        signupBtn.setOnClickListener {
            val intent = Intent(this@LoginActivity, Signup_privacy::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent)
        }
    }
}
