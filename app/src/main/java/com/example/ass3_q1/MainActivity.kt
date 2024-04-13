package com.example.ass3_q1

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.Serializable
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    // Define a handler for scheduling database insertions
    private val handler = Handler(Looper.getMainLooper())
    private val databaseInsertInterval = 100L // 100 milliseconds

    private lateinit var rollTextView: TextView
    private lateinit var pitchTextView: TextView
    private lateinit var yawTextView: TextView
    private lateinit var dbHelper: DatabaseHelper

    // Declare roll, pitch, and yaw as class-level variables
    private var roll = 0f
    private var pitch = 0f
    private var yaw = 0f

    // Magnetometer reading
    private var magnetometerReading = FloatArray(3)

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rollTextView = findViewById(R.id.rollTextView)
        pitchTextView = findViewById(R.id.pitchTextView)
        yawTextView = findViewById(R.id.yawTextView)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        initializeSensors()
        dbHelper = DatabaseHelper(this)
        val graphButton = findViewById<Button>(R.id.graphButton)
        graphButton.setOnClickListener {
            navigateToGraphActivity()
        }
        startPeriodicDatabaseInsertion()

    }
    private fun startPeriodicDatabaseInsertion() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Add orientation data to the database
                addOrientation(roll, pitch, yaw)

                // Schedule the next insertion after the interval
                handler.postDelayed(this, databaseInsertInterval)
            }
        }, databaseInsertInterval)
    }
    private fun addOrientation(roll: Float, pitch: Float, yaw: Float) {
        val db = dbHelper.writableDatabase
        val currentTimeMillis = System.currentTimeMillis()
        val values = ContentValues().apply {
            put(DatabaseHelper.KEY_TIMESTAMP, currentTimeMillis)
            put(DatabaseHelper.KEY_ROLL, roll)
            put(DatabaseHelper.KEY_PITCH, pitch)
            put(DatabaseHelper.KEY_YAW, yaw)
        }
        db.insert(DatabaseHelper.TABLE_ORIENTATION, null, values)
        db.close()
    }

    private fun arePermissionsGranted(): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCode) {
            // Check if all permissions are granted
            if (arePermissionsGranted()) {
                initializeSensors()
            }
        }
    }

    private fun initializeSensors() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onResume() {
        super.onResume()
        if (arePermissionsGranted()) {
            initializeSensors()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }



    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    roll = Math.toDegrees(
                        atan2(
                            event.values[1].toDouble(),
                            event.values[2].toDouble()
                        )
                    ).toFloat()
                    pitch = Math.toDegrees(
                        atan2(
                            (-event.values[0]).toDouble(),
                            sqrt(
                                event.values[1].toDouble().pow(2.0) + event.values[2].toDouble()
                                    .pow(2.0)
                            )
                        )
                    ).toFloat()

                    rollTextView.text = "Roll: $roll"
                    pitchTextView.text = "Pitch: $pitch"
                }

                Sensor.TYPE_MAGNETIC_FIELD -> {
                    // Store the magnetometer reading
                    magnetometerReading[0] = event.values[0]
                    magnetometerReading[1] = event.values[1]
                    magnetometerReading[2] = event.values[2]
                }
            }

            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER && magnetometer != null) {
                val rotationMatrix = FloatArray(9)
                val inclinationMatrix = FloatArray(9)
                val remappedRotationMatrix = FloatArray(9)
                val orientationAngles = FloatArray(3)

                val success = SensorManager.getRotationMatrix(
                    rotationMatrix,
                    inclinationMatrix,
                    event.values,
                    magnetometerReading
                )

                if (success) {
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        remappedRotationMatrix
                    )
                    SensorManager.getOrientation(remappedRotationMatrix, orientationAngles)

                    yaw = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()

                    yawTextView.text = "Yaw: $yaw"
                } else {
                    yawTextView.text = "Yaw: N/A"
                }
            } else {
                yawTextView.text = "Yaw: N/A (Magnetometer not available)"
            }
//            addOrientation(roll, pitch, yaw)
        }
    }

    private fun navigateToGraphActivity() {
        // Retrieve orientation data from the database
        val orientationDataList = dbHelper.getOrientationData()

        val intent = Intent(this, GraphActivity::class.java).apply {
            putExtra("orientation_data", orientationDataList as Serializable)
        }
        startActivity(intent)
    }
}