package com.jader.game

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.jader.game.databinding.ActivityMainBinding
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
    }

    private fun generateRandomNumber(): Int {
        return Random.nextInt(1, 11)
    }

    private fun checkGuess() {
        val userInput = binding.numberInput.text.toString()

        if (userInput.isEmpty()) {
            binding.messageText.text = "Ingresa un numero"
            return
        }

        val guess = userInput.toIntOrNull()
        if (guess == null || guess < 1 || guess > 10) {
            binding.messageText.text = "Ingresa un numero valido entre el 1 al 10"
            return
        }

        if (guess == randomNumber) {
            binding.messageText.text = "Felicidades, adivinaste el numero: $randomNumber"
            binding.guessButton.isEnabled = false
        } else {
            randomNumber = generateRandomNumber()
            binding.messageText.text = "Incorrecto, intenta de nuevo con un numero diferente"
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
                        binding.messageText.text = "¡Número actualizado! Intenta adivinar el nuevo número."
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No necesitamos manejar cambios de precisión en este caso
    }
}