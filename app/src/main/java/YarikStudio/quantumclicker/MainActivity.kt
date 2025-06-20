package YarikStudio.quantumclicker

import android.annotation.SuppressLint
import android.app.Activity
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.random.Random
import kotlin.math.ceil
import kotlin.math.round

class MainActivity : AppCompatActivity() {

    private lateinit var intent: Intent
    private lateinit var layout: ConstraintLayout
    private lateinit var quantum: ImageView
    private lateinit var proton: Button
    private lateinit var electron: Button
    private lateinit var details: ImageView
    private lateinit var points: TextView
    private lateinit var Energy: ImageView
    private lateinit var soundPool: SoundPool
    private lateinit var fire: ConstraintLayout
    private lateinit var snowflake: ConstraintLayout
    private lateinit var chaos: ConstraintLayout
    private lateinit var fireWorld: TextView
    private lateinit var iceWorld: TextView
    private lateinit var chaosWorld: TextView
    private lateinit var darkWorld: TextView
    private lateinit var endText: TextView
    private lateinit var startNewGame: Button

    private var money: Long = 0
    private var protonPrice: Long = 150
    private var electronPrice: Long = 100
    private var pointsForClick: Long = 1
    private var pointsForElectrons: Long = 0
    private var numberOfProtons: Short = 0
    private var numberOfElectrons: Short = 0
    private var numberOfNeutrons: Short = 0
    private var numberOfClicks: Short = 0
    private var soundId1: Int = 0
    private var soundId2: Int = 0
    private var soundId3: Int = 0
    private var pointsForNextTeleport: UByte = 0U
    private var world: UByte = 99u
    private var end: Boolean = false
    private var lang: String = Locale.getDefault().language

    companion object {
        private const val PREFS_NAME = "GamePrefs"
        private const val KEY_MONEY = "money"
        private const val KEY_POINTS_FOR_CLICK = "pointsForClick"
        private const val KEY_POINTS_FOR_ELECTRONS = "pointsForElectrons"
        private const val KEY_ELECTRON_PRICE = "electronPrice"
        private const val KEY_PROTON_PRICE = "protonPrice"
        private const val KEY_CLICKS = "numberOfClicks"
        private const val KEY_ELECTRONS = "numberOfElectrons"
        private const val KEY_PROTONS = "numberOfProtons"
        private const val KEY_NEUTRONS = "numberOfNeutrons"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        layout = findViewById(R.id.main)
        quantum = findViewById(R.id.Coin)
        points = findViewById(R.id.Points)
        proton = findViewById(R.id.Proton)
        electron = findViewById(R.id.Electron)
        details = findViewById(R.id.Details)
        Energy = findViewById(R.id.Energy)
        fire = findViewById(R.id.Fire)
        snowflake = findViewById(R.id.Snowflake)
        chaos = findViewById(R.id.Chaos)
        fireWorld = findViewById(R.id.FireWorld)
        iceWorld = findViewById(R.id.IceWorld)
        chaosWorld = findViewById(R.id.ChaosWorld)
        darkWorld = findViewById(R.id.DarkWorld)
        endText = findViewById(R.id.EndText)
        startNewGame = findViewById(R.id.StartNewGame)

        intent = Intent(this, Details::class.java)

        soundPool = SoundPool.Builder().setMaxStreams(5).build()
        soundId1 = soundPool.load(this, R.raw.quantum_clicker_clickaudio, 1)
        soundId2 = soundPool.load(this, R.raw.teleport, 1)
        soundId3 = soundPool.load(this, R.raw.explosion, 1)

        loadGameState()

        fun isTablet(context: Context): Boolean {
            return context.resources.configuration.smallestScreenWidthDp >= 600
        }

        fun dpToPx(context: Context, dp: Float): Float {
            return dp * context.resources.displayMetrics.density
        }

        if (lang != "ru" && lang != "zh")
            lang = "en"

        fun formatNumberWithSpaces(number: Long): String {
            val formatter = DecimalFormat("###,###", DecimalFormatSymbols(Locale.getDefault()))
            formatter.groupingSize = 3
            return formatter.format(number).replace(",", " ")
        }

        fun updatePoints() {
            points.text = formatNumberWithSpaces(money).toString()
            if (lang == "en") {
                proton.text = "buy proton\n($protonPrice)"
                electron.text = "buy electron\n($electronPrice)"
            }
            if (lang == "ru") {
                proton.text = "купить протон\n($protonPrice)"
                electron.text = "купить электрон\n($electronPrice)"
            }
            if (lang == "zh") {
                proton.text = "购买质子\n($protonPrice)"
                electron.text = "购买电子\n($electronPrice)"
            }
        }

        updatePoints()

        fun isDarkTheme(context: Context): Boolean {
            val currentNightMode = context.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK
            return currentNightMode == Configuration.UI_MODE_NIGHT_YES
        }

        if (lang == "en") {
            proton.text = "buy proton\n($protonPrice)"
            electron.text = "buy electron\n($electronPrice)"
        }
        if (lang == "ru") {
            proton.text = "купить протон\n($protonPrice)"
            electron.text = "купить электрон\n($electronPrice)"
        }
        if (lang == "zh") {
            proton.text = "购买质子\n($protonPrice)"
            electron.text = "购买电子\n($electronPrice)"
        }

        Energy.alpha = 0f
        endText.alpha = 0f
        endText.visibility = View.VISIBLE
        startNewGame.alpha = 0f
        startNewGame.visibility = View.GONE
        if (isDarkTheme(this))
            startNewGame.setBackgroundColor(Color.WHITE)
        fire.visibility = View.GONE
        snowflake.visibility = View.GONE
        chaos.visibility = View.GONE
        fireWorld.visibility = View.GONE
        iceWorld.visibility = View.GONE
        chaosWorld.visibility = View.GONE
        darkWorld.visibility = View.GONE

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        details.setOnClickListener {
            if (!end) {
                soundPool.play(soundId1, 1.0f, 1.0f, 1, 0, 0.5f)
                intent.putExtra("pointsForClick", formatNumberWithSpaces(pointsForClick).toString())
                intent.putExtra(
                    "pointsForElectrons",
                    formatNumberWithSpaces(pointsForElectrons).toString()
                )
                intent.putExtra("numberOfProtons", numberOfProtons.toString())
                intent.putExtra("numberOfElectrons", numberOfElectrons.toString())
                intent.putExtra(
                    "numberOfNeutrons",
                    formatNumberWithSpaces(numberOfNeutrons.toLong()).toString()
                )
                intent.putExtra(
                    "numberOfClicks",
                    formatNumberWithSpaces(numberOfClicks.toLong()).toString()
                )
                startActivity(intent)
            }
        }

        proton.setOnClickListener {
            if (money >= protonPrice) {
                numberOfProtons++
                intent.putExtra("numberOfProtons", numberOfProtons)
                money -= protonPrice
                pointsForClick *= 2
                protonPrice *= 2
                if (lang == "en") {
                    proton.text = "buy proton\n($protonPrice)"
                }
                if (lang == "ru") {
                    proton.text = "купить протон\n($protonPrice)"
                }
                if (lang == "zh") {
                    proton.text = "购买质子\n($protonPrice)"
                }
                saveGameState()
                updatePoints()
                soundPool.play(soundId1, 1.0f, 1.0f, 1, 0, 0.6f)
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
                if (lang == "en") {
                    electron.text = "buy electron\n($electronPrice)"
                }
                if (lang == "ru") {
                    electron.text = "купить электрон\n($electronPrice)"
                }
                if (lang == "zh") {
                    electron.text = "购买电子\n($electronPrice)"
                }
                saveGameState()
                updatePoints()
                soundPool.play(soundId1, 1.0f, 1.0f, 1, 0, 0.6f)
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
                    soundPool.play(soundId1, 1.0f, 1.0f, 1, 0, 1.25f)
                    neutron.animate().alpha(0f).scaleX(2.5f).scaleY(2.5f).setDuration(400)
                        .withEndAction {
                            layout.removeView(neutron)
                        }.start()
                    money += pointsForClick * Random.nextInt(5, 31)
                    updatePoints()
                }
            }
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

                val translationXPx = dpToPx(this@MainActivity, 100f)
                logo.animate().translationX(translationXPx).setDuration(700).start()
                logo.animate().rotationBy(720f).setDuration(1500).start()

                delay(2000)
                YarikStudio.animate().alpha(0f).setDuration(700).start()
                logo.animate().alpha(0f).setDuration(750).start()
            }
        }

        fun nextTeleport() {
            world = 0u
            pointsForNextTeleport = Random.nextInt(20, 200).toUByte()
            fire.visibility = View.GONE
            snowflake.visibility = View.GONE
            chaos.visibility = View.GONE
            points.setTextColor(Color.BLACK)
            quantum.alpha = 1f
            fireWorld.visibility = View.GONE
            iceWorld.visibility = View.GONE
            chaosWorld.visibility = View.GONE
            darkWorld.visibility = View.GONE
            if (isDarkTheme(this)) {
                points.setTextColor(Color.WHITE)
                proton.setBackgroundColor(Color.WHITE)
                electron.setBackgroundColor(Color.WHITE)
                proton.setTextColor(Color.BLACK)
                electron.setTextColor(Color.BLACK)
            } else {
                points.setTextColor(Color.BLACK)
                proton.setBackgroundColor(Color.BLACK)
                electron.setBackgroundColor(Color.BLACK)
                proton.setTextColor(Color.WHITE)
                electron.setTextColor(Color.WHITE)
            }
        }

        fun newTeleport() {
            world = Random.nextInt(1, 2).toUByte()
            soundPool.play(soundId2, 2.0f, 2.0f, 5, 0, 1f)
            if (world.toUInt() == 1u) {
                layout.setBackgroundColor(Color.RED)
                proton.setBackgroundColor(Color.parseColor("#FFFFA500"))
                electron.setBackgroundColor(Color.parseColor("#FFFFA500"))
                points.setTextColor(Color.WHITE)
                fire.visibility = View.VISIBLE
                fireWorld.visibility = View.VISIBLE
                fireWorld.setTextColor(Color.WHITE)
            }
            if (world.toUInt() == 2u) {
                layout.setBackgroundColor(Color.parseColor("#FF00FFFF"))
                proton.setBackgroundColor(Color.WHITE)
                electron.setBackgroundColor(Color.WHITE)
                proton.setTextColor(Color.BLACK)
                electron.setTextColor(Color.BLACK)
                points.setTextColor(Color.BLACK)
                snowflake.visibility = View.VISIBLE
                iceWorld.visibility = View.VISIBLE
                iceWorld.setTextColor(Color.BLACK)
            }
            if (world.toUInt() == 3u) {
                layout.setBackgroundColor(Color.parseColor("#FFCCCCCC"))
                proton.setBackgroundColor(Color.WHITE)
                electron.setBackgroundColor(Color.WHITE)
                proton.setTextColor(Color.BLACK)
                electron.setTextColor(Color.BLACK)
                points.setTextColor(Color.BLACK)
                chaos.visibility = View.VISIBLE
                chaosWorld.visibility = View.VISIBLE
                chaosWorld.setTextColor(Color.BLACK)
            }
            if (world.toUInt() == 4u) {
                layout.setBackgroundColor(Color.BLACK)
                proton.setBackgroundColor(Color.GRAY)
                electron.setBackgroundColor(Color.GRAY)
                proton.setTextColor(Color.parseColor("#FF333333"))
                electron.setTextColor(Color.parseColor("#FF333333"))
                points.setTextColor(Color.parseColor("#FF333333"))
                quantum.alpha = 0.6f
                darkWorld.visibility = View.VISIBLE
                darkWorld.setTextColor(Color.parseColor("#FF333333"))
            }
            lifecycleScope.launch {
                delay(Random.nextLong(6000, 20000))
                nextTeleport()
            }
        }

        quantum.visibility = View.GONE
        points.visibility = View.GONE
        proton.visibility = View.GONE
        electron.visibility = View.GONE
        details.visibility = View.GONE
        lifecycleScope.launch {
            delay(4000)
            quantum.visibility = View.VISIBLE
            points.visibility = View.VISIBLE
            proton.visibility = View.VISIBLE
            electron.visibility = View.VISIBLE
            details.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            delay(4000)
            nextTeleport()
            while (true) {
                delay(Random.nextInt(1000, 22000).toLong())
                if (world != 4.toUByte())
                    if (!end)
                        spawnNeutron()
            }
        }

        lifecycleScope.launch {
            while (isActive) {
                if (world == 2.toUByte()) {
                    delay(Random.nextInt(1000, 4000).toLong())
                    if (world != 4.toUByte()) {
                        if (!end)
                            spawnNeutron()
                    }
                }
                delay(1000)
            }
        }

        lifecycleScope.launch {
            delay(4000)
            while (isActive) {
                delay(Random.nextInt(600, 1200).toLong())
                if (!end)
                    pointsForElectrons()
            }
        }

        fun newGame() {
            lifecycleScope.launch {
                end = false
                world = 99u
                endText.animate().alpha(0f).setDuration(500).start()
                startNewGame.animate().alpha(0f).setDuration(500).start()
                delay(3000)
                startNewGame.visibility = View.GONE
                quantum.scaleX = 1f
                quantum.scaleY = 1f
                money = 0
                points.text = formatNumberWithSpaces(money).toString()
                protonPrice = 150
                electronPrice = 100
                pointsForClick = 1
                pointsForElectrons = 0
                numberOfProtons = 0
                numberOfElectrons = 0
                world = 0u
                delay(500)
                quantum.animate().alpha(1f).setDuration(500).start()
                delay(500)
                if (lang == "en") {
                    proton.text = "buy proton\n($protonPrice)"
                    electron.text = "buy electron\n($electronPrice)"
                }
                if (lang == "ru") {
                    proton.text = "купить протон\n($protonPrice)"
                    electron.text = "купить протон\n($electronPrice)"
                }
                if (lang == "zh") {
                    proton.text = "购买质子\n($protonPrice)"
                    electron.text = "购买电子\n($electronPrice)"
                }
                points.animate().alpha(1f).setDuration(500).start()
                proton.animate().alpha(1f).setDuration(500).start()
                electron.animate().alpha(1f).setDuration(500).start()
                details.animate().alpha(1f).setDuration(500).start()
            }
        }

        fun end() {
            end = true
            nextTeleport()
            proton.animate().alpha(0f).setDuration(200).start()
            electron.animate().alpha(0f).setDuration(200).start()
            details.animate().alpha(0f).setDuration(200).start()
            lifecycleScope.launch {
                for (i in 1..42) {
                    nextTeleport()
                    soundPool.play(soundId1, 1.0f, 1.0f, 0, 0, 1.0f)
                    if (Random.nextInt(1, 3) == 2) {
                        quantum.animate()
                            .scaleX(1.4f)
                            .scaleY(1.4f)
                            .setDuration(100)
                            .withEndAction {
                                quantum.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                            }
                            .start()

                    } else if (Random.nextInt(1, 3) == 2) {
                        quantum.animate()
                            .scaleX(1.8f)
                            .scaleY(1.1f)
                            .setDuration(100)
                            .withEndAction {
                                quantum.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start()
                            }
                            .start()
                    } else {
                        quantum.animate()
                            .scaleX(1.1f)
                            .scaleY(1.8f)
                            .setDuration(100)
                            .withEndAction {
                                quantum.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start()
                            }
                            .start()
                    }

                    world = Random.nextInt(1, 5).toUByte()
                    if (world.toUInt() == 1u) {
                        layout.setBackgroundColor(Color.RED)
                        fire.visibility = View.VISIBLE
                        points.setTextColor(Color.WHITE)
                    }
                    if (world.toUInt() == 2u) {
                        layout.setBackgroundColor(Color.parseColor("#FF00FFFF"))
                        snowflake.visibility = View.VISIBLE
                        points.setTextColor(Color.BLACK)
                    }
                    if (world.toUInt() == 3u) {
                        layout.setBackgroundColor(Color.parseColor("#FFCCCCCC"))
                        chaos.visibility = View.VISIBLE
                        points.setTextColor(Color.BLACK)
                    }
                    if (world.toUInt() == 4u) {
                        layout.setBackgroundColor(Color.BLACK)
                        points.setTextColor(Color.parseColor("#FF333333"))
                        quantum.alpha = 0.6f
                    }
                    delay(200)
                }
            }
            lifecycleScope.launch {
                for (i in 1..50) {
                    points.text = formatNumberWithSpaces(
                        Random.nextLong(
                            0,
                            999_999_999_999_999_999
                        )
                    ).toString()
                    delay(100)
                }
            }
            lifecycleScope.launch {
                money = 100000000000000000
                delay(5100)
                points.text = formatNumberWithSpaces(money).toString()
                var n = 16
                for (i in 1..16) {
                    delay(200)
                    money -= ("9" + "0".repeat(n)).toLong()
                    points.text = formatNumberWithSpaces(money).toString()
                    n--
                }
                nextTeleport()
                money = 0
                points.text = formatNumberWithSpaces(money).toString()
                delay(1000)
                soundPool.play(soundId3, 10.0f, 10.0f, 100, 0, 10f)
                quantum.animate().scaleX(4.0f).scaleY(3.0f).alpha(0f).setDuration(400).start()
                points.animate().alpha(0f).setDuration(150).start()

                for (i in 1..50) {
                    delay(50)
                    spawnNeutron()
                }

                delay(2000)
                endText.animate().alpha(1f).setDuration(500).start()
                delay(1000)
                startNewGame.bringToFront()
                startNewGame.visibility = View.VISIBLE
                startNewGame.animate().alpha(1f).setDuration(500).start()
                startNewGame.setOnClickListener {
                    soundPool.play(soundId1, 1.0f, 1.0f, 1, 0, 0.5f)
                    newGame()
                }
                while (end) {
                    delay(Random.nextInt(500,2500).toLong())
                    spawnNeutron()
                }
            }
        }

            fun click() {
                if (world != 99.toUByte()) {
                    numberOfClicks++
                    pointsForNextTeleport--
                    if (pointsForNextTeleport < 1u && world.toUInt() == 0u) {
                        newTeleport()
                    }
                    money += if (world == 0.toUByte() || world == 2.toUByte())
                        pointsForClick
                    else if (world == 1.toUByte())
                        round(pointsForClick * 1.5).toLong()
                    else if (world == 3.toUByte())
                        ceil(Random.nextDouble() * 2 * pointsForClick).toLong()
                    else if (Random.nextDouble() < 0.5)
                        pointsForClick
                    else
                        return
                    saveGameState()
                    updatePoints()
                    soundPool.play(soundId1, 1.0f, 1.0f, 0, 0, 1.0f)
                    points.requestLayout()
                    if (Random.nextInt(1, 51) == 50) {
                        quantum.animate()
                            .scaleX(0.8f)
                            .scaleY(2f)
                            .setDuration(100)
                            .withEndAction {
                                quantum.animate()
                                    .scaleX(2f)
                                    .scaleY(0.8f)
                                    .setDuration(100)
                                    .withEndAction {
                                        quantum.animate()
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .setDuration(100)
                                    }
                                    .start()
                            }
                            .start()

                    } else if (Random.nextInt(1, 5) < 4) {
                        quantum.animate()
                            .scaleX(1.1f)
                            .scaleY(1.1f)
                            .setDuration(100)
                            .withEndAction {
                                quantum.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start()
                            }
                            .start()
                    } else if (Random.nextInt(1, 3) == 2) {
                        quantum.animate()
                            .scaleX(1.35f)
                            .scaleY(1.1f)
                            .setDuration(100)
                            .withEndAction {
                                quantum.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start()
                            }
                            .start()
                    } else {
                        quantum.animate()
                            .scaleX(1.1f)
                            .scaleY(1.35f)
                            .setDuration(100)
                            .withEndAction {
                                quantum.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start()
                            }
                            .start()
                    }
                }
                if (money > 999_999_999_999_999_999)
                    end()
            }

            logo()
            layout.setOnClickListener {
                if (!end) click()
            }

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

    override fun onPause() {
        super.onPause()
        saveGameState()
    }

    private fun saveGameState() {
        val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong(KEY_MONEY, money)
            putLong(KEY_POINTS_FOR_CLICK, pointsForClick.toLong())
            putLong(KEY_POINTS_FOR_ELECTRONS, pointsForElectrons.toLong())
            putLong(KEY_ELECTRON_PRICE, electronPrice)
            putLong(KEY_PROTON_PRICE, protonPrice)
            putInt(KEY_CLICKS, numberOfClicks.toInt())
            putInt(KEY_ELECTRONS, numberOfElectrons.toInt())
            putInt(KEY_PROTONS, numberOfProtons.toInt())
            putInt(KEY_NEUTRONS, numberOfNeutrons.toInt())
            apply()
        }
    }

    private fun loadGameState() {
        val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        money = sharedPref.getLong(KEY_MONEY, 0)
        pointsForClick = sharedPref.getLong(KEY_POINTS_FOR_CLICK, 1)
        pointsForElectrons = sharedPref.getLong(KEY_POINTS_FOR_ELECTRONS, 1)
        electronPrice = sharedPref.getLong(KEY_ELECTRON_PRICE, 100)
        protonPrice = sharedPref.getLong(KEY_PROTON_PRICE, 150)
        numberOfClicks = sharedPref.getInt(KEY_CLICKS, 0).toShort()
        numberOfElectrons = sharedPref.getInt(KEY_ELECTRONS, 0).toShort()
        numberOfProtons = sharedPref.getInt(KEY_PROTONS, 0).toShort()
        numberOfNeutrons = sharedPref.getInt(KEY_NEUTRONS, 0).toShort()
    }
}
