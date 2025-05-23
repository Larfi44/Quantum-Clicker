package YarikStudio.quantumclicker

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Details : AppCompatActivity() {

    var pointsForClickText: TextView = findViewById(R.id.PointsForClick)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        fun pointsForClickTextUpdate() {
            pointsForClickText.text = intent.getStringExtra("pointsForClick")
        }

        val back: ImageView = findViewById(R.id.Back)
        back.setOnClickListener {
            finish()
        }
    }
}