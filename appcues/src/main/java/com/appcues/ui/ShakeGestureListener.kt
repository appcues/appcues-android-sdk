package com.appcues.ui

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
import kotlin.math.abs

internal class ShakeGestureListener(private val context: Context) : SensorEventListener {

    companion object {

        private const val TICK_RATE = 60
        private const val SPEED_NORMALIZER = 10000
        private const val SHAKE_SPEED_THRESHOLD = 700
        private const val SHAKE_COUNT_THRESHOLD = 3

        private const val NOTIFICATION_VIBRATION_TIME = 250L
        private const val NOTIFICATION_VIBRATION_AMPLITUDE = 80

        private const val VALUE_X = 0
        private const val VALUE_Y = 1
        private const val VALUE_Z = 2
    }

    private var sensorManager: SensorManager? = null
    private var shakeListener: (() -> Unit)? = null
    private var shouldVibrate: Boolean = false

    private var lastTickTime: Long = 0
    private var lastValueX = -1.0f
    private var lastValueY = -1.0f
    private var lastValueZ = -1.0f
    private var shakeCount: Int = 0

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
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
    }

    override fun onSensorChanged(event: SensorEvent?) = with(event) {
        val currentTime = System.currentTimeMillis()
        val deltaTime = currentTime - lastTickTime
        // return when event is null (safety check)
        if (
            this == null ||
            // event type is other than accelerometer (should not happen because we are only listening to it)
            sensor.type != Sensor.TYPE_ACCELEROMETER ||
            // time is from last update is less than defined tick rate
            deltaTime < TICK_RATE
        ) return

        lastTickTime = currentTime

        // do math to figure out speed
        val x = values[VALUE_X]
        val y = values[VALUE_Y]
        val z = values[VALUE_X]
        val speed: Float = abs(x + y + z - lastValueX - lastValueY - lastValueY) / deltaTime * SPEED_NORMALIZER

        // if speed is greater than speed threshold
        if (speed > SHAKE_SPEED_THRESHOLD) {
            // increment shake count
            shakeCount++
            // check if its a real shake
            if (shakeCount > SHAKE_COUNT_THRESHOLD) {
                // notify and clean shakeCount
                notifyListener()
                shakeCount = 0
            }
        } else {
            // last gesture was not part of a shake so we clear this value too
            shakeCount = 0
        }

        // update with new values
        lastValueX = values[VALUE_X]
        lastValueY = values[VALUE_Y]
        lastValueZ = values[VALUE_Z]
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
