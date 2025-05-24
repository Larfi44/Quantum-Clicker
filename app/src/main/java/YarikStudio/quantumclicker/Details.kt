package YarikStudio.quantumclicker

import android.annotation.SuppressLint

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Details : AppCompatActivity() {

    private lateinit var pointsForClickText: TextView
    private lateinit var pointsForElectronsText: TextView
    private lateinit var numberOfProtonsText: TextView
    private lateinit var numberOfElectronsText: TextView
    private lateinit var numberOfNeutronsText: TextView
    private lateinit var numberOfClicksText: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        pointsForClickText = findViewById(R.id.PointsForClick)
        pointsForElectronsText = findViewById(R.id.PointsForElectrons)
        numberOfProtonsText = findViewById(R.id.NumberOfProtons)
        numberOfElectronsText = findViewById(R.id.NumberOfElectrons)
        numberOfNeutronsText = findViewById(R.id.NumberOfNeutrons)
        numberOfClicksText = findViewById(R.id.NumberOfClicks)

        pointsForClickText.text = intent.getStringExtra("pointsForClick")
        pointsForElectronsText.text = intent.getStringExtra("pointsForElectrons")
        numberOfProtonsText.text = intent.getStringExtra("numberOfProtons")
        numberOfElectronsText.text = intent.getStringExtra("numberOfElectrons")
        numberOfNeutronsText.text = intent.getStringExtra("numberOfNeutrons")
        numberOfClicksText.text = intent.getStringExtra("numberOfClicks")

        val back: ImageView = findViewById(R.id.Back)
        back.setOnClickListener {
            finish()
        }
    }
}