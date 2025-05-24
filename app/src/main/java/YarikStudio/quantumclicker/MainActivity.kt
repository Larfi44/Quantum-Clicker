package YarikStudio.quantumclicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var intent: Intent
    private lateinit var layout: ConstraintLayout
    private lateinit var coin: ImageView
    private lateinit var points: TextView
    private lateinit var proton: Button
    private lateinit var electron: Button
    private lateinit var details: ImageView
    private lateinit var Energy: ImageView
    private lateinit var soundPool: SoundPool

    private var money: Long = 0
    private var protonPrice: Long = 150
    private var electronPrice: Long = 100
    private var pointsForClick: Long = 1
    private var pointsForElectrons: Int = 0
    private var numberOfProtons: Short = 0
    private var numberOfElectrons: Short = 0
    private var numberOfNeutrons: Short = 0
    private var numberOfClicks: Short = 0
    private var soundId: Int = 0
    private var pointsForNextTeleport: Short? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        layout = findViewById(R.id.main)
        coin = findViewById(R.id.Coin)
        points = findViewById(R.id.Points)
        proton = findViewById(R.id.Proton)
        electron = findViewById(R.id.Electron)
        details = findViewById(R.id.Details)
        Energy = findViewById(R.id.Energy)

        val intent = Intent(this, Details::class.java)

        soundPool = SoundPool.Builder().setMaxStreams(5).build()
        soundId = soundPool.load(this, R.raw.quantum_clicker_clickaudio, 1)

        proton.text = "buy proton ($protonPrice)"
        electron.text = "buy electron ($electronPrice)"
        Energy.alpha = 0f

        window.attributes = window.attributes.apply {
            rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_JUMPCUT
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

            details.setOnClickListener {
                soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 0.5f)
                intent.putExtra("pointsForClick", pointsForClick.toString())
                intent.putExtra("pointsForElectrons", pointsForElectrons.toString())
                intent.putExtra("numberOfProtons", numberOfProtons.toString())
                intent.putExtra("numberOfElectrons", numberOfElectrons.toString())
                intent.putExtra("numberOfNeutrons", numberOfNeutrons.toString())
                intent.putExtra("numberOfClicks", numberOfClicks.toString())
                startActivity(intent)
            }

        fun formatNumberWithSpaces(number: Long): String {
            val formatter = DecimalFormat("###,###", DecimalFormatSymbols(Locale.getDefault()))
            formatter.groupingSize = 3
            return formatter.format(number).replace(",", " ")
        }

        fun updatePoints() {
            points.text = formatNumberWithSpaces(money).toString()
        }

        proton.setOnClickListener {
            if (money >= protonPrice) {
                numberOfProtons++
                intent.putExtra("numberOfProtons", numberOfProtons)
                money -= protonPrice
                pointsForClick *= 2
                protonPrice *= 2
                proton.text = "buy proton \n(" + formatNumberWithSpaces(protonPrice) + ")"
                updatePoints()
                soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 0.6f)
            }
            proton.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .withEndAction {
                    proton.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }
                .start()
        }

        electron.setOnClickListener {
            numberOfElectrons++
            if (money >= electronPrice) {
                money -= electronPrice
                if (pointsForElectrons > 0)
                    pointsForElectrons *= 2
                else
                    pointsForElectrons = 4
                electronPrice *= 2
                electron.text = "buy electron \n(" + formatNumberWithSpaces(electronPrice) + ")"
                updatePoints()
                soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 0.6f)
                electron.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .withEndAction {
                        electron.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start()
                    }
                    .start()
            }
        }

        fun pointsForElectrons() {
            money += pointsForElectrons
            updatePoints()
        }

        fun spawnNeutron() {
            numberOfNeutrons++
            layout.post {
                val neutron = ImageView(this).apply {
                    layoutParams = Energy.layoutParams.run {
                        ViewGroup.LayoutParams(width, height)
                    }
                    setImageDrawable(Energy.drawable)
                    layout.addView(this)
                }
                val randomX = Random.nextInt(0, layout.width - Energy.width).toFloat()
                neutron.x = randomX
                neutron.y = 0f
                neutron.scaleX = 1f
                neutron.scaleY = 1f
                neutron.animate().alpha(1f).setDuration(500).start()
                        neutron.animate()
                            .y((layout.height - neutron.height - 450).toFloat())
                            .rotationBy(Random.nextInt(360, 1200).toFloat())
                            .setDuration(Random.nextInt(2500, 4000).toLong())
                            .withEndAction {
                                neutron.animate()
                                    .alpha(0f)
                                    .setDuration(200)
                                    .withEndAction {
                                        layout.removeView(neutron)
                                    }
                                    .start()
                    }
                    .start()
                neutron.setOnClickListener {
                    it.isClickable = false
                    neutron.animate().cancel()
                    soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.25f)
                    neutron.animate().alpha(0f).scaleX(2.5f).scaleY(2.5f).setDuration(400).withEndAction {
                        layout.removeView(neutron)
                    }.start()
                    money += pointsForClick * Random.nextInt(5, 31)
                    updatePoints()
                }
            }
        }

        fun isDarkTheme(context: Context): Boolean {
            val currentNightMode = context.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK
            return currentNightMode == Configuration.UI_MODE_NIGHT_YES
        }

        if (isDarkTheme(this)) {
            points.setTextColor(Color.WHITE)
        } else {
            points.setTextColor(Color.BLACK)
        }

        fun logo() {

            val YarikStudio: ImageView = findViewById(R.id.Yarik__Studio)
            val logo: ImageView = findViewById(R.id.Logo)

            if (isDarkTheme(this)) {
                YarikStudio.setColorFilter(Color.WHITE)
                logo.setColorFilter(Color.WHITE)
            }

            YarikStudio.visibility = View.VISIBLE
            logo.visibility = View.VISIBLE
            YarikStudio.alpha = 0f
            logo.alpha = 0f

            lifecycleScope.launch {
                YarikStudio.animate().alpha(1f).setDuration(1500).start()
                delay(800)
                YarikStudio.animate().rotationX(360f).setDuration(1000).start()
                delay(200)
                logo.animate().alpha(1f).setDuration(1000).start()
                logo.animate().translationX(150f).setDuration(500).start()
                logo.animate().rotationBy(720f).setDuration(1500).start()
                delay(2000)
                YarikStudio.animate().alpha(0f).setDuration(750).start()
                logo.animate().alpha(0f).setDuration(750).start()
            }
        }

        fun click() {
            numberOfClicks++
            money += pointsForClick
            updatePoints()
            soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f)
            points.requestLayout()
            if (Random.nextInt(1,51) == 50) {
                coin.animate()
                    .scaleX(0.8f)
                    .scaleY(2f)
                    .setDuration(100)
                    .withEndAction {
                        coin.animate()
                            .scaleX(2f)
                            .scaleY(0.8f)
                            .setDuration(100)
                            .withEndAction {
                                coin.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                            }
                            .start()
                    }
                    .start()

            } else if (Random.nextInt(1,5) < 4) {
                coin.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(100)
                    .withEndAction {
                        coin.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            } else if (Random.nextInt(1,3) == 2) {
                coin.animate()
                    .scaleX(1.35f)
                    .scaleY(1.1f)
                    .setDuration(100)
                    .withEndAction {
                        coin.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            } else {
                coin.animate()
                    .scaleX(1.1f)
                    .scaleY(1.35f)
                    .setDuration(100)
                    .withEndAction {
                        coin.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            }
        }

        fun nextTeleport() {
            pointsForNextTeleport = Random.nextInt(25,150).toShort()
        }

        coin.visibility = View.GONE
        points.visibility = View.GONE
        proton.visibility = View.GONE
        electron.visibility = View.GONE
        details.visibility = View.GONE
        lifecycleScope.launch {
            delay(4000)
            coin.visibility = View.VISIBLE
            points.visibility = View.VISIBLE
            proton.visibility = View.VISIBLE
            electron.visibility = View.VISIBLE
            details.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            delay(4000)
            while (true) {
                delay(Random.nextInt(1000, 22000).toLong())
                spawnNeutron()
            }
        }

        lifecycleScope.launch {
            delay(4000)
            while (true) {
                delay(Random.nextInt(600, 1200).toLong())
                pointsForElectrons()
            }
        }

        logo()
        layout.setOnClickListener {click()}
        nextTeleport()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}