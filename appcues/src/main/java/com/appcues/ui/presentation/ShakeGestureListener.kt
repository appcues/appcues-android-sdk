package com.appcues.ui.presentation

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.sqrt

internal class ShakeGestureListener(private val context: Context) : SensorEventListener {

    companion object {

        // global delay between shake events in millis
        private const val SHAKE_DELAY = 2000L

        // min acceleration to count as a shake
        private const val SHAKE_MIN_ACCELERATION = 15
        private const val SHAKE_MAX_ACCELERATION = 40
        private const val SHAKE_COUNT_THRESHOLD = 3
        private const val SHAKE_MOTION_WINDOW = 500L

        private const val NOTIFICATION_VIBRATION_TIME = 250L
        private const val NOTIFICATION_VIBRATION_AMPLITUDE = 80

        private var lastShakeTime = 0L
    }

    private var sensorManager: SensorManager? = null
    private var shakeListener: (() -> Unit)? = null
    private var shouldVibrate: Boolean = false

    private var acceleration = SensorManager.STANDARD_GRAVITY
    private var currentAcceleration = SensorManager.STANDARD_GRAVITY
    private var lastAcceleration = SensorManager.STANDARD_GRAVITY

    private var shakeCount = 0
    private var lastShakeMotion = 0L

    fun addListener(shouldVibrate: Boolean, listener: () -> Unit) {
        // set the listener and call start
        this.shouldVibrate = shouldVibrate
        this.shakeListener = listener
        start()
    }

    fun clearListener() {
        // clear listener and call stop
        shakeListener = null
        stop()
    }

    private fun notifyListener() {
        shakeListener?.let {
            it.invoke()
            if (shouldVibrate) {
                vibrate()
            }
        }
    }

    fun start() {
        // lets only start if we have a listener setup
        if (shakeListener == null) return

        sensorManager = (context.getSystemService(SENSOR_SERVICE) as SensorManager?)
            ?.also {
                it.unregisterListener(this)
                it.registerListener(
                    this,
                    it.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_GAME
                )
            }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        with(event) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = currentTime - lastShakeTime

            // guard checks
            if (this == null || sensor.type != Sensor.TYPE_ACCELEROMETER || deltaTime < SHAKE_DELAY) return

            // Fetching x,y,z values
            val x = values[0]
            val y = values[1]
            val z = values[2]
            lastAcceleration = currentAcceleration
            currentAcceleration = (sqrt(x * x + y * y + z * z) - SensorManager.STANDARD_GRAVITY).coerceAtLeast(0f)
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration += delta

            // if we are in shake motion, we check to see if we are above the below the threshold
            // before moving forward.
            if (shakeCount != 0 && (currentTime - lastShakeMotion) > SHAKE_MOTION_WINDOW) {
                shakeCount = 0
                return
            }

            // if acceleration is ok we set the lastShakeMotion to current Time
            if (acceleration > SHAKE_MIN_ACCELERATION && acceleration < SHAKE_MAX_ACCELERATION) {
                lastShakeMotion = currentTime
                // and if we got consecutive shake counts before shakeCount set to zero again
                // then it means we detected a shake motion
                if (++shakeCount >= SHAKE_COUNT_THRESHOLD) {
                    notifyListener()
                    lastShakeTime = currentTime
                    shakeCount = 0
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun vibrate() {
        if (VERSION.SDK_INT >= VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager?)
                ?.defaultVibrator
                ?.vibrate(VibrationEffect.createOneShot(NOTIFICATION_VIBRATION_TIME, NOTIFICATION_VIBRATION_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(VIBRATOR_SERVICE) as Vibrator?)
                ?.vibrate(NOTIFICATION_VIBRATION_TIME)
        }
    }
}
