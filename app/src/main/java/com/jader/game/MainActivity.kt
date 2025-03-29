package com.jader.game

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.jader.game.databinding.ActivityMainBinding
import java.util.Locale
import kotlin.math.sqrt
import kotlin.random.Random

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private var randomNumber = generateRandomNumber()
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var lastZ: Float = 0f
    private val shakeThreshold = 800

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar textos iniciales desde recursos
        binding.messageText.text = getString(R.string.guess_prompt)
        binding.numberInput.hint = getString(R.string.guess_hint)
        binding.guessButton.text = getString(R.string.guess_button)
        binding.themeSwitch.text = getString(R.string.dark_mode)

        // Configurar el botón para verificar la adivinanza
        binding.guessButton.setOnClickListener {
            checkGuess()
        }

        // Inicializar el SensorManager y el acelerómetro
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Configurar el switch de tema
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Configurar el spinner de idiomas
        val languages = arrayOf("Español", "English")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.languageSpinner.adapter = adapter

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> setLocale("es")
                    1 -> setLocale("en")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Actualizar textos
        binding.messageText.text = getString(R.string.guess_prompt)
        binding.numberInput.hint = getString(R.string.guess_hint)
        binding.guessButton.text = getString(R.string.guess_button)
        binding.themeSwitch.text = getString(R.string.dark_mode)
    }

    private fun generateRandomNumber(): Int {
        return Random.nextInt(1, 11)
    }

    private fun checkGuess() {
        val userInput = binding.numberInput.text.toString()

        if (userInput.isEmpty()) {
            binding.messageText.text = getString(R.string.enter_number)
            return
        }

        val guess = userInput.toIntOrNull()
        if (guess == null || guess < 1 || guess > 10) {
            binding.messageText.text = getString(R.string.invalid_number)
            return
        }

        if (guess == randomNumber) {
            binding.messageText.text = String.format(getString(R.string.correct_guess), randomNumber)
            binding.guessButton.isEnabled = false
        } else {
            randomNumber = generateRandomNumber()
            binding.messageText.text = getString(R.string.wrong_guess)
            binding.numberInput.text.clear()
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val currentTime = System.currentTimeMillis()
                if ((currentTime - lastUpdate) > 100) {
                    val diffTime = currentTime - lastUpdate
                    lastUpdate = currentTime

                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]

                    val speed = sqrt((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ)) / diffTime * 10000

                    if (speed > shakeThreshold) {
                        randomNumber = generateRandomNumber()
                        binding.messageText.text = getString(R.string.number_updated)
                        binding.numberInput.text.clear()
                        binding.guessButton.isEnabled = true
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}