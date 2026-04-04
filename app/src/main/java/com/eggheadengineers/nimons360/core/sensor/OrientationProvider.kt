package com.eggheadengineers.nimons360.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class OrientationProvider(private val context: Context) {

    fun azimuthFlow(): Flow<Float> = callbackFlow {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotVec = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val rotMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                SensorManager.getOrientation(rotMatrix, orientation)
                val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                trySend(azimuthDeg)
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        sm.registerListener(listener, rotVec, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sm.unregisterListener(listener) }
    }
}
