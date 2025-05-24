package YarikStudio.quantumclicker

import android.media.SoundPool
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView

class Details : AppCompatActivity() {

    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        soundPool = SoundPool.Builder().setMaxStreams(5).build()
        soundId = soundPool.load(this, R.raw.quantum_clicker_clickaudio, 1)

        findViewById<TextView>(R.id.PointsForClick).text = intent.getStringExtra("pointsForClick")
        findViewById<TextView>(R.id.PointsForElectrons).text = intent.getStringExtra("pointsForElectrons")
        findViewById<TextView>(R.id.NumberOfProtons).text = intent.getStringExtra("numberOfProtons")
        findViewById<TextView>(R.id.NumberOfElectrons).text = intent.getStringExtra("numberOfElectrons")
        findViewById<TextView>(R.id.NumberOfNeutrons).text = intent.getStringExtra("numberOfNeutrons")
        findViewById<TextView>(R.id.NumberOfClicks).text = intent.getStringExtra("numberOfClicks")

        findViewById<ImageView>(R.id.Back).setOnClickListener {
            playSoundAndFinish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                playSoundAndFinish()
            }
        })
    }

    private fun playSoundAndFinish() {
        soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 0.5f)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}