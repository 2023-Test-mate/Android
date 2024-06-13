package kr.hs.emirim.evie.testmateloginpage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class GuideActivity1 : AppCompatActivity() {

    lateinit var nextBtn : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.guide_page1)
        nextBtn = findViewById(R.id.next1)

        nextBtn.setOnClickListener {
            val intent = Intent(this, GuideActivity2::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent)
        }
    }
}